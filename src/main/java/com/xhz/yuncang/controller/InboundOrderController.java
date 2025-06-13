package com.xhz.yuncang.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xhz.yuncang.dto.*;
import com.xhz.yuncang.entity.*;
import com.xhz.yuncang.utils.AjaxResult;
import com.xhz.yuncang.utils.BfsFindPath;
import com.xhz.yuncang.utils.Constants;
import com.xhz.yuncang.utils.UserHolder;
import com.xhz.yuncang.vo.path.CarStorageToShelf;
import com.xhz.yuncang.vo.path.Point;
import com.xhz.yuncang.vo.shelf.AllShelfInfoVo;
import com.xhz.yuncang.vo.shelf.ShelfInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.xhz.yuncang.service.*;
import com.xhz.yuncang.vo.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
/**
 * 入库单管理控制器
 *
 * <p>负责处理入库单的全生命周期管理，包括创建、删除、查询和路径规划等功能。
 * 提供入库单的CRUD操作，并与AGV小车调度系统集成实现智能仓储管理。
 *
 * <p>主要功能模块：
 * <ul>
 *   <li>入库单创建与校验</li>
 *   <li>入库单状态管理</li>
 *   <li>分页查询与模糊搜索</li>
 *   <li>货架容量计算与分配</li>
 *   <li>AGV运输路径规划</li>
 * </ul>
 * @author xhz
 * @see InboundOrderService 入库单服务接口
 * @see InboundOrderDetailService 入库单明细服务
 * @see AgvCarService AGV小车服务
 */
@RestController
@PreAuthorize("hasRole('管理员') or hasRole('操作员')")
//@RequestMapping("/inboundOrder")
public class InboundOrderController {
    /**
     * 入库单服务实例
     */
    @Autowired
    private InboundOrderService inboundOrderService;

    /**
     * 用户服务实例
     */
    @Autowired
    private UserService userService;
    /**
     * 库存服务实例
     */
    @Autowired
    private InventoryService inventoryService;
    /**
     * 货架服务实例
     */
    @Autowired
    private StorageShelfService storageShelfService;
    /**
     * 入库单明细服务实例
     */
    @Autowired
    private InboundOrderDetailService inboundOrderDetailService;
    /**
     * 仓储控制器实例
     */
    @Autowired
    private AllStorageController allStorageController;

    /**
     * 货架库存服务实例
     */
    @Autowired
    private ShelfInventoryService shelfInventoryService;
    /**
     * AGV小车服务实例
     */
    @Autowired
    private AgvCarService agvCarService;
    /**
     * 商品服务实例
     */
    @Autowired
    private ProductService productService;

    @Autowired
    private RemindService remindService;
//    /**
//     * 员工点击入库时调用该函数更新入库单信息
//     * 先更新入库单的状态（把未开始变为已入库)---》调用函数改变位置，更新shelfInventory表---》改变库存种各种商品的总数
//     */
//    @PostMapping("/update")
//    public ResponseEntity<AjaxResult> update() {
//        if (inboundOrderService.updateStatus(orderNumber, newStatus)) {
//            return ResponseEntity.ok(AjaxResult.success("状态更新成功"));
//        }
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(AjaxResult.error("状态更新失败"));
//    }

//    @PostMapping("/getByName")
//    public ResponseEntity<AjaxResult> getByName(@RequestBody Map<String, String> order) {
//        String orderName = order.get("orderName");
//        InboundOrder inboundOrder = inboundOrderService.getByOrderName(orderName);
//        if (inboundOrder == null) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(AjaxResult.error("入库单不存在"));
//        }
//        return ResponseEntity.ok(AjaxResult.success(new InboundOrderVo(inboundOrder.getOrderName(),inboundOrder.getOrderNumber(),
//                inboundOrder.getCreateTime(),inboundOrder.getUserId(),inboundOrder.getStatus())));
//    }

    /**
     * 创建入库单
     *
     * <p>处理入库单创建请求，执行以下操作：
     * <ol>
     *   <li>校验订单名称唯一性</li>
     *   <li>计算货架总容量</li>
     *   <li>校验商品是否存在</li>
     *   <li>生成雪花算法订单编号</li>
     *   <li>保存主表和明细表数据</li>
     * </ol>
     *
     * @param inboundOrderDTO 入库单数据传输对象，包含：
     *                        <ul>
     *                          <li>orderName - 订单名称</li>
     *                          <li>details - 商品明细列表</li>
     *                        </ul>
     * @return 响应实体，包含：
     *         <ul>
     *           <li>成功：状态码200+成功消息</li>
     *           <li>失败：状态码400+错误原因（订单重复/货架不足/商品不存在等）</li>
     *         </ul>
     * @throws Exception 当出现以下情况时返回400错误：
     *                               <ul>
     *                                 <li>订单名称为空</li>
     *                                 <li>明细列表为空</li>
     *                                 <li>订单名称重复</li>
     *                                 <li>货架容量不足</li>
     *                                 <li>商品不存在</li>
     *                               </ul>
     */
    @PostMapping("/inbound")
    public ResponseEntity<AjaxResult> addInboundOrder(@RequestBody InboundOrderDTO inboundOrderDTO){
        System.out.println("inboundOrderDTO:  "+inboundOrderDTO);
        if (inboundOrderDTO==null||inboundOrderDTO.getDetails()==null||inboundOrderDTO.getDetails().isEmpty()||inboundOrderDTO.getOrderName()==null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"请填入正确信息"));
        }
        InboundOrder order = inboundOrderService.getByOrderName(inboundOrderDTO.getOrderName());
        if (order!=null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"订单名称重复"));
        }else {
            //添加到库存
            // ToDo 各种前置判断
            double maxWeights=0;
            List<StorageShelf> allShelves = storageShelfService.findAllShelves();
            if (allShelves.isEmpty()){

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"没有货架"));
            }else {
                for (StorageShelf storageShelf:allShelves){
                    String shelfCode = storageShelf.getShelfCode();
                    List<ShelfInventory> byShelfCode = shelfInventoryService.findByShelfCode(shelfCode);
                    if (byShelfCode.isEmpty()){
                        maxWeights+=storageShelf.getMaxWeight();
                    }
                }
            }
            //入库单的重量
            List<SalesOrderDetailAddDTO> productList1 = inboundOrderDTO.getDetails();
            UserVo user = UserHolder.getUser();
            Double orderWeights=0d;
            for (SalesOrderDetailAddDTO productDTO:productList1){
                String name = productDTO.getName();
                Product product = productService.findByName(name);

                if (product==null){
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"没有该物品"));
                }

                Integer quantity = productDTO.getQuantity();
                if (quantity==null){
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"填写数字"));
                }
                productDTO.setSku(product.getSku());
                orderWeights+=product.getWeight()*quantity;
            }
            if (orderWeights>maxWeights){
                remindService.saveRemind(new Remind(null,Constants.REMIND_WARNING,"货架数量不足","于"+LocalDateTime.now()+",时刻,"+
                        user.getUserType()+user.getUsername()+"在添加入库单时，货架数量不足，建议添加总承重大于"+(orderWeights-maxWeights)+"的货架",
                        LocalDateTime.now(),"0"));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"货架容量不足"));
            }
            //可以添加
            //更新入库表
            //雪花算法生成OrderNumber
            String idStr = IdWorker.getIdStr();
            UserVo user1 = UserHolder.getUser();
            String userId = userService.findByUname(user1.getUsername()).getUserId();
            InboundOrder inboundOrder = new InboundOrder(null,inboundOrderDTO.getOrderName(),idStr, LocalDateTime.now(),
                    userId,Constants.STATUS_ORDER_TOSTART);
            inboundOrderService.addOneInboundOrder(inboundOrder);

            //更新入库详细表
            List<SalesOrderDetailAddDTO> productList = inboundOrderDTO.getDetails();
            for (SalesOrderDetailAddDTO productDTO:productList){
                String sku = productDTO.getSku();
                Integer quantity = productDTO.getQuantity();
                Inventory product = inventoryService.getBySku(sku);
                InboundOrderDetail inboundOrderDetail = new InboundOrderDetail(null,idStr,sku,(long)quantity);
                inboundOrderDetailService.addOneInboundOrderDetail(inboundOrderDetail);
            }
            // ToDo 前置条件判断
            // ToDo 分配算法放置处
            remindService.saveRemind(new Remind(null,Constants.REMIND_SUCCESS,"成功添加入库单","于"+LocalDateTime.now()+",时刻,"+
                    user.getUserType()+user.getUsername()+"成功添加了编号为"+idStr+"的入库单",
                    LocalDateTime.now(),"1"));
            return ResponseEntity.ok().body(AjaxResult.success("添加成功"));
        }
    }

    /**
     * 删除入库单
     * @param id 入库单id
     * @return
     */
    @DeleteMapping("/inbound/{id}")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<AjaxResult> deleteInboundOrderByName(@PathVariable Long id) {
        InboundOrder order = inboundOrderService.getById(id);
        String orderName = order.getOrderName();
        if (order == null) {
            //未查找到对应的入库单
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400, "不存在该入库单"));
        } else if (order.getStatus() == Constants.STATUS_ORDER_TOSTART) {
            //想要删除的入库单处于未开始的状态，则无法删除
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400, "该入库单未开始"));
        } else {
            //进行删除，返回操作结果
            Boolean b = inboundOrderService.deleteByOrderName(orderName);
            if (b) {//如果成功删除了入库单，则接着删除和该入库单相关的明细表
                List<InboundOrderDetail> orders = inboundOrderDetailService.listByOrderNumber(order.getOrderNumber());
                for (InboundOrderDetail orderDetail : orders) {
                    Boolean c = inboundOrderDetailService.deleteByOrderNumberAndSku(orderDetail.getOrderNumber(), orderDetail.getSku());
                    if (!c) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error("删除失败"));
                    }
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error("删除失败"));
            }
        }
        UserVo user = UserHolder.getUser();
        remindService.saveRemind(new Remind(null,Constants.REMIND_SUCCESS,"成功删除入库单","于"+LocalDateTime.now()+",时刻,"+
                user.getUserType()+user.getUsername()+"成功删除了编号为"+order.getOrderNumber()+"的入库单",
                LocalDateTime.now(),"1"));
        return ResponseEntity.ok().body(AjaxResult.success());
    }
    /**
     * 分页查询入库单
     *
     * <p>支持分页和按订单号模糊查询，返回结构化的分页数据：
     * <ul>
     *   <li>当前页数据列表</li>
     *   <li>总记录数</li>
     *   <li>前后页导航信息</li>
     * </ul>
     *
     * @param current 当前页码（从1开始）
     * @param pageSize 每页记录数（默认10）
     * @param orderName 订单名称模糊查询条件
     * @return 响应实体，包含：
     *         <ul>
     *           <li>分页数据对象</li>
     *           <li>分页导航信息</li>
     *           <li>转换后的VO列表</li>
     *         </ul>
     * @throws Exception 当出现以下情况时返回400错误：
     *                               <ul>
     *                                 <li>页码小于1</li>
     *                                 <li>页码超过最大页数</li>
     *                               </ul>
     */
        @GetMapping("/inbound")
        public ResponseEntity<AjaxResult> listInboundOrdersByPage (
                @RequestParam(defaultValue = "1")int current,
                @RequestParam(defaultValue = "10")int pageSize,
                @RequestParam(defaultValue = "") String orderName
        ){ // 可选负责人过滤
            if (current < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400, "页数小于1"));
            }
            Page<InboundOrder> page = new Page<>(current, pageSize);
            QueryWrapper<InboundOrder> queryWrapper = new QueryWrapper<>();

            if (!orderName.isEmpty()) {
                queryWrapper.like("order_number", orderName);
            }
            Page<InboundOrder> result = inboundOrderService.page(page, queryWrapper);

            if (result.getPages() == 0) {
                return ResponseEntity.ok().body(AjaxResult.success("当前入库单为空", ""));
            }
            if (current > result.getPages()) {
                //大于最大页数
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400, "页数大于最大页数"));
            }
            // 页数超限检查
            // 分页导航计算
            int nowPage = (int) result.getCurrent();
            int beforePage = nowPage == 1 ? -1 : nowPage - 1;
            int nextPage = nowPage == result.getPages() ? -1 : nowPage + 1;
            List<InboundOrder> inboundOrders = result.getRecords();
            // 转换为VO列表
            List<InboundOrderVo> voList = inboundOrders.stream().map(order -> {
                String orderNumber = order.getOrderNumber();
                List<InboundOrderDetail> inboundOrderDetails = inboundOrderDetailService.listByOrderNumber(orderNumber);
                List<SalesOrderDetailAddDTO> detailAddDTOS = new ArrayList<>();
                if (!inboundOrderDetails.isEmpty()){
                    for(InboundOrderDetail inbound:inboundOrderDetails){
                        Product bySku = productService.findBySku(inbound.getSku());
                        detailAddDTOS.add(new SalesOrderDetailAddDTO(bySku.getName(), bySku.getSku(),inbound.getQuantity().intValue() ));
                    }
                }
                return new InboundOrderVo(
                            order.getId(),
                            order.getOrderName(),
                            order.getOrderNumber(),
                            order.getCreateTime(),
                            order.getUserId(),
                            order.getStatus(),
                            detailAddDTOS);
                    }).toList();

            // 构建分页响应
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("prev", beforePage);
            responseMap.put("next", nextPage);
            responseMap.put("total", result.getTotal());
            responseMap.put("list", voList);
            return ResponseEntity.ok().body(AjaxResult.success(responseMap));
        }

        public static int beginX =11;
        public static int beginY =24;
        public static int beginZ=1;
        public static int endX=11;
        public static int endY=12;
        public static int endZ=1;
        public static int maxBordX =48;
        public static int maxBordY =34;

        @Autowired
        private FactoryConfigService factoryConfigService;
    /**
     * 算法实现入库
     * @param id
     * @return
     */
    @GetMapping("/path/{id}")
        public ResponseEntity<AjaxResult>  findFirstShortestPath(
                @PathVariable Long id
        ){

            System.out.println(id);
            InboundOrder inboundOrder = inboundOrderService.getById(id);
            if (inboundOrder==null){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"没有该订单"));
            }
            String status = inboundOrder.getStatus();
            if (!Objects.equals(status, Constants.STATUS_ORDER_TOSTART)){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"请提交未开始的入库单"));
            }
            String orderNumber = inboundOrder.getOrderNumber();
            /**
             *确保了订单正常，下面找最短路径的货架
             */
//            List<ShelfInfoVo> allShelves = shelfInventoryService.getAllShelves();
            //////////////////////////////////////////////////////////////////
            //这里简单去个重，后面要改，不然有ShelfCode重复的情况，导致后面出错
            List<ShelfInfoVo> allShelves = shelfInventoryService.getAllShelves().stream()
                    .collect(Collectors.toMap(
                            ShelfInfoVo::getShelfCode,
                            Function.identity(),
                            (existing, replacement) -> existing // 遇重复时保留第一个
                    ))
                    .values()
                    .stream()
                    .collect(Collectors.toList());

            //////////////////////////////////////////////////////////////////
            List<ShelfInfoVo> sortedShelves = allShelves.stream()
                    .sorted(Comparator.comparingInt(shelf -> {
                        int x = (int) Double.parseDouble(shelf.getShelfX());
                        int y = (int) Double.parseDouble(shelf.getShelfY());
                        int z = (int) Double.parseDouble(shelf.getShelfZ()); // 这里改为 Double
                        return Math.abs(x - beginX)*Constants.MOVE_DIFFER_X + Math.abs(y - beginY)*Constants.MOVE_DIFFER_Y
                                + Math.abs(z - beginZ+1) * Constants.MOVE_DIFFER_Z;
                    }))
                    .collect(Collectors.toList());
            System.out.println(sortedShelves);

            System.out.println("最短路径的货架List（从小到大）："+sortedShelves);
            /**
             * 获得了最短路径的货架的List，可以按照这个分配货物，注意分配的货物的重量+该货架已有的重量不能超过货架的maxWeight
             */
            List<InboundOrderDetail> inboundOrderDetails = inboundOrderDetailService.listByOrderNumber(orderNumber);
            // 返回结果：每个货架对应的商品列表
            List<Map<String, List<SalesOrderDetailAddDTO>>> result = new ArrayList<>();
            // 每个货架分配的商品记录：shelfCode → List<SalesOrderDetailAddDTO>
            Map<String, List<SalesOrderDetailAddDTO>> shelfAllocation = new LinkedHashMap<>();
            // 每个货架当前已用重量缓存
            Map<String, Double> shelfCurrentWeight = new HashMap<>();
            // 初始化每个货架的已有重量
            for (ShelfInfoVo shelf : sortedShelves) {
                String shelfCode = shelf.getShelfCode();
                List<ShelfInventory> shelfInventories = shelfInventoryService.findByShelfCode(shelfCode);

                double existingWeight = 0;
                if (!shelfInventories.isEmpty()){
                    for (ShelfInventory shelfInventory:shelfInventories){
                        String sku = shelfInventory.getSku();
                        Long quantity = shelfInventory.getQuantity();
                        Product bySku = productService.findBySku(sku);
                        if (bySku==null){
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"货物不存在"));
                        }
                        existingWeight += (double) (bySku.getWeight()*quantity);
                    }
                }
                shelfCurrentWeight.put(shelfCode, existingWeight);
                shelfAllocation.put(shelfCode, new ArrayList<>());
            }

            // 遍历订单中每个 SKU 项
            for (InboundOrderDetail detail : inboundOrderDetails) {
                String sku = detail.getSku();
                //订单里该物品的数量
                long quantityToAllocate = detail.getQuantity();
                Product bySku = productService.findBySku(sku);
                Double singleWeight = bySku.getWeight();
                String productName = bySku.getName();
                System.out.println("物品："+productName+"  ;重量："+singleWeight);
                for (ShelfInfoVo shelf : sortedShelves) {
                    if (quantityToAllocate <= 0) break;

                    String shelfCode = shelf.getShelfCode();
                    long maxWeight = shelf.getMaxWeight();
                    double currentWeight = shelfCurrentWeight.getOrDefault(shelfCode, 0.0);//获取货架当前的重量
                    double availableWeight = maxWeight - currentWeight;//该货架的剩余重量

                    long maxQuantityCanFit = singleWeight == 0.0 ? 0 : (long)Math.floor(availableWeight / singleWeight); //可放的数量，向下取整
                    if (maxQuantityCanFit <= 0) continue;

                    long quantityAllocated = Math.min(quantityToAllocate, maxQuantityCanFit);  //订单里的货物数量与可放的物品数量取最小值，得到该货架放该物品的数量
                    double weightAllocated = quantityAllocated * singleWeight;//得到该货架放该物品的重量

                    // 构造分配的商品对象
                    SalesOrderDetailAddDTO item = new SalesOrderDetailAddDTO();
                    item.setName(productName);
                    item.setSku(sku);
                    item.setQuantity((int) quantityAllocated);

                    shelfAllocation.get(shelfCode).add(item);
                    shelfCurrentWeight.put(shelfCode, currentWeight + weightAllocated);//每次更新货架层的存的重量
                    quantityToAllocate -= quantityAllocated;//订单需要的数量=订单需要的数量-放到货架的数量，  可能 >0：循环继续  =0：该物品放完了；退出
                }

                if (quantityToAllocate > 0) {
                    //这里quantityToAllocate>0 仅有一种情况，即货架放完了但订单里物品还有剩余
//                    throw new RuntimeException("SKU " + sku + " 剩余未分配数量：" + quantityToAllocate);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"货架不足"));
                }
            }
            Set<Map.Entry<String, List<SalesOrderDetailAddDTO>>> entries = shelfAllocation.entrySet();
            // 组装结果为 List<Map<String, List<DTO>>>
            for (Map.Entry<String, List<SalesOrderDetailAddDTO>> entry : shelfAllocation.entrySet()) {
                Map<String, List<SalesOrderDetailAddDTO>> shelfMap = new HashMap<>();
                shelfMap.put(entry.getKey(), entry.getValue());

                result.add(shelfMap);
            }


            System.out.println("============================");
            System.out.println(result);

            /**
             * 我们得到了订单里的物品对应的货架后，后面把每个货架层的订单物品分配给小车，
             *          注意：1、小车的最大承重可能会小于需要运到该货架层物品的重量，这时候就要多一个小车装配了
             *               2、可能会出现小车复用的情况
             *               3、小车从起点出发运货到货架，再从货架返回终点（48，34）
             *               返回  List<CarStorageToShelf>
             *                   其中：public class CarStorageToShelf{
             *                          car_number： string   //车子编号
             *                          max_weight:  double   //最大载重
             *                          sun_weight:  double   //当前承重
             *                          startX:  int
             *                          startY:  int
             *                          middleX:  int        //货架交互点x
             *                          middleY   int        //货架交互点y
             *                          middleZ   int        //货架层数z
             *                          endX      int        //终点x
             *                          endZ      int        //终点y
             *                          shelf_code 货架层编号
             *                          products:  List<SalesOrderDetailAddDTO>   //车子放的物品list
             *                   }
             *
             */
            List<AgvCar> agvCars = agvCarService.findAll();
            if (agvCars.isEmpty()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"没有小车！！"));
            }
            String position="";
            //将小车按照maxWeight从大到小排序，载重大的先装货
            agvCars.sort(Comparator.comparingDouble(AgvCar::getMaxWeight).reversed());
            List<CarStorageToShelf> carTasks = new ArrayList<>();
            //小车循环使用索引
            int carIndex=0;

            // 快速获取 shelf 坐标信息
            Map<String, ShelfInfoVo> shelfInfoMap = sortedShelves.stream()
                    .collect(Collectors.toMap(ShelfInfoVo::getShelfCode, Function.identity()));

            for (Map<String, List<SalesOrderDetailAddDTO>> shelfEntry : result){
                for (Map.Entry<String, List<SalesOrderDetailAddDTO>> entry : shelfEntry.entrySet()){
                    //该货架层的编码
                    String shelfCode = entry.getKey();
                    //该货架层存的东西和数量
                    List<SalesOrderDetailAddDTO> items = entry.getValue();
                    //获得该货架层的位置等详细信息
                    ShelfInfoVo shelf = shelfInfoMap.get(shelfCode);
                    if (shelf==null){
                        System.out.println("shelfCode:"+shelfCode+"没有该货架");
                        continue;
                    }
                    int middleX = (int)Double.parseDouble(shelf.getShelfX());
                    if (middleX==14||middleX==18||middleX==22||middleX==26||middleX==30||middleX==34||
                    middleX==38||middleX==42||middleX==46){
                        middleX=middleX-1;
                        position="左";
                    }else {
                        middleX=middleX+1;
                        position="右";
                    }
                    int middleY = (int)Double.parseDouble(shelf.getShelfY());
                    int middleZ = (int)Double.parseDouble(shelf.getShelfZ());

                    // 拆分当前货架的商品清单（可能多个车来装）
                    List<SalesOrderDetailAddDTO> remainingItems = new ArrayList<>(items);
                    while(!remainingItems.isEmpty()){
                        //取一个可用的小车
                        AgvCar car = agvCars.get(carIndex);
                        carIndex=(carIndex+1)% agvCars.size();

                        //该小车最大载重
                        double maxWeightKg = car.getMaxWeight();
                        double sumWeight = 0.0;
                        List<SalesOrderDetailAddDTO> carLoad = new ArrayList<>();

                        Iterator<SalesOrderDetailAddDTO> iter = remainingItems.iterator();
                        while (iter.hasNext()){
                            SalesOrderDetailAddDTO dto = iter.next();
                            Product product = productService.findBySku(dto.getSku());
                            Double weight = product.getWeight();//单价重量
                            Integer quantity = dto.getQuantity();//数量
                            Double totalWeight =weight*quantity;
                            if (sumWeight+totalWeight<=maxWeightKg){
                                //可以全装
                                carLoad.add(dto);
                                sumWeight+=totalWeight;
                                iter.remove();//加上后删掉该条货物记录
                            }else {
                                //不能全装
                                int canLoadQty = (int) Math.floor((maxWeightKg - sumWeight) / weight);
                                if (canLoadQty>0){
                                    //可以装部分
                                    SalesOrderDetailAddDTO partial = new SalesOrderDetailAddDTO();
                                    partial.setName(dto.getName());
                                    partial.setSku(dto.getSku());
                                    partial.setQuantity(canLoadQty);
                                    carLoad.add(partial);//小车装这些
                                    sumWeight+=canLoadQty*weight;
                                    dto.setQuantity(quantity - canLoadQty);
                                }
                                //canLoadQty<=0说明该小车一点都装不了了，退出while
                                break;
                            }
                        }

                        // 5. 构建运输任务
                        AgvCar agvCar = agvCarService.findByCarNumber(car.getCarNumber());
                        CarStorageToShelf task = new CarStorageToShelf();
                        task.setCarNumber(car.getCarNumber());
                        task.setMaxWeight(maxWeightKg);
                        task.setSumWeight(sumWeight);
                        task.setStartX(agvCar.getLocationX().intValue());//改成小车停放点X
                        task.setStartY(agvCar.getLocationY().intValue());//改成小车停放点Y
                        task.setMiddleX(middleX);
                        task.setMiddleY(middleY);
                        task.setMiddleZ(middleZ);
                        task.setEndX(agvCar.getLocationX().intValue());//改成小车停放点X
                        task.setEndY(agvCar.getLocationY().intValue());//改成小车停放点Y
                        task.setShelfCode(shelfCode);
                        task.setProducts(carLoad);
                        task.setPosition(position);

                        carTasks.add(task);
                    }

                }
            }
            System.out.println("小车装的货物以及去的位置："+carTasks);
            /**
             * 知道了小车装的哪些货及其数量，以及小车的出发点、货架交互点，以及终点
             * 现在我们先生成地图，再根据地图找路径
             */

            /**
             * 地图生成完毕，可以开始最短路径查找了
             * 想要输出  AllCarPath{
             *             List<CarStorageToShelf> details;
             *          }
             */
            for (CarStorageToShelf task:carTasks){
                int startX = task.getStartX();
                int startY = task.getStartY();
                int midX = task.getMiddleX();
                int midY = task.getMiddleY();
                int middleZ = task.getMiddleZ(); // 当前货架的层数
                int endX = task.getEndX();
                int endY = task.getEndY();
                if (task.getProducts().isEmpty()){
                    continue;
                }

                //////////////////////////////////////////
                //从停车点--》入库区域
                int[][] map1=BfsFindPath.InMapToProduct(startX,startY,9,25);
                List<Point> path1_1 =BfsFindPath.bfsFindPath(map1,startX,startY,5,33,0);
                //开始装货//2s
                int LoadingTime=Constants.INTERACTION;
                int time = path1_1.get(path1_1.size() - 1).getTime();
                for (int i=1;i<=LoadingTime;i++){
                    path1_1.add(new Point(5,33,time+i));
                }
                int resultTime=time+LoadingTime;

                int[][] map2=BfsFindPath.InMapToProduct(startX,startY,0,0);
                List<Point> path1_2 =BfsFindPath.bfsFindPath(map2,5,33,11,24,resultTime);
                // 5. 合并路径
                if (!path1_2.isEmpty() && !path1_1.isEmpty()) {
                    if (path1_1.get(path1_1.size() - 1).getX() == path1_2.get(0).getX() &&
                            path1_1.get(path1_1.size() - 1).getY() == path1_2.get(0).getY()) {
                        path1_2.remove(0);
                    }
                }
                int resultTime1=path1_2.get(path1_2.size()-1).getTime();
                //////////////////////////////////////////
                // 1. 生成地图
                int[][] map = BfsFindPath.InMap();
                // 2. 起点 → 中间点
//                List<Point> path1 = BfsFindPath.bfsFindPath(map, startX, startY, midX, midY, 0);
                List<Point> path1 = BfsFindPath.bfsFindPath(map, 11, 24, midX, midY, resultTime1);
                /////////////////////////////////
                if (!path1_2.isEmpty() && !path1.isEmpty()) {
                    if (path1_2.get(path1_2.size() - 1).getX() == path1.get(0).getX() &&
                            path1_2.get(path1_2.size() - 1).getY() == path1.get(0).getY()) {
                        path1.remove(0);
                    }
                }
                ////////////////////////////////
                // 3. 插入Z轴等待时间
                int zTimeCost = middleZ * 2;
                int lastTime = path1.get(path1.size() - 1).getTime();
                for (int i = 1; i <= zTimeCost; i++) {
                    path1.add(new Point(midX, midY, lastTime + i)); // 停在原地增加时间
                }
                int resumeTime = lastTime + zTimeCost;
                // 4. 中间点 → 终点
                List<Point> path2 = BfsFindPath.bfsFindPath(map, midX, midY, 11,12, resumeTime);
                // 5. 合并路径
                if (!path2.isEmpty() && !path1.isEmpty()) {
                    if (path1.get(path1.size() - 1).getX() == path2.get(0).getX() &&
                            path1.get(path1.size() - 1).getY() == path2.get(0).getY()) {
                        path2.remove(0);
                    }
                }

                //////////////////////////
                //回停机位
                int resultTime2=path2.get(path2.size()-1).getTime();
                List<Point> path2_1 = BfsFindPath.bfsFindPath(map2, 11, 12, endX,endY, resultTime2);
                if (!path2_1.isEmpty() && !path2.isEmpty()) {
                    if (path2.get(path2.size() - 1).getX() == path2_1.get(0).getX() &&
                            path2.get(path2.size() - 1).getY() == path2_1.get(0).getY()) {
                        path2_1.remove(0);
                    }
                }


                ////////////////////////
                List<Point> fullPath = new ArrayList<>(path1_1);
                fullPath.addAll(path1_2);
                fullPath.addAll(path1);
                fullPath.addAll(path2);
                fullPath.addAll(path2_1);
//                List<Point> fullPath = new ArrayList<>(path1);
//                fullPath.addAll(path2);
                // 6. 注入
                task.setPaths(fullPath);
            }

//            carTasks
        carTasks = carTasks.stream()
                .filter(task -> task.getPaths() != null && !task.getPaths().isEmpty())
                .collect(Collectors.toList());

        int globalStartTime = 0;
        Map<String, Integer> carLastEndTime = new HashMap<>();
        Map<Integer, Set<String>> occupied = new HashMap<>(); // 时间戳 -> 占用位置

//        String chooseNoBlocked="是";

        //是否处理小车重合的冲突
        String dealing="是";
        List<FactoryConfig> list = factoryConfigService.list();
        if (list!=null){
            FactoryConfig first = list.getFirst();
            String pathStrategy = first.getPathStrategy();
            if (!pathStrategy.equals("是")&&!pathStrategy.equals("否")){
                dealing="是";
            }else {
                if (pathStrategy.equals("是")){
                    dealing="否";
                }else {
                    dealing="是";
                }
            }
        }
        if (dealing.equals("是")){
            //处理冲突
            for (CarStorageToShelf task : carTasks) {
                String carNumber = task.getCarNumber();
                List<Point> originalPath = task.getPaths();

                // 初始化出发时间
                int startTime = carLastEndTime.getOrDefault(carNumber, globalStartTime);
                List<Point> adjustedPath;

                // 处理冲突（延迟出发）
                while (true) {
                    boolean conflict = false;
                    adjustedPath = new ArrayList<>();

                    for (Point p : originalPath) {
                        int t = p.getTime() + startTime;
                        String pos = p.getX() + "," + p.getY();
                        Set<String> occupiedAtT = occupied.getOrDefault(t, new HashSet<>());

                        if (occupiedAtT.contains(pos)) {
                            conflict = true;
                            break;
                        }
                        adjustedPath.add(new Point(p.getX(), p.getY(), t));
                    }

                    if (!conflict) break;
                    startTime++; // 延后出发时间，重试
                }

                // 更新 paths
                task.setPaths(adjustedPath);
                task.setDispatchTime(startTime);

                // 占位记录
                for (Point p : adjustedPath) {
                    int t = p.getTime();
                    String pos = p.getX() + "," + p.getY();
                    occupied.computeIfAbsent(t, k -> new HashSet<>()).add(pos);
                }

                // 更新该车最后时间
                int endTime = adjustedPath.get(adjustedPath.size() - 1).getTime() + 1;
                carLastEndTime.put(carNumber, endTime);
            }


        }else {
            //不处理冲突
            for (CarStorageToShelf task : carTasks) {
                String carNumber = task.getCarNumber();
                // 跳过无路径的小车任务
                if (task.getPaths() == null || task.getPaths().isEmpty()) {
                    continue;
                }
                // 获取该车上一次结束时间（若第一次，则为当前全局时间）
                int startTime = carLastEndTime.getOrDefault(carNumber, globalStartTime);
                // 设置 dispatchTime
                task.setDispatchTime(startTime);

                // 调整路径中每个点的时间（加上 dispatchTime）
                List<Point> adjustedPath = new ArrayList<>();

                for (Point p : task.getPaths()) {
                    Point newPoint = new Point(p.getX(), p.getY(), p.getTime() + startTime);
                    adjustedPath.add(newPoint);
                }
                task.setPaths(adjustedPath);

                // 记录这辆车的最后使用时间：最后一个点的时间 + 1
                int lastTime = adjustedPath.get(adjustedPath.size() - 1).getTime() + 1;
                carLastEndTime.put(carNumber, lastTime);
            }
        }



            /**
             * 算法得到了，就可以根据算法的结果开始更新数据库了
             *       1、  更新订单状态为：已完成
             *       2、  更新货架-货物关系表
             *       3、  更新库存表
             *
             */
//            1
            String orderNumber1 = inboundOrder.getOrderNumber();
            boolean b = inboundOrderService.updateStatus(orderNumber1, Constants.STATUS_ORDER_FINISHED);
            if (!b){
                return ResponseEntity.internalServerError().body(AjaxResult.error(500,"状态更新失败"));
            }
        for (CarStorageToShelf carStorageToShelf: carTasks){
            String shelfCode = carStorageToShelf.getShelfCode();
            List<SalesOrderDetailAddDTO> items = carStorageToShelf.getProducts();
            for(SalesOrderDetailAddDTO item:items){
                //检查该货架上是否有SKU
                ShelfInventory existing = shelfInventoryService.findByShelfCodeAndSku(shelfCode, item.getSku());
                if (existing!=null){
                    //有-》数量增加
                    existing.setQuantity(existing.getQuantity()+ item.getQuantity());
                    shelfInventoryService.updateById(existing);
                }else {
                    //没有-》插入新值
                    ShelfInventory shelfInventory = new ShelfInventory(null, shelfCode, item.getSku(),
                            Long.valueOf(item.getQuantity()));
                    shelfInventoryService.save(shelfInventory);
                }
            }
        }
            //2
//            for(Map<String, List<SalesOrderDetailAddDTO>> shelfMap : result){
//                for (Map.Entry<String, List<SalesOrderDetailAddDTO>> entry : shelfMap.entrySet()){
//                    String shelfCode = entry.getKey();
//                    List<SalesOrderDetailAddDTO> items = entry.getValue();
//                    for(SalesOrderDetailAddDTO item:items){
//                        //检查该货架上是否有SKU
//                        ShelfInventory existing = shelfInventoryService.findByShelfCodeAndSku(shelfCode, item.getSku());
//                        if (existing!=null){
//                            //有-》数量增加
//                            existing.setQuantity(existing.getQuantity()+ item.getQuantity());
//                            shelfInventoryService.updateById(existing);
//                        }else {
//                            //没有-》插入新值
//                            ShelfInventory shelfInventory = new ShelfInventory(null, shelfCode, item.getSku(),
//                                    Long.valueOf(item.getQuantity()));
//                            shelfInventoryService.save(shelfInventory);
//                        }
//                    }
//                }
//            }
            //3更新库存表
            List<InboundOrderDetail> details = inboundOrderDetailService.listByOrderNumber(orderNumber1);
            for (InboundOrderDetail detail:details){
                Long quantity = detail.getQuantity();
                String sku = detail.getSku();
                Inventory inventory = inventoryService.getBySku(sku);
                if (inventory==null){
                    inventoryService.addOneInventory(new Inventory(null,sku,quantity));
                }else {
                    boolean b1 = inventoryService.increaseInventory(sku, quantity);
                    if (!b1) return ResponseEntity.internalServerError().body(AjaxResult.error(500,"库存表更新错误"));
                }
            }
        UserVo user = UserHolder.getUser();
        remindService.saveRemind(new Remind(null,Constants.REMIND_SUCCESS,"成功入库","于"+LocalDateTime.now()+",时刻,"+
                user.getUserType()+user.getUsername()+"成功入库了编号为"+inboundOrder.getOrderNumber()+"的入库单",
                LocalDateTime.now(),"1"));
            return ResponseEntity.ok(AjaxResult.success(carTasks));

        }







    /**
     * 入库路径
     * @param id
     * @return
     */
    @GetMapping("/in/{id}")
    public ResponseEntity<AjaxResult>  findShortestInPath(
            @PathVariable Long id
    ){

        System.out.println(id);
        InboundOrder inboundOrder = inboundOrderService.getById(id);
        if (inboundOrder==null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"没有该订单"));
        }
        String orderNumber = inboundOrder.getOrderNumber();
        /**
         *确保了订单正常，下面找最短路径的货架
         */
//            List<ShelfInfoVo> allShelves = shelfInventoryService.getAllShelves();
        //////////////////////////////////////////////////////////////////
        //这里简单去个重，后面要改，不然有ShelfCode重复的情况，导致后面出错
        List<ShelfInfoVo> allShelves = shelfInventoryService.getAllShelves().stream()
                .collect(Collectors.toMap(
                        ShelfInfoVo::getShelfCode,
                        Function.identity(),
                        (existing, replacement) -> existing // 遇重复时保留第一个
                ))
                .values()
                .stream()
                .collect(Collectors.toList());

        //////////////////////////////////////////////////////////////////
        List<ShelfInfoVo> sortedShelves = allShelves.stream()
                .sorted(Comparator.comparingInt(shelf -> {
                    int x = (int) Double.parseDouble(shelf.getShelfX());
                    int y = (int) Double.parseDouble(shelf.getShelfY());
                    int z = (int) Double.parseDouble(shelf.getShelfZ()); // 这里改为 Double
                    return Math.abs(x - beginX)*Constants.MOVE_DIFFER_X + Math.abs(y - beginY)*Constants.MOVE_DIFFER_Y
                            + Math.abs(z - beginZ+1) * Constants.MOVE_DIFFER_Z;
                }))
                .collect(Collectors.toList());
        System.out.println(sortedShelves);

        System.out.println("最短路径的货架List（从小到大）："+sortedShelves);
        /**
         * 获得了最短路径的货架的List，可以按照这个分配货物，注意分配的货物的重量+该货架已有的重量不能超过货架的maxWeight
         */
        List<InboundOrderDetail> inboundOrderDetails = inboundOrderDetailService.listByOrderNumber(orderNumber);
        // 返回结果：每个货架对应的商品列表
        List<Map<String, List<SalesOrderDetailAddDTO>>> result = new ArrayList<>();
        // 每个货架分配的商品记录：shelfCode → List<SalesOrderDetailAddDTO>
        Map<String, List<SalesOrderDetailAddDTO>> shelfAllocation = new LinkedHashMap<>();
        // 每个货架当前已用重量缓存
        Map<String, Double> shelfCurrentWeight = new HashMap<>();
        // 初始化每个货架的已有重量
        for (ShelfInfoVo shelf : sortedShelves) {
            String shelfCode = shelf.getShelfCode();
            List<ShelfInventory> shelfInventories = shelfInventoryService.findByShelfCode(shelfCode);

            double existingWeight = 0;
            if (!shelfInventories.isEmpty()){
                for (ShelfInventory shelfInventory:shelfInventories){
                    String sku = shelfInventory.getSku();
                    Long quantity = shelfInventory.getQuantity();
                    Product bySku = productService.findBySku(sku);
                    if (bySku==null){
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"货物不存在"));
                    }
                    existingWeight += (double) (bySku.getWeight()*quantity);
                }
            }
            shelfCurrentWeight.put(shelfCode, existingWeight);
            shelfAllocation.put(shelfCode, new ArrayList<>());
        }

        // 遍历订单中每个 SKU 项
        for (InboundOrderDetail detail : inboundOrderDetails) {
            String sku = detail.getSku();
            //订单里该物品的数量
            long quantityToAllocate = detail.getQuantity();
            Product bySku = productService.findBySku(sku);
            Double singleWeight = bySku.getWeight();
            String productName = bySku.getName();
            System.out.println("物品："+productName+"  ;重量："+singleWeight);
            for (ShelfInfoVo shelf : sortedShelves) {
                if (quantityToAllocate <= 0) break;

                String shelfCode = shelf.getShelfCode();
                long maxWeight = shelf.getMaxWeight();
                double currentWeight = shelfCurrentWeight.getOrDefault(shelfCode, 0.0);//获取货架当前的重量
                double availableWeight = maxWeight - currentWeight;//该货架的剩余重量

                long maxQuantityCanFit = singleWeight == 0.0 ? 0 : (long)Math.floor(availableWeight / singleWeight); //可放的数量，向下取整
                if (maxQuantityCanFit <= 0) continue;

                long quantityAllocated = Math.min(quantityToAllocate, maxQuantityCanFit);  //订单里的货物数量与可放的物品数量取最小值，得到该货架放该物品的数量
                double weightAllocated = quantityAllocated * singleWeight;//得到该货架放该物品的重量

                // 构造分配的商品对象
                SalesOrderDetailAddDTO item = new SalesOrderDetailAddDTO();
                item.setName(productName);
                item.setSku(sku);
                item.setQuantity((int) quantityAllocated);

                shelfAllocation.get(shelfCode).add(item);
                shelfCurrentWeight.put(shelfCode, currentWeight + weightAllocated);//每次更新货架层的存的重量
                quantityToAllocate -= quantityAllocated;//订单需要的数量=订单需要的数量-放到货架的数量，  可能 >0：循环继续  =0：该物品放完了；退出
            }

            if (quantityToAllocate > 0) {
                //这里quantityToAllocate>0 仅有一种情况，即货架放完了但订单里物品还有剩余
//                    throw new RuntimeException("SKU " + sku + " 剩余未分配数量：" + quantityToAllocate);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"货架不足"));
            }
        }
        Set<Map.Entry<String, List<SalesOrderDetailAddDTO>>> entries = shelfAllocation.entrySet();
        // 组装结果为 List<Map<String, List<DTO>>>
        for (Map.Entry<String, List<SalesOrderDetailAddDTO>> entry : shelfAllocation.entrySet()) {
            Map<String, List<SalesOrderDetailAddDTO>> shelfMap = new HashMap<>();
            shelfMap.put(entry.getKey(), entry.getValue());
            result.add(shelfMap);
        }


        System.out.println("============================");
        System.out.println(result);

        /**
         * 我们得到了订单里的物品对应的货架后，后面把每个货架层的订单物品分配给小车，
         *          注意：1、小车的最大承重可能会小于需要运到该货架层物品的重量，这时候就要多一个小车装配了
         *               2、可能会出现小车复用的情况
         *               3、小车从起点出发运货到货架，再从货架返回终点（48，34）
         *               返回  List<CarStorageToShelf>
         *                   其中：public class CarStorageToShelf{
         *                          car_number： string   //车子编号
         *                          max_weight:  double   //最大载重
         *                          sun_weight:  double   //当前承重
         *                          startX:  int
         *                          startY:  int
         *                          middleX:  int        //货架交互点x
         *                          middleY   int        //货架交互点y
         *                          middleZ   int        //货架层数z
         *                          endX      int        //终点x
         *                          endZ      int        //终点y
         *                          shelf_code 货架层编号
         *                          products:  List<SalesOrderDetailAddDTO>   //车子放的物品list
         *                   }
         *
         */
        List<AgvCar> agvCars = agvCarService.findAll();
        if (agvCars.isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"没有小车！！"));
        }
        String position="";
        //将小车按照maxWeight从大到小排序，载重大的先装货
        agvCars.sort(Comparator.comparingDouble(AgvCar::getMaxWeight).reversed());
        List<CarStorageToShelf> carTasks = new ArrayList<>();
        //小车循环使用索引
        int carIndex=0;

        // 快速获取 shelf 坐标信息
        Map<String, ShelfInfoVo> shelfInfoMap = sortedShelves.stream()
                .collect(Collectors.toMap(ShelfInfoVo::getShelfCode, Function.identity()));



        for (Map<String, List<SalesOrderDetailAddDTO>> shelfEntry : result){
            for (Map.Entry<String, List<SalesOrderDetailAddDTO>> entry : shelfEntry.entrySet()){
                //该货架层的编码
                String shelfCode = entry.getKey();
                //该货架层存的东西和数量
                List<SalesOrderDetailAddDTO> items = entry.getValue();
                //获得该货架层的位置等详细信息
                ShelfInfoVo shelf = shelfInfoMap.get(shelfCode);
                if (shelf==null){
                    System.out.println("shelfCode:"+shelfCode+"没有该货架");
                    continue;
                }
                int middleX = (int)Double.parseDouble(shelf.getShelfX());
                if (middleX==14||middleX==18||middleX==22||middleX==26||middleX==30||middleX==34||
                        middleX==38||middleX==42||middleX==46){
                    middleX=middleX-1;
                    position="左";
                }else {
                    middleX=middleX+1;
                    position="右";
                }
                int middleY = (int)Double.parseDouble(shelf.getShelfY());
                int middleZ = (int)Double.parseDouble(shelf.getShelfZ());

                // 拆分当前货架的商品清单（可能多个车来装）
                List<SalesOrderDetailAddDTO> remainingItems = new ArrayList<>(items);
                while(!remainingItems.isEmpty()){
                    //取一个可用的小车
                    AgvCar car = agvCars.get(carIndex);
                    carIndex=(carIndex+1)% agvCars.size();

                    //该小车最大载重
                    double maxWeightKg = car.getMaxWeight();
                    double sumWeight = 0.0;
                    List<SalesOrderDetailAddDTO> carLoad = new ArrayList<>();

                    Iterator<SalesOrderDetailAddDTO> iter = remainingItems.iterator();
                    while (iter.hasNext()){
                        SalesOrderDetailAddDTO dto = iter.next();
                        Product product = productService.findBySku(dto.getSku());
                        Double weight = product.getWeight();//单价重量
                        Integer quantity = dto.getQuantity();//数量
                        Double totalWeight =weight*quantity;
                        if (sumWeight+totalWeight<=maxWeightKg){
                            //可以全装
                            carLoad.add(dto);
                            sumWeight+=totalWeight;
                            iter.remove();//加上后删掉该条货物记录
                        }else {
                            //不能全装
                            int canLoadQty = (int) Math.floor((maxWeightKg - sumWeight) / weight);
                            if (canLoadQty>0){
                                //可以装部分
                                SalesOrderDetailAddDTO partial = new SalesOrderDetailAddDTO();
                                partial.setName(dto.getName());
                                partial.setSku(dto.getSku());
                                partial.setQuantity(canLoadQty);
                                carLoad.add(partial);//小车装这些
                                sumWeight+=canLoadQty*weight;
                                dto.setQuantity(quantity - canLoadQty);
                            }
                            //canLoadQty<=0说明该小车一点都装不了了，退出while
                            break;
                        }
                    }

                    // 5. 构建运输任务
                    AgvCar agvCar = agvCarService.findByCarNumber(car.getCarNumber());
                    CarStorageToShelf task = new CarStorageToShelf();
                    task.setCarNumber(car.getCarNumber());
                    task.setMaxWeight(maxWeightKg);
                    task.setSumWeight(sumWeight);
                    task.setStartX(agvCar.getLocationX().intValue());//改成小车停放点X
                    task.setStartY(agvCar.getLocationY().intValue());//改成小车停放点Y
                    task.setMiddleX(middleX);
                    task.setMiddleY(middleY);
                    task.setMiddleZ(middleZ);
                    task.setEndX(agvCar.getLocationX().intValue());//改成小车停放点X
                    task.setEndY(agvCar.getLocationY().intValue());//改成小车停放点Y
                    task.setShelfCode(shelfCode);
                    task.setProducts(carLoad);
                    task.setPosition(position);

                    carTasks.add(task);
                }

            }
        }
        System.out.println("小车装的货物以及去的位置："+carTasks);
        /**
         * 知道了小车装的哪些货及其数量，以及小车的出发点、货架交互点，以及终点
         * 现在我们先生成地图，再根据地图找路径
         */

        /**
         * 地图生成完毕，可以开始最短路径查找了
         * 想要输出  AllCarPath{
         *             List<CarStorageToShelf> details;
         *          }
         */
        for (CarStorageToShelf task:carTasks){
            int startX = task.getStartX();
            int startY = task.getStartY();
            int midX = task.getMiddleX();
            int midY = task.getMiddleY();
            int middleZ = task.getMiddleZ(); // 当前货架的层数
            int endX = task.getEndX();
            int endY = task.getEndY();
            if (task.getProducts().isEmpty()){
                continue;
            }

            //////////////////////////////////////////
            //从停车点--》入库区域
            int[][] map1=BfsFindPath.InMapToProduct(startX,startY,9,25);
            List<Point> path1_1 =BfsFindPath.bfsFindPath(map1,startX,startY,5,33,0);
            //开始装货//2s
            int LoadingTime=Constants.INTERACTION;
            int time = path1_1.get(path1_1.size() - 1).getTime();
            for (int i=1;i<=LoadingTime;i++){
                path1_1.add(new Point(5,33,time+i));
            }
            int resultTime=time+LoadingTime;

            int[][] map2=BfsFindPath.InMapToProduct(startX,startY,0,0);
            List<Point> path1_2 =BfsFindPath.bfsFindPath(map2,5,33,11,24,resultTime);
            // 5. 合并路径
            if (!path1_2.isEmpty() && !path1_1.isEmpty()) {
                if (path1_1.get(path1_1.size() - 1).getX() == path1_2.get(0).getX() &&
                        path1_1.get(path1_1.size() - 1).getY() == path1_2.get(0).getY()) {
                    path1_2.remove(0);
                }
            }
            int resultTime1=path1_2.get(path1_2.size()-1).getTime();
            //////////////////////////////////////////
            // 1. 生成地图
            int[][] map = BfsFindPath.InMap();
            // 2. 起点 → 中间点
//                List<Point> path1 = BfsFindPath.bfsFindPath(map, startX, startY, midX, midY, 0);
            List<Point> path1 = BfsFindPath.bfsFindPath(map, 11, 24, midX, midY, resultTime1);
            /////////////////////////////////
            if (!path1_2.isEmpty() && !path1.isEmpty()) {
                if (path1_2.get(path1_2.size() - 1).getX() == path1.get(0).getX() &&
                        path1_2.get(path1_2.size() - 1).getY() == path1.get(0).getY()) {
                    path1.remove(0);
                }
            }
            ////////////////////////////////
            // 3. 插入Z轴等待时间
            int zTimeCost = middleZ * 2;
            int lastTime = path1.get(path1.size() - 1).getTime();
            for (int i = 1; i <= zTimeCost; i++) {
                path1.add(new Point(midX, midY, lastTime + i)); // 停在原地增加时间
            }
            int resumeTime = lastTime + zTimeCost;
            // 4. 中间点 → 终点
            List<Point> path2 = BfsFindPath.bfsFindPath(map, midX, midY, 11,12, resumeTime);
            // 5. 合并路径
            if (!path2.isEmpty() && !path1.isEmpty()) {
                if (path1.get(path1.size() - 1).getX() == path2.get(0).getX() &&
                        path1.get(path1.size() - 1).getY() == path2.get(0).getY()) {
                    path2.remove(0);
                }
            }

            //////////////////////////
            //回停机位
            int resultTime2=path2.get(path2.size()-1).getTime();
            List<Point> path2_1 = BfsFindPath.bfsFindPath(map2, 11, 12, endX,endY, resultTime2);
            if (!path2_1.isEmpty() && !path2.isEmpty()) {
                if (path2.get(path2.size() - 1).getX() == path2_1.get(0).getX() &&
                        path2.get(path2.size() - 1).getY() == path2_1.get(0).getY()) {
                    path2_1.remove(0);
                }
            }


            ////////////////////////
            List<Point> fullPath = new ArrayList<>(path1_1);
            fullPath.addAll(path1_2);
            fullPath.addAll(path1);
            fullPath.addAll(path2);
            fullPath.addAll(path2_1);
//                List<Point> fullPath = new ArrayList<>(path1);
//                fullPath.addAll(path2);
            // 6. 注入
            task.setPaths(fullPath);
        }

//            carTasks
        carTasks = carTasks.stream()
                .filter(task -> task.getPaths() != null && !task.getPaths().isEmpty())
                .collect(Collectors.toList());

        int globalStartTime = 0;
        Map<String, Integer> carLastEndTime = new HashMap<>();
        Map<Integer, Set<String>> occupied = new HashMap<>(); // 时间戳 -> 占用位置


        //是否处理小车重合的冲突
        String dealing="是";
        List<FactoryConfig> list = factoryConfigService.list();
        if (list!=null){
            FactoryConfig first = list.getFirst();
            String pathStrategy = first.getPathStrategy();
            if (!pathStrategy.equals("是")&&!pathStrategy.equals("否")){
                dealing="是";
            }else {
                if (pathStrategy.equals("是")){
                    dealing="否";
                }else {
                    dealing="是";
                }
            }
        }
        if (dealing.equals("是")){
            //处理冲突
            for (CarStorageToShelf task : carTasks) {
                String carNumber = task.getCarNumber();
                List<Point> originalPath = task.getPaths();

                // 初始化出发时间
                int startTime = carLastEndTime.getOrDefault(carNumber, globalStartTime);
                List<Point> adjustedPath;

                // 处理冲突（延迟出发）
                while (true) {
                    boolean conflict = false;
                    adjustedPath = new ArrayList<>();

                    for (Point p : originalPath) {
                        int t = p.getTime() + startTime;
                        String pos = p.getX() + "," + p.getY();
                        Set<String> occupiedAtT = occupied.getOrDefault(t, new HashSet<>());

                        if (occupiedAtT.contains(pos)) {
                            conflict = true;
                            break;
                        }
                        adjustedPath.add(new Point(p.getX(), p.getY(), t));
                    }

                    if (!conflict) break;
                    startTime++; // 延后出发时间，重试
                }

                // 更新 paths
                task.setPaths(adjustedPath);
                task.setDispatchTime(startTime);

                // 占位记录
                for (Point p : adjustedPath) {
                    int t = p.getTime();
                    String pos = p.getX() + "," + p.getY();
                    occupied.computeIfAbsent(t, k -> new HashSet<>()).add(pos);
                }

                // 更新该车最后时间
                int endTime = adjustedPath.get(adjustedPath.size() - 1).getTime() + 1;
                carLastEndTime.put(carNumber, endTime);
            }


        }else {
            //不处理冲突
            for (CarStorageToShelf task : carTasks) {
                String carNumber = task.getCarNumber();
                // 跳过无路径的小车任务
                if (task.getPaths() == null || task.getPaths().isEmpty()) {
                    continue;
                }
                // 获取该车上一次结束时间（若第一次，则为当前全局时间）
                int startTime = carLastEndTime.getOrDefault(carNumber, globalStartTime);
                // 设置 dispatchTime
                task.setDispatchTime(startTime);

                // 调整路径中每个点的时间（加上 dispatchTime）
                List<Point> adjustedPath = new ArrayList<>();

                for (Point p : task.getPaths()) {
                    Point newPoint = new Point(p.getX(), p.getY(), p.getTime() + startTime);
                    adjustedPath.add(newPoint);
                }
                task.setPaths(adjustedPath);

                // 记录这辆车的最后使用时间：最后一个点的时间 + 1
                int lastTime = adjustedPath.get(adjustedPath.size() - 1).getTime() + 1;
                carLastEndTime.put(carNumber, lastTime);
            }
        }

        Map<String ,CarStorageToShelf> mergedMap=new LinkedHashMap<>();

        for (CarStorageToShelf task:carTasks){
            String carNumber = task.getCarNumber();
            if (!mergedMap.containsKey(carNumber)){
                mergedMap.put(carNumber,task);
            }else {
                CarStorageToShelf existing = mergedMap.get(carNumber);
                List<Point> mergedPaths = new ArrayList<>(existing.getPaths());
                List<Point> newPaths = task.getPaths();

                if (!mergedPaths.isEmpty() && !newPaths.isEmpty()) {
                    Point last = mergedPaths.get(mergedPaths.size() - 1);
                    Point first = newPaths.get(0);
                    if (last.getX() == first.getX() && last.getY() == first.getY()&&last.getTime()==first.getTime()) {
                        newPaths.remove(0);
                    }
                }
                mergedPaths.addAll(newPaths);
                existing.setPaths(mergedPaths);

                List<SalesOrderDetailAddDTO> detailAddDTOS = new ArrayList<>(existing.getProducts());
                detailAddDTOS.addAll(task.getProducts());
                existing.setProducts(detailAddDTOS);

                existing.setSumWeight(existing.getSumWeight()+task.getSumWeight());
                existing.setEndX(task.getEndX());
                existing.setEndY(task.getEndY());
            }
        }

        List<CarStorageToShelf> mergedCarTasks = new ArrayList<>(mergedMap.values());



        return ResponseEntity.ok(AjaxResult.success(mergedCarTasks));

    }

}
