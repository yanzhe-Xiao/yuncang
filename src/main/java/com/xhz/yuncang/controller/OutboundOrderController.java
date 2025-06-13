package com.xhz.yuncang.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xhz.yuncang.dto.OutDTO;
import com.xhz.yuncang.dto.SalesOrderDetailAddDTO;
import com.xhz.yuncang.entity.*;
import com.xhz.yuncang.service.*;
import com.xhz.yuncang.utils.AjaxResult;
import com.xhz.yuncang.utils.BfsFindPath;
import com.xhz.yuncang.utils.Constants;
import com.xhz.yuncang.vo.PageVo;
import com.xhz.yuncang.vo.OutboundOrderInfoVo;
import com.xhz.yuncang.vo.ProductSimpleVo;
import com.xhz.yuncang.vo.path.*;
import com.xhz.yuncang.vo.shelf.ShelfInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
/**
 * 出库订单管理控制器
 *
 * <p>负责处理出库订单的全生命周期管理，主要功能包括：
 * <ul>
 *   <li>出库订单的分页查询</li>
 *   <li>执行出库操作</li>
 *   <li>订单状态管理</li>
 * </ul>
 *
 * <p>业务规则：
 * <ul>
 *   <li>只有状态为"未开始"的订单可出库</li>
 *   <li>出库操作需验证订单完整性</li>
 *   <li>分页查询需验证页码有效性</li>
 * </ul>
 * @author skc
 * @see OutboundOrderService 出库订单服务
 * @see SalesOrderService 销售订单服务
 * @see SalesOrderDetailService 销售订单明细服务
 */
@RestController
@PreAuthorize("hasRole('操作员') or hasRole('管理员')")
public class OutboundOrderController {
    /**
     * 出库订单服务实例
     */
    @Autowired
    private OutboundOrderService outboundOrderService;

    @Autowired
    private FactoryConfigService factoryConfigService;
    /**
     * 销售订单服务实例
     */
    @Autowired
    private SalesOrderService salesOrderService;
    /**
     * 销售订单明细服务实例
     */
    @Autowired
    private SalesOrderDetailService salesOrderDetailService;

    @Autowired
    private ShelfInventoryService shelfInventoryService;

    @Autowired
    private StorageShelfService storageShelfService;

    @Autowired
    private ProductService productService;

    @Autowired
    private AgvCarService agvCarService;

    @Autowired
    private InventoryService inventoryService;

    /**
     * 分页查询出库订单
     *
     * <p>获取分页的出库订单列表，包含以下处理逻辑：
     * <ol>
     *   <li>验证页码有效性（必须大于0）</li>
     *   <li>执行分页查询</li>
     *   <li>构建分页导航信息</li>
     *   <li>转换数据为VO对象</li>
     * </ol>
     *
     * @param pageNo 当前页码（从1开始）
     * @return 响应实体，包含：
     *         <ul>
     *           <li>成功：状态码200+分页数据(PageVo&lt;OutboundOrderInfoVo&gt;)</li>
     *           <li>失败：状态码400+错误信息</li>
     *         </ul>
     * @see PageVo 分页数据包装类
     * @see OutboundOrderInfoVo 出库订单信息VO
     */
    @GetMapping("/outboundorder/{pageNo}")
    public ResponseEntity<AjaxResult> showOutboundOrdersByPage(@PathVariable int pageNo) {
        if (pageNo <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error("页数小于 1"));
        }
        System.out.println("请求的页数 pageNo: " + pageNo);
        Page<OutboundOrder> page = outboundOrderService.page(new Page<>(pageNo, Constants.PageSize)); // 一页显示 Constants.PageSize 条数据
        System.out.println("获得的数据 page: " + page);
        System.out.println("\n总页数 getPages: " + page.getPages());
        System.out.println("\n当前页数 getCurrent: " + page.getCurrent());
        System.out.println("\n出库订单总数量 getTotal: " + page.getTotal());
        if (page.getPages()==0){
            return ResponseEntity.ok().body(AjaxResult.success("当前出库单为空",null));
        }
        if (pageNo > page.getPages()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error("页数大于最大页数"));
        }

        // 当前页数
        int nowPage = (int) page.getCurrent();
        // 若当前页为第一页，上一页设为 -1
        int beforePage = nowPage == 1 ? -1 : nowPage - 1;
        // 若当前页为最后一页，下一页设为 -1
        int nextPage = nowPage == (int) page.getPages() ? -1 : nowPage + 1;

        // 将 List<OutboundOrder> 转换为 List<OutboundOrderInfoVo>
        List<OutboundOrder> outboundOrders = page.getRecords();
        List<OutboundOrderInfoVo> outboundOrderInfoVoList = outboundOrders.stream().map(order -> new OutboundOrderInfoVo(
                order.getOrderNumber(),
                order.getPlannedDate(),
                order.getUserId(),
                order.getStatus()
        )).toList();

        PageVo<OutboundOrderInfoVo> outboundOrderPageVo = new PageVo<>(
                nowPage,
                beforePage,
                nextPage,
                (int) page.getPages(),
                outboundOrderInfoVoList
        );
        System.out.println("outboundOrderListPageInfo: " + outboundOrderPageVo);

        return ResponseEntity.ok().body(AjaxResult.success(outboundOrderPageVo));
    }

    /**
     * 执行出库操作
     *
     * <p>处理出库流程，需满足以下条件：
     * <ul>
     *   <li>订单状态必须为"未开始"</li>
     *   <li>订单编号必须有效</li>
     *   <li>关联数据必须完整</li>
     * </ul>
     *
     * <p>出库流程说明：
     * <ol>
     *   <li>调用算法获取出库路径</li>
     *   <li>更新库存数量</li>
     *   <li>更新货架货物关系</li>
     *   <li>修改订单状态为"已完成"</li>
     * </ol>
     *
     * @param id 销售订单ID（路径参数）
     * @param outDTO 出库数据传输对象，包含：
     *               <ul>
     *                 <li>orderNumber - 订单编号</li>
     *                 <li>status - 订单状态</li>
     *               </ul>
     * @return 响应实体，包含：
     *         <ul>
     *           <li>成功：状态码200+空数据</li>
     *           <li>失败：状态码400+错误原因</li>
     *         </ul>
     */



//    @PreAuthorize("hasRole('客户') or has")
    @PutMapping("/out/{id}")
    public ResponseEntity<AjaxResult> outBound(@PathVariable Long id, @RequestBody OutDTO outDTO){
        System.out.println(outDTO+"  id:"+id);
        if (!outDTO.getStatus().equals(Constants.STATUS_ORDER_TOSTART)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"该出库单已出库或正在出库"));
        }
        if (outDTO.getOrderNumber() == null || outDTO.getOrderNumber().isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"出库失败"));
        }
        SalesOrder order = salesOrderService.findByOrderNo(outDTO.getOrderNumber());
        OutboundOrder outboundOrder = outboundOrderService.findByOrderNumber(outDTO.getOrderNumber());
        List<SalesOrderDetail> details = salesOrderDetailService.getSalesOrderDetailsByOrderNumber(outDTO.getOrderNumber());
        if (order==null||outboundOrder==null|| details.isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"数据库没有该数据"));
        }

        /**
         * 此时可以开始出库了！！！！！
         *
         * 1、调用【算法】获得 需要出库的货物的位置、数量，以及对应的路径
         * 2、  处理 ：  库存表  减去对应的货物数量【在创建订单的时候就做了判断，故库存一定够】
         * 3、  处理：   货架-货物关系表  更新， 根据位置，sku，数量更新
         * 4、  处理：   更新 出库单的状态为“已完成” 【这里订单、订单详细都不变】
         */
        int beginX = 11, beginY = 14, beginZ = 1;



        double alpha; // 路径权重
        double beta;  // 数量权重
        String choose=Constants.OUT_PREFER;

        List<FactoryConfig> list = factoryConfigService.list();
        if (list!=null&&!list.isEmpty()){
            FactoryConfig first = list.getFirst();
            String pathStrategy = first.getPathStrategy();
            if (pathStrategy!=null){
                choose=pathStrategy;
            }
        }
        switch (choose){
            case "system-judged":
                // 系统智能判断
                int skuCount = details.size();
                long avgQty = details.stream()
                        .mapToLong(SalesOrderDetail::getQuantity)
                        .sum() / Math.max(1, skuCount);

                if (skuCount <= 3 && avgQty > 20) {
                    alpha = 0.8;
                    beta = 3000.0;
                } else {
                    alpha = 2.0;
                    beta = 500.0;
                }
                break;
            case "short-path":
                alpha = 2.0;
                beta = 500.0;
                break;
            case "more-stock":
                alpha = 0.5;
                beta = 3000.0;
                break;
            case "balanced":
                alpha = 1.0;
                beta = 1000.0;
                break;
            default:
                alpha = 1.0;
                beta = 1000.0;
                break;
        }

        String orderNumber = outDTO.getOrderNumber();
        List<OutboundRecord> outboundRecords = new ArrayList<>();
        for (SalesOrderDetail salesOrderDetail:details){
            String sku = salesOrderDetail.getSku();
            Product product = productService.findBySku(sku);
            Long quantity = salesOrderDetail.getQuantity();//需要的数量
            List<ShelfInventory> inventoryList = shelfInventoryService.findBySku(sku);
            if (inventoryList==null||inventoryList.isEmpty()){
                return ResponseEntity.badRequest().body(AjaxResult.error(400,"找不到货物"));
            }


            /**
             * 1、先判断是否有“一次能取完”的 → 最快方案；
             * 2、如果没有，再按 距离/数量比值 排序，做最佳分配。
             *
             */
            // 排序：路径最近优先  VS    一次能取完优先
            //score = 路径距离 / 可出库数量
            //1、
            // 优先找一个能一次出完的货架（路径最短优先）
            final long finalNeededQty = quantity;
            Optional<ShelfInventory> oneShot = inventoryList.stream()
                    .filter(inv -> inv.getQuantity() != null && inv.getQuantity() >= finalNeededQty)
                    .min(Comparator.comparingInt(inv -> {
                        String shelfCode = inv.getShelfCode();
                        StorageShelf shelf = storageShelfService.findByShelfCode(shelfCode);
                        int locationX = shelf.getLocationX().intValue();
                        int locationY = shelf.getLocationY().intValue();
                        int locationZ = shelf.getLocationZ().intValue();
                        return Math.abs(locationX - beginX)*Constants.MOVE_DIFFER_X + Math.abs(locationY - beginY)*
                                Constants.MOVE_DIFFER_Y + Math.abs(locationZ - beginZ + 1) * Constants.MOVE_DIFFER_Z;
                    }));

            if (oneShot.isPresent()) {
                ShelfInventory inv = oneShot.get();
                outboundRecords.add(new OutboundRecord(sku,product.getName(),inv.getShelfCode(), quantity));
                inv.setQuantity(inv.getQuantity() - quantity);
                continue;
            }

            //不能一次出完，就按 距离/数量 综合打分排序
            List<ShelfInventory> sortedList = inventoryList.stream()
                    .filter(inv -> inv.getQuantity() != null && inv.getQuantity() > 0)
                    .sorted(Comparator.comparingDouble(inv -> {
                        String shelfCode = inv.getShelfCode();
                        StorageShelf shelf = storageShelfService.findByShelfCode(shelfCode);
                        int locationX = shelf.getLocationX().intValue();
                        int locationY = shelf.getLocationY().intValue();
                        int locationZ = shelf.getLocationZ().intValue();
                        int pathScore= Math.abs(locationX - beginX)*Constants.MOVE_DIFFER_X + Math.abs(locationY - beginY)*
                                Constants.MOVE_DIFFER_Y + Math.abs(locationZ - beginZ + 1) * Constants.MOVE_DIFFER_Z;
                        double distanceScore = pathScore * alpha;
                        double quantityScore = beta / (double) inv.getQuantity(); // 越多越好
                        return distanceScore + quantityScore;//越小越好
                    }))
                    .toList();


            // 从排序后的货架中分配库存
            for (ShelfInventory inv:sortedList){
                if (quantity<=0) break;

                Long available = inv.getQuantity();//货架有的数量
                if (available==null||available<=0) continue;

                Long toPick = Math.min(available, quantity);

                outboundRecords.add(new OutboundRecord(sku,product.getName(), inv.getShelfCode(),toPick));
                inv.setQuantity(available-toPick);
                quantity-=toPick;

            }

            if (quantity>0){
                return ResponseEntity.internalServerError().body(AjaxResult.error(500,"库存不足！！"));
            }

        }
        System.out.println(outboundRecords.size());
//        outboundRecords.forEach(System.out::println);

        /**
         * 以获得订单里的物品要从哪些货架层上拿，以及拿多少的问题
         * 后面我们合并相同货架层的物品，得到我们需要到哪些货架层拿哪几类物品以及对应的数量
         */
        Map<String, Map<String, SalesOrderDetailAddDTO>> tempMap = new HashMap<>();

        for (OutboundRecord record : outboundRecords) {
            String shelfCode = record.getShelfCode();
            String sku = record.getSku();
            String productName = record.getProductName();
            Long quantity = record.getQuantity();

            tempMap
                    .computeIfAbsent(shelfCode, k -> new HashMap<>())
                    .compute(sku, (k, existing) -> {
                        if (existing == null) {
                            return new SalesOrderDetailAddDTO(productName,sku, quantity.intValue());
                        } else {
                            existing.setQuantity(existing.getQuantity() + quantity.intValue());
                            return existing;
                        }
                    });
        }
        // 转成 Map<String, List<PickTaskItem>>
        Map<String, List<SalesOrderDetailAddDTO>> result = new HashMap<>();
        for (Map.Entry<String, Map<String, SalesOrderDetailAddDTO>> entry : tempMap.entrySet()) {
            result.put(entry.getKey(), new ArrayList<>(entry.getValue().values()));
        }
        System.out.println(result.size());
        List<ShelfPickTask> tasks = new ArrayList<>();

        for (Map.Entry<String, List<SalesOrderDetailAddDTO>> entry : result.entrySet()) {
            String shelfCode = entry.getKey();
            List<SalesOrderDetailAddDTO> items = entry.getValue();

            double totalWeight = items.stream()
                    .mapToDouble(item -> {
                        String sku = item.getSku();
                        Product product = productService.findBySku(sku);
                        return product.getWeight() * item.getQuantity();
                    })
                    .sum();

            ShelfPickTask task = new ShelfPickTask();
            task.setShelfCode(shelfCode);
            task.setLocationX(storageShelfService.findByShelfCode(shelfCode).getLocationX());
            task.setLocationY(storageShelfService.findByShelfCode(shelfCode).getLocationY());
            task.setLocationZ(storageShelfService.findByShelfCode(shelfCode).getLocationZ());
            task.setItems(items);
            task.setTotalWeight(totalWeight);
            tasks.add(task);
        }
        // 按总重量降序排序
        tasks.sort(Comparator.comparingDouble(ShelfPickTask::getTotalWeight).reversed());
        System.out.println(tasks.size());
        /**
         * 1、把这些货架层分配给小车装载
         *        1、小车最大载重若比该货架层的小，尽可能的装
         *        2、小车最大载重若大于该货架层1，则可全装到小车上，并查找距离该货架层1最近的货架层2，如果货架层2可以全装到该小车上则该小车去2那里装货，否则回出库点
         *        3、可能会出现小车的复用的问题
         *        返回：List<CarOut>
         *
         */
        List<AgvCar> agvCars = agvCarService.findAll();
        if (agvCars.isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"没有小车！！"));
        }
        //将小车按照maxWeight从大到小排序，载重大的先安排
        agvCars.sort(Comparator.comparingDouble(AgvCar::getMaxWeight).reversed());

        // 小车复用循环索引
        int carIndex = 0;
        String position = "";

        //小车去多个货架层的标记
        Set<String> assignedShelfCodes = new HashSet<>();

        List<CarOut> carTasks = new ArrayList<>();
        for (ShelfPickTask shelfPickTask:tasks){
            String shelfCode = shelfPickTask.getShelfCode();//该货架c
            final String sc=shelfCode;
            if (assignedShelfCodes.contains(shelfCode)) continue;
            List<SalesOrderDetailAddDTO> items = new ArrayList<>(shelfPickTask.getItems());
            Double totalWeight = shelfPickTask.getTotalWeight();//货架总重量
            Double locationX = shelfPickTask.getLocationX();
            Double locationY = shelfPickTask.getLocationY();
            Double locationZ = shelfPickTask.getLocationZ();
            AgvCar agvCar = agvCars.get(carIndex);
            carIndex = (carIndex + 1) % agvCars.size(); // 复用小车

            Double maxWeight = agvCar.getMaxWeight();
            double sumWeight=0.0;
            List<CarOutDetail> detailList = new ArrayList<>();
            while (!items.isEmpty()){

                //小车可装载的物品
                List<SalesOrderDetailAddDTO> carLoad = new ArrayList<>();

                //货架中其中一个货物
                Iterator<SalesOrderDetailAddDTO> iter = items.iterator();
                while (iter.hasNext()){
                    SalesOrderDetailAddDTO item = iter.next();
                    Product product = productService.findBySku(item.getSku());
                    Double unitWeight = product.getWeight();
                    Integer quantity = item.getQuantity();
                    double itemWeight= unitWeight*quantity;

                    if (sumWeight+itemWeight<=maxWeight){
                        //全装
                        carLoad.add(item);
                        sumWeight+=itemWeight;
                        iter.remove();
                    }else {
                        //部分装
                        int canLoadQty=(int)Math.floor((maxWeight-sumWeight)/unitWeight);
                        if (canLoadQty>0){
                            SalesOrderDetailAddDTO partItem = new SalesOrderDetailAddDTO();
                            partItem.setSku(item.getSku());
                            partItem.setName(item.getName());
                            partItem.setQuantity(canLoadQty);
                            carLoad.add(partItem);
                            sumWeight += canLoadQty * unitWeight;
                            item.setQuantity(quantity - canLoadQty);
                        }
                        break;
                    }
                }

                CarOutDetail detail = new CarOutDetail();
                detail.setShelfCode(shelfCode);
                detail.setLocationX(locationX);
                detail.setLocationY(locationY);
                detail.setLocationZ(locationZ);
                detail.setMiddleY(locationY);
                detail.setMiddleZ(locationZ);
                if (locationX==14||locationX==18||locationX==22||locationX==26||locationX==30||locationX==34||
                locationX==38||locationX==42||locationX==46){
                    detail.setMiddleX(locationX-1);
                    detail.setPosition("左");
                }else {
                    detail.setMiddleX(locationX+1);
                    detail.setPosition("右");
                }
                detail.setSumWeight(carLoad.stream().mapToDouble(dto -> {
                    Product product = productService.findBySku(dto.getSku());
                    return product.getWeight() * dto.getQuantity();
                }).sum());
                detail.setProductDetails(carLoad);
                detailList.add(detail);
                assignedShelfCodes.add(shelfCode);


                // 如果小车还有剩余载重
                double remaining = maxWeight - sumWeight;

                ShelfPickTask candidate = tasks.stream()
                        .filter(t -> !t.getShelfCode().equals(sc))//不是该货架（不是本次循环中的货架）
                        .filter(t -> !assignedShelfCodes.contains(t.getShelfCode()))//没被拿过
                        .filter(t -> t.getTotalWeight() <= remaining)
                        .max(Comparator.comparingDouble(ShelfPickTask::getTotalWeight))
                        .orElse(null);

                if (candidate != null) {
                    shelfCode = candidate.getShelfCode();
                    items = new ArrayList<>(candidate.getItems());
                    totalWeight = candidate.getTotalWeight();
                    locationX = candidate.getLocationX();
                    locationY = candidate.getLocationY();
                    locationZ = candidate.getLocationZ();

                    assignedShelfCodes.add(shelfCode);
                    continue; // 重新进入 while 尝试再装
                }else {
                    break;
                }


            }
            CarOut task = new CarOut();
            task.setCarNumber(agvCar.getCarNumber());
            task.setMaxWeight(maxWeight);
            task.setSumWeight(sumWeight);
            task.setStartX(agvCar.getLocationX().intValue());//入库点
            task.setStartY(agvCar.getLocationY().intValue());
            task.setEndX(agvCar.getLocationX().intValue()); // 出库点
            task.setEndY(agvCar.getLocationY().intValue());
            detailList.sort(Comparator.comparingDouble(detail ->
                    Math.abs(detail.getLocationX() - 11)*Constants.MOVE_DIFFER_X + Math.abs(detail.getLocationY() - 23)*Constants.MOVE_DIFFER_Y
                    +Math.abs(detail.getLocationZ())*Constants.MOVE_DIFFER_Z
            ));
            task.setDetails(detailList);

            carTasks.add(task);
        }
        /**
         * 获得了每个小车起始点和终点，以及需要去的货架层List（一系列中间点）
         * 又获得了地图，开始算法找路径
         */
        for (CarOut carOut:carTasks){
            Integer startX = carOut.getStartX();
            Integer startY = carOut.getStartY();
            Integer endX = carOut.getEndX();
            Integer endY = carOut.getEndY();
            int currentTime=0;
            //从停车点到仓库入口
            int[][] map1=BfsFindPath.InMapToProduct(startX,startY,9,25);
            List<Point> path1 = BfsFindPath.bfsFindPath(map1, startX, startY, 11, 23, 0);
            List<Point> fullPath = new ArrayList<>(path1);
            currentTime+=fullPath.getLast().getTime();
            List<CarOutDetail> details1 = carOut.getDetails();
            int lastX = 11;
            int lastY = 23;
            int[][] map2 = BfsFindPath.InMap();
            for (CarOutDetail detail:details1){
                int midX = detail.getMiddleX().intValue();
                int midY = detail.getMiddleY().intValue();
                int midZ = detail.getMiddleZ().intValue();
//                int resultTime=fullPath.getLast().getTime();
                //出库入口-》货架交互点
                int beforeStopTime;
                if (lastY==midY&&lastX==midX){
                    //同一货架不同层
                    //暂时不处理
                }
                List<Point> path2 = BfsFindPath.bfsFindPath(map2, lastX, lastY, midX, midY, currentTime);
                if (!path2.isEmpty() && !fullPath.isEmpty()) {
                    if (fullPath.getLast().equals(path2.getFirst())) {
                        path2.removeFirst();
                    }
                }

                fullPath.addAll(path2);
                // 2. Z轴等待装货（每层耗时2秒）
                currentTime = fullPath.getLast().getTime();
                int zTime = midZ * 2;
                for (int i = 1; i <= zTime; i++) {
                    fullPath.add(new Point(midX, midY, currentTime + i));
                }
                currentTime += zTime;
                lastX = midX;
                lastY = midY;
            }

            // 3. 最后一个货架 → 出库出口点（固定11,11）int[][] map2 = BfsFindPath.InMap();
            List<Point> toExit = BfsFindPath.bfsFindPath(map2, lastX, lastY, 11, 11, currentTime);
            if (!toExit.isEmpty() && !fullPath.isEmpty()) {
                if (fullPath.get(fullPath.size() - 1).equals(toExit.get(0))) {
                    toExit.remove(0);
                }
            }
            fullPath.addAll(toExit);

            //出口--》出库点
            int time = fullPath.getLast().getTime();
            int[][] map3 = BfsFindPath.InMapToProduct(11, 11, 3, 2);
            List<Point> path3 = BfsFindPath.bfsFindPath(map3, 11, 11, 5, 2, time);//(5,2)开始出货
            int UnLoadingTime=Constants.INTERACTION;
            int time1 = fullPath.getLast().getTime();


            if (!path3.isEmpty() && !fullPath.isEmpty()) {
                if (fullPath.get(fullPath.size() - 1).equals(path3.get(0))) {
                    path3.remove(0);
                }
            }
            fullPath.addAll(path3);
            for (int i=1;i<=UnLoadingTime;i++){
                fullPath.add(new Point(5,2,fullPath.getLast().getTime()+1));
            }
            //出库点--》停车位
            int[][] map4 = BfsFindPath.InMapToProduct(endX, endY, 9, 2);
            List<Point> path4 = BfsFindPath.bfsFindPath(map4, 5, 2, endX, endY, fullPath.getLast().getTime());
            if (!path4.isEmpty() && !fullPath.isEmpty()) {
                if (fullPath.get(fullPath.size() - 1).equals(path4.get(0))) {
                    path4.remove(0);
                }
            }
            fullPath.addAll(path4);
            carOut.setPaths(fullPath);
        }


        carTasks = carTasks.stream()
                    .filter(task -> task.getPaths() != null && !task.getPaths().isEmpty())
                    .collect(Collectors.toList());

        int globalStartTime = 0;
        Map<String, Integer> carLastEndTime = new HashMap<>();
        Map<Integer, Set<String>> occupied = new HashMap<>(); // 时间点 → 被占用坐标


        // 是否处理小车路径冲突

        String dealing="是";
        List<FactoryConfig> list1 = factoryConfigService.list();
        if (list1!=null){
            FactoryConfig first = list1.getFirst();
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
        if (dealing.equals("是")) {

            for (CarOut task : carTasks) {
                String carNumber = task.getCarNumber();
                List<Point> originalPath = task.getPaths();

                // 初始出发时间
                int startTime = carLastEndTime.getOrDefault(carNumber, globalStartTime);
                List<Point> adjustedPath;

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
                    startTime++; // 有冲突则延迟1秒
                }

                // 更新路径与出发时间
                task.setPaths(adjustedPath);
                task.setDispatchTime(startTime);

                // 占位记录
                for (Point p : adjustedPath) {
                    int t = p.getTime();
                    String pos = p.getX() + "," + p.getY();
                    occupied.computeIfAbsent(t, k -> new HashSet<>()).add(pos);
                }

                // 记录该车最新使用结束时间
                int endTime = adjustedPath.get(adjustedPath.size() - 1).getTime() + 1;
                carLastEndTime.put(carNumber, endTime);
            }

        } else {
            // 不处理冲突，统一按最后一次结束时间顺延

            for (CarOut task : carTasks) {
                String carNumber = task.getCarNumber();
                List<Point> originalPath = task.getPaths();

                if (originalPath == null || originalPath.isEmpty()) continue;

                int startTime = carLastEndTime.getOrDefault(carNumber, globalStartTime);
                List<Point> adjustedPath = new ArrayList<>();

                for (Point p : originalPath) {
                    adjustedPath.add(new Point(p.getX(), p.getY(), p.getTime() + startTime));
                }

                task.setPaths(adjustedPath);
                task.setDispatchTime(startTime);

                int endTime = adjustedPath.get(adjustedPath.size() - 1).getTime() + 1;
                carLastEndTime.put(carNumber, endTime);
            }
        }


        /**
         * 算法完成！！获得了小车对应的路径
         *   开始更新数据库
         *   1、更新库存表
         *   2、更新出库单状态
         *   3、更新货架-货物关系图
         */
        tasks.forEach(System.out::println);
        String orderNumber1 = order.getOrderNumber();
        List<SalesOrderDetail> details1 = salesOrderDetailService.getSalesOrderDetailsByOrderNumber(orderNumber1);
        for (SalesOrderDetail orderDetail:details1){
            String sku = orderDetail.getSku();
            Long quantity = orderDetail.getQuantity();
            boolean b = inventoryService.decreaseInventory(sku, quantity);
            if (!b){
                return ResponseEntity.badRequest().body(AjaxResult.error(400,"库存不足"));
            }
        }
        outboundOrderService.updateStatus(orderNumber1,Constants.STATUS_ORDER_FINISHED);
        for (ShelfPickTask shelfPickTask:tasks){
            String shelfCode = shelfPickTask.getShelfCode();
            List<SalesOrderDetailAddDTO> items = shelfPickTask.getItems();
            if (items==null ||items.isEmpty()) continue;
            for (SalesOrderDetailAddDTO detail:items){
                String sku = detail.getSku();
                Integer quantity = detail.getQuantity();
                ShelfInventory shelfInventory = shelfInventoryService.findByShelfCodeAndSku(shelfCode, sku);
                shelfInventory.setQuantity(shelfInventory.getQuantity()-quantity);
                shelfInventoryService.updateShelfInventory(shelfInventory);
            }
        }

///////////////////////////////////////////////////////////////////////////////////////////
        Map<String, CarOut> mergedMap = new LinkedHashMap<>();

        for (CarOut task : carTasks) {
            String carNumber = task.getCarNumber();

            if (!mergedMap.containsKey(carNumber)) {
                // 首次出现，复制整个任务
                mergedMap.put(carNumber, task);
            } else {
                // 合并到已有记录
                CarOut existing = mergedMap.get(carNumber);

                // 合并 paths
                List<Point> mergedPaths = new ArrayList<>(existing.getPaths());
                List<Point> newPaths = task.getPaths();

                // 保证路径不重复衔接
                if (!mergedPaths.isEmpty() && !newPaths.isEmpty()) {
                    Point last = mergedPaths.get(mergedPaths.size() - 1);
                    Point first = newPaths.get(0);
                    if (last.getX() == first.getX() && last.getY() == first.getY()) {
                        newPaths.remove(0);
                    }
                }
                mergedPaths.addAll(newPaths);
                existing.setPaths(mergedPaths);

                // 合并详情
                List<CarOutDetail> combinedDetails = new ArrayList<>(existing.getDetails());
                combinedDetails.addAll(task.getDetails());
                existing.setDetails(combinedDetails);

                // 更新总重
                existing.setSumWeight(existing.getSumWeight() + task.getSumWeight());

                // 最终终点更新为最近一次的
                existing.setEndX(task.getEndX());
                existing.setEndY(task.getEndY());
            }
        }

        List<CarOut> mergedCarTasks = new ArrayList<>(mergedMap.values());
        return ResponseEntity.ok(AjaxResult.success(mergedCarTasks));

//        return ResponseEntity.ok(AjaxResult.success(carTasks));
    }


    /**
     * 路劲展示
     * @param id
     * @return
     */
    @GetMapping("/out/{id}")
    public ResponseEntity<AjaxResult> outBound(@PathVariable Long id){
        OutboundOrder outDTO = outboundOrderService.getById(id);
//        if (!outDTO.getStatus().equals(Constants.STATUS_ORDER_TOSTART)){
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"该出库单已出库或正在出库"));
//        }
        if (outDTO.getOrderNumber() == null || outDTO.getOrderNumber().isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"出库失败"));
        }
        SalesOrder order = salesOrderService.findByOrderNo(outDTO.getOrderNumber());
        OutboundOrder outboundOrder = outboundOrderService.findByOrderNumber(outDTO.getOrderNumber());
        List<SalesOrderDetail> details = salesOrderDetailService.getSalesOrderDetailsByOrderNumber(outDTO.getOrderNumber());
        if (order==null||outboundOrder==null|| details.isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"数据库没有该数据"));
        }

        /**
         * 此时可以开始出库了！！！！！
         *
         * 1、调用【算法】获得 需要出库的货物的位置、数量，以及对应的路径
         * 2、  处理 ：  库存表  减去对应的货物数量【在创建订单的时候就做了判断，故库存一定够】
         * 3、  处理：   货架-货物关系表  更新， 根据位置，sku，数量更新
         * 4、  处理：   更新 出库单的状态为“已完成” 【这里订单、订单详细都不变】
         */
        int beginX = 11, beginY = 14, beginZ = 1;



        double alpha; // 路径权重
        double beta;  // 数量权重

        switch (Constants.OUT_PREFER){
            case "system-judged":
                // 系统智能判断
                int skuCount = details.size();
                long avgQty = details.stream()
                        .mapToLong(SalesOrderDetail::getQuantity)
                        .sum() / Math.max(1, skuCount);

                if (skuCount <= 3 && avgQty > 20) {
                    alpha = 0.8;
                    beta = 3000.0;
                } else {
                    alpha = 2.0;
                    beta = 500.0;
                }
                break;
            case "short-path":
                alpha = 2.0;
                beta = 500.0;
                break;
            case "more-stock":
                alpha = 0.5;
                beta = 3000.0;
                break;
            case "balanced":
                alpha = 1.0;
                beta = 1000.0;
                break;
            default:
                alpha = 1.0;
                beta = 1000.0;
                break;
        }

        String orderNumber = outDTO.getOrderNumber();
        List<OutboundRecord> outboundRecords = new ArrayList<>();
        for (SalesOrderDetail salesOrderDetail:details){
            String sku = salesOrderDetail.getSku();
            Product product = productService.findBySku(sku);
            Long quantity = salesOrderDetail.getQuantity();//需要的数量
            List<ShelfInventory> inventoryList = shelfInventoryService.findBySku(sku);
            if (inventoryList==null||inventoryList.isEmpty()){
                return ResponseEntity.badRequest().body(AjaxResult.error(400,"找不到货物"));
            }


            /**
             * 1、先判断是否有“一次能取完”的 → 最快方案；
             * 2、如果没有，再按 距离/数量比值 排序，做最佳分配。
             *
             */
            // 排序：路径最近优先  VS    一次能取完优先
            //score = 路径距离 / 可出库数量
            //1、
            // 优先找一个能一次出完的货架（路径最短优先）
            final long finalNeededQty = quantity;
            Optional<ShelfInventory> oneShot = inventoryList.stream()
                    .filter(inv -> inv.getQuantity() != null && inv.getQuantity() >= finalNeededQty)
                    .min(Comparator.comparingInt(inv -> {
                        String shelfCode = inv.getShelfCode();
                        StorageShelf shelf = storageShelfService.findByShelfCode(shelfCode);
                        int locationX = shelf.getLocationX().intValue();
                        int locationY = shelf.getLocationY().intValue();
                        int locationZ = shelf.getLocationZ().intValue();
                        return Math.abs(locationX - beginX)*Constants.MOVE_DIFFER_X + Math.abs(locationY - beginY)*
                                Constants.MOVE_DIFFER_Y + Math.abs(locationZ - beginZ + 1) * Constants.MOVE_DIFFER_Z;
                    }));

            if (oneShot.isPresent()) {
                ShelfInventory inv = oneShot.get();
                outboundRecords.add(new OutboundRecord(sku,product.getName(),inv.getShelfCode(), quantity));
                inv.setQuantity(inv.getQuantity() - quantity);
                continue;
            }

            //不能一次出完，就按 距离/数量 综合打分排序
            List<ShelfInventory> sortedList = inventoryList.stream()
                    .filter(inv -> inv.getQuantity() != null && inv.getQuantity() > 0)
                    .sorted(Comparator.comparingDouble(inv -> {
                        String shelfCode = inv.getShelfCode();
                        StorageShelf shelf = storageShelfService.findByShelfCode(shelfCode);
                        int locationX = shelf.getLocationX().intValue();
                        int locationY = shelf.getLocationY().intValue();
                        int locationZ = shelf.getLocationZ().intValue();
                        int pathScore= Math.abs(locationX - beginX)*Constants.MOVE_DIFFER_X + Math.abs(locationY - beginY)*
                                Constants.MOVE_DIFFER_Y + Math.abs(locationZ - beginZ + 1) * Constants.MOVE_DIFFER_Z;
                        double distanceScore = pathScore * alpha;
                        double quantityScore = beta / (double) inv.getQuantity(); // 越多越好
                        return distanceScore + quantityScore;//越小越好
                    }))
                    .toList();


            // 从排序后的货架中分配库存
            for (ShelfInventory inv:sortedList){
                if (quantity<=0) break;

                Long available = inv.getQuantity();//货架有的数量
                if (available==null||available<=0) continue;

                Long toPick = Math.min(available, quantity);

                outboundRecords.add(new OutboundRecord(sku,product.getName(), inv.getShelfCode(),toPick));
                inv.setQuantity(available-toPick);
                quantity-=toPick;

            }

            if (quantity>0){
                return ResponseEntity.internalServerError().body(AjaxResult.error(500,"库存不足！！"));
            }

        }
        System.out.println(outboundRecords.size());
//        outboundRecords.forEach(System.out::println);

        /**
         * 以获得订单里的物品要从哪些货架层上拿，以及拿多少的问题
         * 后面我们合并相同货架层的物品，得到我们需要到哪些货架层拿哪几类物品以及对应的数量
         */
        Map<String, Map<String, SalesOrderDetailAddDTO>> tempMap = new HashMap<>();

        for (OutboundRecord record : outboundRecords) {
            String shelfCode = record.getShelfCode();
            String sku = record.getSku();
            String productName = record.getProductName();
            Long quantity = record.getQuantity();

            tempMap
                    .computeIfAbsent(shelfCode, k -> new HashMap<>())
                    .compute(sku, (k, existing) -> {
                        if (existing == null) {
                            return new SalesOrderDetailAddDTO(productName,sku, quantity.intValue());
                        } else {
                            existing.setQuantity(existing.getQuantity() + quantity.intValue());
                            return existing;
                        }
                    });
        }
        // 转成 Map<String, List<PickTaskItem>>
        Map<String, List<SalesOrderDetailAddDTO>> result = new HashMap<>();
        for (Map.Entry<String, Map<String, SalesOrderDetailAddDTO>> entry : tempMap.entrySet()) {
            result.put(entry.getKey(), new ArrayList<>(entry.getValue().values()));
        }
        System.out.println(result.size());
        List<ShelfPickTask> tasks = new ArrayList<>();

        for (Map.Entry<String, List<SalesOrderDetailAddDTO>> entry : result.entrySet()) {
            String shelfCode = entry.getKey();
            List<SalesOrderDetailAddDTO> items = entry.getValue();

            double totalWeight = items.stream()
                    .mapToDouble(item -> {
                        String sku = item.getSku();
                        Product product = productService.findBySku(sku);
                        return product.getWeight() * item.getQuantity();
                    })
                    .sum();

            ShelfPickTask task = new ShelfPickTask();
            task.setShelfCode(shelfCode);
            task.setLocationX(storageShelfService.findByShelfCode(shelfCode).getLocationX());
            task.setLocationY(storageShelfService.findByShelfCode(shelfCode).getLocationY());
            task.setLocationZ(storageShelfService.findByShelfCode(shelfCode).getLocationZ());
            task.setItems(items);
            task.setTotalWeight(totalWeight);
            tasks.add(task);
        }
        // 按总重量降序排序
        tasks.sort(Comparator.comparingDouble(ShelfPickTask::getTotalWeight).reversed());
        System.out.println(tasks.size());
        /**
         * 1、把这些货架层分配给小车装载
         *        1、小车最大载重若比该货架层的小，尽可能的装
         *        2、小车最大载重若大于该货架层1，则可全装到小车上，并查找距离该货架层1最近的货架层2，如果货架层2可以全装到该小车上则该小车去2那里装货，否则回出库点
         *        3、可能会出现小车的复用的问题
         *        返回：List<CarOut>
         *
         */
        List<AgvCar> agvCars = agvCarService.findAll();
        if (agvCars.isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"没有小车！！"));
        }
        //将小车按照maxWeight从大到小排序，载重大的先安排
        agvCars.sort(Comparator.comparingDouble(AgvCar::getMaxWeight).reversed());

        // 小车复用循环索引
        int carIndex = 0;
        String position = "";

        //小车去多个货架层的标记
        Set<String> assignedShelfCodes = new HashSet<>();

        List<CarOut> carTasks = new ArrayList<>();
        for (ShelfPickTask shelfPickTask:tasks){
            String shelfCode = shelfPickTask.getShelfCode();//该货架c
            final String sc=shelfCode;
            if (assignedShelfCodes.contains(shelfCode)) continue;
            List<SalesOrderDetailAddDTO> items = new ArrayList<>(shelfPickTask.getItems());
            Double totalWeight = shelfPickTask.getTotalWeight();//货架总重量
            Double locationX = shelfPickTask.getLocationX();
            Double locationY = shelfPickTask.getLocationY();
            Double locationZ = shelfPickTask.getLocationZ();
            AgvCar agvCar = agvCars.get(carIndex);
            carIndex = (carIndex + 1) % agvCars.size(); // 复用小车

            Double maxWeight = agvCar.getMaxWeight();
            double sumWeight=0.0;
            List<CarOutDetail> detailList = new ArrayList<>();
            while (!items.isEmpty()){

                //小车可装载的物品
                List<SalesOrderDetailAddDTO> carLoad = new ArrayList<>();

                //货架中其中一个货物
                Iterator<SalesOrderDetailAddDTO> iter = items.iterator();
                while (iter.hasNext()){
                    SalesOrderDetailAddDTO item = iter.next();
                    Product product = productService.findBySku(item.getSku());
                    Double unitWeight = product.getWeight();
                    Integer quantity = item.getQuantity();
                    double itemWeight= unitWeight*quantity;

                    if (sumWeight+itemWeight<=maxWeight){
                        //全装
                        carLoad.add(item);
                        sumWeight+=itemWeight;
                        iter.remove();
                    }else {
                        //部分装
                        int canLoadQty=(int)Math.floor((maxWeight-sumWeight)/unitWeight);
                        if (canLoadQty>0){
                            SalesOrderDetailAddDTO partItem = new SalesOrderDetailAddDTO();
                            partItem.setSku(item.getSku());
                            partItem.setName(item.getName());
                            partItem.setQuantity(canLoadQty);
                            carLoad.add(partItem);
                            sumWeight += canLoadQty * unitWeight;
                            item.setQuantity(quantity - canLoadQty);
                        }
                        break;
                    }
                }

                CarOutDetail detail = new CarOutDetail();
                detail.setShelfCode(shelfCode);
                detail.setLocationX(locationX);
                detail.setLocationY(locationY);
                detail.setLocationZ(locationZ);
                detail.setMiddleY(locationY);
                detail.setMiddleZ(locationZ);
                if (locationX==14||locationX==18||locationX==22||locationX==26||locationX==30||locationX==34||
                        locationX==38||locationX==42||locationX==46){
                    detail.setMiddleX(locationX-1);
                    detail.setPosition("左");
                }else {
                    detail.setMiddleX(locationX+1);
                    detail.setPosition("右");
                }
                detail.setSumWeight(carLoad.stream().mapToDouble(dto -> {
                    Product product = productService.findBySku(dto.getSku());
                    return product.getWeight() * dto.getQuantity();
                }).sum());
                detail.setProductDetails(carLoad);
                detailList.add(detail);
                assignedShelfCodes.add(shelfCode);


                // 如果小车还有剩余载重
                double remaining = maxWeight - sumWeight;

                ShelfPickTask candidate = tasks.stream()
                        .filter(t -> !t.getShelfCode().equals(sc))//不是该货架（不是本次循环中的货架）
                        .filter(t -> !assignedShelfCodes.contains(t.getShelfCode()))//没被拿过
                        .filter(t -> t.getTotalWeight() <= remaining)
                        .max(Comparator.comparingDouble(ShelfPickTask::getTotalWeight))
                        .orElse(null);

                if (candidate != null) {
                    shelfCode = candidate.getShelfCode();
                    items = new ArrayList<>(candidate.getItems());
                    totalWeight = candidate.getTotalWeight();
                    locationX = candidate.getLocationX();
                    locationY = candidate.getLocationY();
                    locationZ = candidate.getLocationZ();

                    assignedShelfCodes.add(shelfCode);
                    continue; // 重新进入 while 尝试再装
                }else {
                    break;
                }


            }
            CarOut task = new CarOut();
            task.setCarNumber(agvCar.getCarNumber());
            task.setMaxWeight(maxWeight);
            task.setSumWeight(sumWeight);
            task.setStartX(agvCar.getLocationX().intValue());//入库点
            task.setStartY(agvCar.getLocationY().intValue());
            task.setEndX(agvCar.getLocationX().intValue()); // 出库点
            task.setEndY(agvCar.getLocationY().intValue());
            detailList.sort(Comparator.comparingDouble(detail ->
                    Math.abs(detail.getLocationX() - 11)*Constants.MOVE_DIFFER_X + Math.abs(detail.getLocationY() - 23)*Constants.MOVE_DIFFER_Y
                            +Math.abs(detail.getLocationZ())*Constants.MOVE_DIFFER_Z
            ));
            task.setDetails(detailList);

            carTasks.add(task);
        }
        /**
         * 获得了每个小车起始点和终点，以及需要去的货架层List（一系列中间点）
         * 又获得了地图，开始算法找路径
         */
        for (CarOut carOut:carTasks){
            Integer startX = carOut.getStartX();
            Integer startY = carOut.getStartY();
            Integer endX = carOut.getEndX();
            Integer endY = carOut.getEndY();
            int currentTime=0;
            //从停车点到仓库入口
            int[][] map1=BfsFindPath.InMapToProduct(startX,startY,9,25);
            List<Point> path1 = BfsFindPath.bfsFindPath(map1, startX, startY, 11, 23, 0);
            List<Point> fullPath = new ArrayList<>(path1);
            currentTime+=fullPath.getLast().getTime();
            List<CarOutDetail> details1 = carOut.getDetails();
            int lastX = 11;
            int lastY = 23;
            int[][] map2 = BfsFindPath.InMap();
            for (CarOutDetail detail:details1){
                int midX = detail.getMiddleX().intValue();
                int midY = detail.getMiddleY().intValue();
                int midZ = detail.getMiddleZ().intValue();
//                int resultTime=fullPath.getLast().getTime();
                //出库入口-》货架交互点
                int beforeStopTime;
                if (lastY==midY&&lastX==midX){
                    //同一货架不同层
                    //暂时不处理
                }
                List<Point> path2 = BfsFindPath.bfsFindPath(map2, lastX, lastY, midX, midY, currentTime);
                if (!path2.isEmpty() && !fullPath.isEmpty()) {
                    if (fullPath.getLast().equals(path2.getFirst())) {
                        path2.removeFirst();
                    }
                }

                fullPath.addAll(path2);
                // 2. Z轴等待装货（每层耗时2秒）
                currentTime = fullPath.getLast().getTime();
                int zTime = midZ * 2;
                for (int i = 1; i <= zTime; i++) {
                    fullPath.add(new Point(midX, midY, currentTime + i));
                }
                currentTime += zTime;
                lastX = midX;
                lastY = midY;
            }

            // 3. 最后一个货架 → 出库出口点（固定11,11）int[][] map2 = BfsFindPath.InMap();
            List<Point> toExit = BfsFindPath.bfsFindPath(map2, lastX, lastY, 11, 11, currentTime);
            if (!toExit.isEmpty() && !fullPath.isEmpty()) {
                if (fullPath.get(fullPath.size() - 1).equals(toExit.get(0))) {
                    toExit.remove(0);
                }
            }
            fullPath.addAll(toExit);

            //出口--》出库点
            int time = fullPath.getLast().getTime();
            int[][] map3 = BfsFindPath.InMapToProduct(11, 11, 3, 2);
            List<Point> path3 = BfsFindPath.bfsFindPath(map3, 11, 11, 5, 2, time);//(5,2)开始出货
            int UnLoadingTime=Constants.INTERACTION;
            int time1 = fullPath.getLast().getTime();


            if (!path3.isEmpty() && !fullPath.isEmpty()) {
                if (fullPath.get(fullPath.size() - 1).equals(path3.get(0))) {
                    path3.remove(0);
                }
            }
            fullPath.addAll(path3);
            for (int i=1;i<=UnLoadingTime;i++){
                fullPath.add(new Point(5,2,fullPath.getLast().getTime()+1));
            }
            //出库点--》停车位
            int[][] map4 = BfsFindPath.InMapToProduct(endX, endY, 9, 2);
            List<Point> path4 = BfsFindPath.bfsFindPath(map4, 5, 2, endX, endY, fullPath.getLast().getTime());
            if (!path4.isEmpty() && !fullPath.isEmpty()) {
                if (fullPath.get(fullPath.size() - 1).equals(path4.get(0))) {
                    path4.remove(0);
                }
            }
            fullPath.addAll(path4);
            carOut.setPaths(fullPath);
        }


        carTasks = carTasks.stream()
                .filter(task -> task.getPaths() != null && !task.getPaths().isEmpty())
                .collect(Collectors.toList());

        int globalStartTime = 0;
        Map<String, Integer> carLastEndTime = new HashMap<>();
        Map<Integer, Set<String>> occupied = new HashMap<>(); // 时间点 → 被占用坐标


        // 是否处理小车路径冲突
        if (Constants.DEALING_WITH_CONFLICTS.equals("是")) {

            for (CarOut task : carTasks) {
                String carNumber = task.getCarNumber();
                List<Point> originalPath = task.getPaths();

                // 初始出发时间
                int startTime = carLastEndTime.getOrDefault(carNumber, globalStartTime);
                List<Point> adjustedPath;

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
                    startTime++; // 有冲突则延迟1秒
                }

                // 更新路径与出发时间
                task.setPaths(adjustedPath);
                task.setDispatchTime(startTime);

                // 占位记录
                for (Point p : adjustedPath) {
                    int t = p.getTime();
                    String pos = p.getX() + "," + p.getY();
                    occupied.computeIfAbsent(t, k -> new HashSet<>()).add(pos);
                }

                // 记录该车最新使用结束时间
                int endTime = adjustedPath.get(adjustedPath.size() - 1).getTime() + 1;
                carLastEndTime.put(carNumber, endTime);
            }

        } else {
            // 不处理冲突，统一按最后一次结束时间顺延

            for (CarOut task : carTasks) {
                String carNumber = task.getCarNumber();
                List<Point> originalPath = task.getPaths();

                if (originalPath == null || originalPath.isEmpty()) continue;

                int startTime = carLastEndTime.getOrDefault(carNumber, globalStartTime);
                List<Point> adjustedPath = new ArrayList<>();

                for (Point p : originalPath) {
                    adjustedPath.add(new Point(p.getX(), p.getY(), p.getTime() + startTime));
                }

                task.setPaths(adjustedPath);
                task.setDispatchTime(startTime);

                int endTime = adjustedPath.get(adjustedPath.size() - 1).getTime() + 1;
                carLastEndTime.put(carNumber, endTime);
            }
        }

        Map<String, CarOut> mergedMap = new LinkedHashMap<>();

        for (CarOut task : carTasks) {
            String carNumber = task.getCarNumber();

            if (!mergedMap.containsKey(carNumber)) {
                // 首次出现，复制整个任务
                mergedMap.put(carNumber, task);
            } else {
                // 合并到已有记录
                CarOut existing = mergedMap.get(carNumber);

                // 合并 paths
                List<Point> mergedPaths = new ArrayList<>(existing.getPaths());
                List<Point> newPaths = task.getPaths();

                // 保证路径不重复衔接
                if (!mergedPaths.isEmpty() && !newPaths.isEmpty()) {
                    Point last = mergedPaths.get(mergedPaths.size() - 1);
                    Point first = newPaths.get(0);
                    if (last.getX() == first.getX() && last.getY() == first.getY()&&last.getTime()==first.getTime()) {
                        newPaths.remove(0);
                    }
                }
                mergedPaths.addAll(newPaths);
                existing.setPaths(mergedPaths);

                // 合并详情
                List<CarOutDetail> combinedDetails = new ArrayList<>(existing.getDetails());
                combinedDetails.addAll(task.getDetails());
                existing.setDetails(combinedDetails);

                // 更新总重
                existing.setSumWeight(existing.getSumWeight() + task.getSumWeight());

                // 最终终点更新为最近一次的
                existing.setEndX(task.getEndX());
                existing.setEndY(task.getEndY());
            }
        }

        List<CarOut> mergedCarTasks = new ArrayList<>(mergedMap.values());
        System.out.println("================================================="+mergedCarTasks);
        return ResponseEntity.ok(AjaxResult.success(mergedCarTasks));
        /**
         * 算法完成！！获得了小车对应的路径
         *   开始更新数据库
         *   1、更新库存表
         *   2、更新出库单状态
         *   3、更新货架-货物关系图
         */
//        tasks.forEach(System.out::println);
//        String orderNumber1 = order.getOrderNumber();
//        List<SalesOrderDetail> details1 = salesOrderDetailService.getSalesOrderDetailsByOrderNumber(orderNumber1);
//        for (SalesOrderDetail orderDetail:details1){
//            String sku = orderDetail.getSku();
//            Long quantity = orderDetail.getQuantity();
//            boolean b = inventoryService.decreaseInventory(sku, quantity);
//            if (!b){
//                return ResponseEntity.badRequest().body(AjaxResult.error(400,"库存不足"));
//            }
//        }
//        outboundOrderService.updateStatus(orderNumber1,Constants.STATUS_ORDER_FINISHED);
//        for (ShelfPickTask shelfPickTask:tasks){
//            String shelfCode = shelfPickTask.getShelfCode();
//            List<SalesOrderDetailAddDTO> items = shelfPickTask.getItems();
//            if (items==null ||items.isEmpty()) continue;
//            for (SalesOrderDetailAddDTO detail:items){
//                String sku = detail.getSku();
//                Integer quantity = detail.getQuantity();
//                ShelfInventory shelfInventory = shelfInventoryService.findByShelfCodeAndSku(shelfCode, sku);
//                shelfInventory.setQuantity(shelfInventory.getQuantity()-quantity);
//                shelfInventoryService.updateShelfInventory(shelfInventory);
//            }
//        }


//        return ResponseEntity.ok(AjaxResult.success(carTasks));
    }


}