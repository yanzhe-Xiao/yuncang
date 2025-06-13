package com.xhz.yuncang.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xhz.yuncang.dto.*;
import com.xhz.yuncang.entity.*;
import com.xhz.yuncang.service.*;
import com.xhz.yuncang.utils.AjaxResult;
import com.xhz.yuncang.utils.Constants;
import com.xhz.yuncang.utils.UserHolder;
import com.xhz.yuncang.vo.PageVo;
import com.xhz.yuncang.vo.SalesOrderInfoVo;
import com.xhz.yuncang.vo.UserInfoVo;
import com.xhz.yuncang.vo.UserVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 销售订单管理控制器
 *
 * <p>负责处理销售订单的全生命周期管理，提供以下核心功能：
 * <ul>
 *   <li>销售订单的分页查询</li>
 *   <li>订单的创建与删除</li>
 *   <li>库存校验与预警</li>
 *   <li>与出库单的联动管理</li>
 * </ul>
 *
 * <p>业务规则：
 * <ul>
 *   <li>订单编号必须唯一</li>
 *   <li>创建订单需检查库存余量</li>
 *   <li>只有"未开始"状态的订单可删除</li>
 *   <li>消费者只能查看自己的订单</li>
 * </ul>
 * @author xhz
 * @see SalesOrderService 销售订单服务
 * @see OutboundOrderService 出库订单服务
 * @see SalesOrderDetailService 销售订单明细服务
 */
@RestController
public class SalesOrderController {
    @Autowired
    private static final Logger logger = LoggerFactory.getLogger(SalesOrderController.class);

    /**
     * 销售订单服务实例
     */
    @Autowired
    private SalesOrderService salesOrderService;
    /**
     * 库存服务实例
     */
    @Autowired
    private InventoryService inventoryService;
    /**
     * 销售订单明细服务实例
     */
    @Autowired
    private SalesOrderDetailService salesOrderDetailService;
    /**
     * 用户服务实例
     */
    @Autowired
    private UserService userService;
    /**
     * 出库订单服务实例
     */
    @Autowired
    private OutboundOrderService outboundOrderService;
    /**
     * 商品服务实例
     */
    @Autowired
    private ProductService productService;
    /**
     * 提醒服务实例
     */
    @Autowired
    private RemindService remindService;


    /**
     * 分页查询销售订单
     *
     * <p>获取分页的订单列表，支持按订单号模糊查询，包含以下处理：
     * <ol>
     *   <li>验证页码有效性</li>
     *   <li>消费者只能查看自己的订单</li>
     *   <li>构建分页导航信息</li>
     *   <li>关联查询订单明细和出库状态</li>
     * </ol>
     *
     * @param current 当前页码（从1开始，默认1）
     * @param pageSize 每页记录数（默认10）
     * @param orderNumber 订单号模糊查询条件
     * @return 响应实体，包含：
     *         <ul>
     *           <li>成功：状态码200+分页数据</li>
     *           <li>失败：状态码400+错误信息</li>
     *         </ul>
     */
    @GetMapping("/order")
    public ResponseEntity<AjaxResult> getAllOrders(
            @RequestParam(defaultValue = "1")int current,
            @RequestParam(defaultValue = "10")int pageSize,
            @RequestParam(defaultValue = "")String orderNumber
    ){
        if (current<=0){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"页数小于1"));
        }
        Page<SalesOrder> page = new Page<>(current, pageSize);
        QueryWrapper<SalesOrder> queryWrapper = new QueryWrapper<>();

        if (!orderNumber.isEmpty()) {
            queryWrapper.like("order_number", orderNumber);
        }
        UserVo user = UserHolder.getUser();
        if (Objects.equals(user.getUserType(), Constants.USER_CUSTOMER)){
            User user1 = userService.findByUname(user.getUsername());
            queryWrapper.eq("user_id",user1.getUserId());
        }
        Page<SalesOrder> result = salesOrderService.page(page, queryWrapper);
        System.out.println("result: "+result);
        if (result.getPages()==0){
            return ResponseEntity.ok().body(AjaxResult.success("当前订单为空",""));
        }
        if (current>result.getPages()){
            //大于最大页数
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"页数大于最大页数"));
        }
        //当前页数nowPage（大于等于1，小于等于 最大页数）
        int nowPage= (int) result.getCurrent();
        //若当前页数为第一页，则它的上一页（beforePage）设为 -1；
        int beforePage= nowPage==1?-1:nowPage-1;
        //若当前页数为最后一页，则它的下一页(nextPage)设为 -1；
        int nextPage= nowPage==(int)result.getPages()?-1:nowPage+1;
        //List<SalesOrder>转为List<UserListPageVo>
        List<SalesOrder> salesOrders = result.getRecords();
        List<SalesOrderInfoVo> orderInfoVoList = salesOrders.stream().map(salesOrder -> {
            String number = salesOrder.getOrderNumber();
            OutboundOrder orderNumber1 = outboundOrderService.findByOrderNumber(number);
            List<SalesOrderDetail> details = salesOrderDetailService.getSalesOrderDetailsByOrderNumber(number);
            ArrayList<SalesOrderDetailAddDTO> detailAddDTOS = new ArrayList<>();
            if (!details.isEmpty()){
                for (SalesOrderDetail detail:details){
                    Product product = productService.findBySku(detail.getSku());
                    detailAddDTOS.add(new SalesOrderDetailAddDTO(product.getName(),detail.getSku(),detail.getQuantity().intValue()));
                }
            }
            return new SalesOrderInfoVo(
                    salesOrder.getId().toString(),
                    salesOrder.getOrderNumber(),
                    salesOrder.getUserId(),
                    salesOrder.getCreateTime(),
                    orderNumber1.getStatus(),
                    detailAddDTOS,
                    orderNumber1.getId().toString(),
                    orderNumber1.getUserId()
        );}).toList();

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("prev", beforePage);
        responseMap.put("next", nextPage);
        responseMap.put("total", result.getTotal());
        responseMap.put("list", orderInfoVoList);
        return ResponseEntity.ok().body(AjaxResult.success(responseMap));
    }

    /**
     * 创建销售订单
     *
     * <p>处理订单创建流程，包含以下步骤：
     * <ol>
     *   <li>验证订单信息完整性</li>
     *   <li>检查订单编号唯一性</li>
     *   <li>校验库存余量（考虑未出库的预留库存）</li>
     *   <li>创建销售订单记录</li>
     *   <li>创建关联的出库单记录</li>
     *   <li>保存订单明细</li>
     * </ol>
     *
     * <p>库存不足时会自动创建预警记录
     *
     * @param salesOrderInfoDTO 订单数据，包含：
     *                          <ul>
     *                            <li>orderNumber - 订单编号</li>
     *                            <li>details - 商品明细列表</li>
     *                          </ul>
     * @return 响应实体，包含：
     *         <ul>
     *           <li>成功：状态码200+成功消息</li>
     *           <li>失败：状态码400+错误原因</li>
     *         </ul>
     */
    @PostMapping("/order")
    public ResponseEntity<AjaxResult> addOrder(@RequestBody SalesOrderAddDTO salesOrderInfoDTO){
        System.out.println("salesOrderInfoDTO: "+salesOrderInfoDTO);
        /**
         *   需要注意：UserHold.getUser()为不为空的问题
         */
        String username = UserHolder.getUser().getUsername();
//            String username="xhz";
        User user = userService.findByUname(username);
        if (salesOrderInfoDTO == null || salesOrderInfoDTO.getDetails()==null||salesOrderInfoDTO.getDetails().isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"请填写信息"));
        }
        SalesOrder salesOrder = salesOrderService.findByOrderNo(salesOrderInfoDTO.getOrderNumber());
        if (salesOrder!=null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"订单编号重复"));
        }else {
            //判断库存是否盈余
            List<SalesOrderDetailAddDTO> productList = salesOrderInfoDTO.getDetails();
            for (SalesOrderDetailAddDTO productDTO:productList){
                String name = productDTO.getName();
                Product product1 = productService.findByName(name);
                if (product1==null){
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"未知物品"));
                }
                String sku = product1.getSku();
                productDTO.setSku(sku);
                Integer quantity = productDTO.getQuantity();
                Inventory product = inventoryService.getBySku(sku);
                if (product==null){
                    //库存里找不到改商品
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"没有该商品"));
                }
                Long productNum=product.getQuantity();
                //"未开始"
                List<OutboundOrder> outboundOrderList = outboundOrderService.findByStatus(Constants.STATUS_ORDER_TOSTART);
                if (outboundOrderList!=null){
                    for (OutboundOrder outboundOrder:outboundOrderList){
                        String orderNumber = outboundOrder.getOrderNumber();
                        SalesOrderDetail sku1 = salesOrderDetailService.getSalesOrderDetailByOrderNumberAndSku(orderNumber, sku);
                        if (sku1!=null){
                            productNum-=sku1.getQuantity();
                        }
                    }
                }
                if (productNum<quantity){
                    //数量不足
                    remindService.saveRemind(new Remind(null,Constants.REMIND_WARNING,"货物"+name+"不足",
                            "于"+LocalDateTime.now()+"时刻，客户"+username+"在添加订单的时候，货物"+name+"数量不足，建议添加"+
                                    (quantity-productNum+1)+"件"+name,LocalDateTime.now(),"0"));
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"库存不足"));
                }
            }
            //数量足够，更新SalesOrder表


            SalesOrder order = new SalesOrder(null ,salesOrderInfoDTO.getOrderNumber(),
                    user.getUserId(), LocalDateTime.now());
            boolean b = salesOrderService.addSalesOrder(order);

            //更新OutboundOrder表
            OutboundOrder outboundOrder = new OutboundOrder(null,order.getOrderNumber(),LocalDate.now(),null,Constants.STATUS_ORDER_TOSTART);
            outboundOrderService.addOneOutboundOrder(outboundOrder);

            //更新SalesOrderDetail表
            for (SalesOrderDetailAddDTO productDTO:productList){
                SalesOrderDetail salesOrderDetail = new SalesOrderDetail(null,order.getOrderNumber(),
                        productDTO.getSku(),(long) productDTO.getQuantity());
                salesOrderDetailService.addSalesOrderDetail(salesOrderDetail);
            }
            remindService.saveRemind(new Remind(null,Constants.REMIND_SUCCESS,"成功添加订单","于"+LocalDateTime.now()+",时刻,"+
                    user.getUserType()+user.getUsername()+"成功添加了编号为"+salesOrderInfoDTO.getOrderNumber()+"的订单",
                    LocalDateTime.now(),"1"));
            return ResponseEntity.ok().body(AjaxResult.success("添加成功"));
        }
    }


    /**
     * 删除销售订单
     *
     * <p>删除订单及相关记录，需满足：
     * <ul>
     *   <li>关联出库单状态必须为"未开始"</li>
     *   <li>级联删除订单明细</li>
     * </ul>
     *
     * @param id 订单ID（路径参数）
     * @return 响应实体，包含：
     *         <ul>
     *           <li>成功：状态码200+成功消息</li>
     *           <li>失败：状态码400/404+错误原因</li>
     *         </ul>
     */
    @DeleteMapping("/order/{id}")
    public ResponseEntity<AjaxResult> removeSalesOrder(@PathVariable("id") Long id){
//        String number = orderNumber.get("orderNumber");
//        String number="12";
        SalesOrder salesOrder2 = salesOrderService.getById(id);
        String number = salesOrder2.getOrderNumber();
        OutboundOrder byOrderNumber = outboundOrderService.findByOrderNumber(number);
        if (byOrderNumber!=null){
            if (byOrderNumber.getStatus().equals(Constants.STATUS_ORDER_TOSTART)){
                //“未开始”--》可以退
                outboundOrderService.deleteByOrderNumber(number);
            }else {
                //正在进行 和 已完成，不可退
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"订正在进行或已完成，不可退"));
            }
        }else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(AjaxResult.error(404,"订单编号未找到"));
        }
        //删除 订单细则
        List<SalesOrderDetail> details = salesOrderDetailService.getSalesOrderDetailsByOrderNumber(number);
        if (details!=null){
            salesOrderDetailService.removeSalesOrderDetailByOrderNumber(number);
        }

        //删除 订单
        SalesOrder salesOrder = salesOrderService.getSalesOrderByOrderNumber(number);
        if (salesOrder!=null){
            salesOrderService.deleteSalesOrderByOrderNumber(number);
        }
        UserVo user = UserHolder.getUser();
        remindService.saveRemind(new Remind(null,Constants.REMIND_SUCCESS,"成功删除订单","于"+LocalDateTime.now()+",时刻,"+
                user.getUserType()+user.getUsername()+"成功删除了编号为"+number+"的订单",
                LocalDateTime.now(),"1"));
        return ResponseEntity.ok().body(AjaxResult.success("成功删除"));
    }

    /**
     * 更新销售订单及其明细
     * 最佳实践：事务注解 @Transactional 应该放在 Service 层方法上。
     * 但为了将逻辑集中展示，这里直接放在Controller方法上。
     * 生产环境中建议将核心事务逻辑封装到 Service 方法中。
     *
     * @param id      要更新的销售订单ID (来自URL)
     * @param request 请求体，包含新的 orderNumber 和 details 列表
     * @return 响应结果
     */
    @PutMapping("/order/{id}")
    @Transactional(rollbackFor = Exception.class) // 开启事务，遇到任何异常都回滚
    public ResponseEntity<AjaxResult> updateOrder(
            @PathVariable("id") Long id,
            @RequestBody UpdateOrderRequestDTO request) {

        logger.info("尝试更新订单 ID: {}, 新订单号: {}, 明细数: {}", id, request.getOrderNumber(), request.getDetails() == null ? 0 :
                request.getDetails().size());

        // 1. 校验：查找原始订单
        SalesOrder existingOrder = salesOrderService.getById(id);
        if (existingOrder == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(AjaxResult.error(404, "要更新的订单ID " + id + " 未找到"));
        }
        if(request.getOrderNumber() == null || request.getOrderNumber().trim().isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400, "新的订单编号不能为空"));
        }

        String originalOrderNumber = existingOrder.getOrderNumber();
        String newOrderNumber = request.getOrderNumber();
        // 判断订单号是否真的改变了
        boolean isOrderNumberChanged = !originalOrderNumber.equals(newOrderNumber);

        // 2. 校验：检查出库单状态
        OutboundOrder outboundOrder = outboundOrderService.findByOrderNumber(originalOrderNumber);
        // 如果出库单存在，并且状态不是“未开始”，则不允许修改
        if (outboundOrder != null && !Constants.STATUS_ORDER_TOSTART.equals(outboundOrder.getStatus())) {
            // 正在进行 和 已完成，不可修改
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AjaxResult.error(400, "关联出库单正在进行或已完成，订单不可修改！状态：" + outboundOrder.getStatus()));
        }

        // 检查新订单号是否已存在 (如果订单号改变了才需要检查)
        if(isOrderNumberChanged) {
            SalesOrder checkOrder = salesOrderService.getSalesOrderByOrderNumber(newOrderNumber);
            // 如果能查到记录，并且查到的记录ID不是当前要修改的订单ID，说明新单号已被其他订单占用
            if(checkOrder != null && !checkOrder.getId().equals(id)){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(AjaxResult.error(400, "新的订单编号 [" + newOrderNumber + "] 已被其他订单占用！"));
            }
        }

        // 3. 准备数据：商品名称转SKU
        List<SalesOrderDetail> newDetailsToSave = new ArrayList<>();
        List<UpdateOrderRequestDTO.OrderDetailDTO> requestDetails = request.getDetails();

        if (!CollectionUtils.isEmpty(requestDetails)) {
            // 提取所有商品名称
            Set<String> productNames = requestDetails.stream()
                    .map(UpdateOrderRequestDTO.OrderDetailDTO::getName)
                    .collect(Collectors.toSet());

            // 查询并构建 Map<Name, Sku>
            Map<String, String> nameToSkuMap = productService.getNameToSkuMap(productNames);

            // 校验 & 构建新明细列表
            for (UpdateOrderRequestDTO.OrderDetailDTO item : requestDetails) {
                String sku = nameToSkuMap.get(item.getName());
                if (sku == null) {
                    // 如果有任何一个商品名称找不到对应的SKU，则报错并回滚 (因为有 @Transactional)
                    // throw new RuntimeException("商品名称 [" + item.getName() + "] 在商品库中未找到");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(AjaxResult.error(400,"商品名称 [" + item.getName() + "] 在商品库中未找到，无法更新订单！"));
                }
                if(item.getQuantity() == null || item.getQuantity() <=0){
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(AjaxResult.error(400,"商品 [" + item.getName() + "] 数量必须大于0！"));
                }
                SalesOrderDetail detail = new SalesOrderDetail();
                detail.setOrderNumber(newOrderNumber); // 使用新的订单号
                detail.setSku(sku);
                detail.setQuantity(item.getQuantity());
                newDetailsToSave.add(detail);
            }
        }

        // --- 以下操作在事务中执行 ---

        try {
            // 4. 删除旧的明细 (必须使用原始订单号删除)
            salesOrderDetailService.removeSalesOrderDetailByOrderNumber(originalOrderNumber);
            // 或使用MybatisPlus:
            // salesOrderDetailService.remove(Wrappers.<SalesOrderDetail>lambdaQuery().eq(SalesOrderDetail::getOrderNumber, originalOrderNumber));

            // 5. 插入新的明细 (如果列表不为空)
            if (!newDetailsToSave.isEmpty()) {
                salesOrderDetailService.saveBatch(newDetailsToSave); // MybatisPlus 批量插入
            }

            // 6. 如果订单号改变了，更新主表和出库单表
            if(isOrderNumberChanged){
                // 更新销售主单
                existingOrder.setOrderNumber(newOrderNumber);
                // 如果有更新时间字段，可以在这里设置 existingOrder.setUpdateTime(LocalDateTime.now())
                salesOrderService.updateById(existingOrder);

                // 更新关联的出库单 (如果存在)
                if(outboundOrder != null){
                    outboundOrder.setOrderNumber(newOrderNumber);
                    // 使用 ID 更新更安全
                    outboundOrderService.updateById(outboundOrder);
                    // 或者使用 QueryWrapper 更新
                    // outboundOrderService.update(Wrappers.<OutboundOrder>lambdaUpdate()
                    //        .set(OutboundOrder::getOrderNumber, newOrderNumber)
                    //        .eq(OutboundOrder::getOrderNumber, originalOrderNumber)); // 注意这种方式会更新所有同名订单，如果有id最好用id
                }
            } else {
                // 即使订单号没变，如果主表有其他字段或update_time需要更新，也要调用update
                // salesOrderService.updateById(existingOrder);
            }

            // 7. 记录日志/提醒
            UserVo user = UserHolder.getUser(); // 假设可以获取到
            String logMsg = String.format("于 %s 时刻, %s%s 成功修改了订单(ID:%d), 原编号:%s, 新编号:%s, 包含 %d 项明细。",
                    LocalDateTime.now(),
                    user != null ? user.getUserType(): "系统",
                    user != null ? user.getUsername(): "",
                    id,
                    originalOrderNumber,
                    newOrderNumber,
                    newDetailsToSave.size());

            remindService.saveRemind(new Remind(null, Constants.REMIND_SUCCESS, "成功修改订单", logMsg,
                    LocalDateTime.now(), "1")); // 假设"1"代表已处理

            logger.info("订单 ID: {} 更新成功。", id);
            return ResponseEntity.ok().body(AjaxResult.success("订单更新成功", newOrderNumber));

        } catch (Exception e) {
            logger.error("更新订单 ID: {} 失败, 事务将回滚", id, e);
            // 抛出运行时异常，使@Transactional生效回滚
            throw new RuntimeException("更新订单失败: " + e.getMessage(), e);
            // 或者不抛出，返回错误信息（但需确保@Transactional配置正确或手动回滚）
            // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(AjaxResult.error(500,"更新订单失败：" + e.getMessage()));
        }
    }


}
