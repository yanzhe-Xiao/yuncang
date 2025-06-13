package com.xhz.yuncang.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xhz.yuncang.dto.InboundOrderDetailInfoDTO;
import com.xhz.yuncang.dto.InventoryDTO;
import com.xhz.yuncang.dto.InventoryListDTO;
import com.xhz.yuncang.dto.SalesOrderDetailAddDTO;
import com.xhz.yuncang.entity.*;
import com.xhz.yuncang.service.*;
import com.xhz.yuncang.utils.AjaxResult;
import com.xhz.yuncang.utils.Constants;
import com.xhz.yuncang.vo.InventoryInfoVo;
import com.xhz.yuncang.vo.PageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * 库存管理控制器
 *
 * <p>负责处理库存记录的增删改查操作，提供以下核心功能：
 * <ul>
 *   <li>库存记录的单条/批量查询</li>
 *   <li>库存记录的添加与删除</li>
 *   <li>库存记录的分页查询</li>
 * </ul>
 *
 * <p>数据校验规则：
 * <ul>
 *   <li>SKU必须唯一</li>
 *   <li>数量必须为非负整数</li>
 *   <li>分页参数必须有效</li>
 * </ul>
 * @author WanCong
 * @see InventoryService 库存服务接口
 * @see InventoryDTO 库存数据传输对象
 * @see InventoryInfoVo 库存信息VO
 */
@RestController
@RequestMapping("/inventory")
//@PreAuthorize("hasRole('操作员') or hasRole('管理员')")
public class InventoryController {
    /**
     * 库存服务实例
     */
    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ProductService productService;
    /**
     * 根据SKU查询库存
     */
    @PostMapping("/getBySku")
    @PreAuthorize("hasRole('操作员') or hasRole('管理员')")
    public ResponseEntity<AjaxResult> getInventoryBySku(@RequestBody Map<String,String> sku) {
        Inventory inventory = inventoryService.getBySku(sku.get("sku"));
        if (inventory == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(AjaxResult.error("库存记录不存在"));
        }
        return ResponseEntity.ok(AjaxResult.success(inventory));
    }
    /**
     * 批量查询库存记录
     *
     * <p>根据SKU列表查询对应的库存信息，返回简化DTO列表
     *
     * @param inventoryListDTO 包含SKU列表的对象，结构：
     *                         {
     *                             "sku": ["SKU1", "SKU2", ...]
     *                         }
     * @return 响应实体，包含：
     *         <ul>
     *           <li>成功：状态码200+库存DTO列表</li>
     *           <li>失败：状态码400+错误信息</li>
     *         </ul>
     * @see InventoryDTO 简化的库存数据传输对象
     */
    @PostMapping("/batch")
    @PreAuthorize("hasRole('操作员') or hasRole('管理员')")
    public ResponseEntity<AjaxResult> listInventoryBySkus(@RequestBody InventoryListDTO inventoryListDTO) {
        List<InventoryDTO> inventories = inventoryService.listBySkus(inventoryListDTO.getSku()).stream()
                .map(inventory->new InventoryDTO(inventory.getSku(),inventory.getQuantity())).toList();
        return ResponseEntity.ok(AjaxResult.success(inventories));
    }



        @Autowired
        private SalesOrderService salesOrderService;

        @Autowired
        private OutboundOrderService outboundOrderService;

        @Autowired
        private SalesOrderDetailService salesOrderDetailService;
        @GetMapping("/products/all")
        public ResponseEntity<AjaxResult> getAllProducts(
                @RequestParam(defaultValue = "1")int current,
                @RequestParam(defaultValue = "200")int pageSize
        ){
            List<Inventory> inventories = inventoryService.listAll();
            System.out.println(inventories);
            List<SalesOrderDetailAddDTO> products=new ArrayList<>();
            if (inventories==null){
                return ResponseEntity.ok(AjaxResult.success(""));
            }
            for (Inventory inventory:inventories){
                String sku = inventory.getSku();
                Product product = productService.findBySku(sku);


                List<SalesOrder> list = salesOrderService.list();
                int minus=0;
                for (SalesOrder order:list){
                    String orderNumber = order.getOrderNumber();
                    OutboundOrder outboundOrder = outboundOrderService.findByOrderNumber(orderNumber);
                    if (outboundOrder.getStatus().equals(Constants.STATUS_ORDER_TOSTART)){
                        List<SalesOrderDetail> details = salesOrderDetailService.getSalesOrderDetailsByOrderNumber(orderNumber);
                        for (SalesOrderDetail detail:details){
                            if (detail.getSku().equals(sku)){
                                minus+=detail.getQuantity();
                            }
                        }
                    }
                }
                products.add(new SalesOrderDetailAddDTO(product.getName(),sku,inventory.getQuantity().intValue()-minus));

            }
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("prev", -1);
            responseMap.put("next", -1);
            responseMap.put("total", products.size());
            responseMap.put("list", products);
            return ResponseEntity.ok().body(AjaxResult.success(responseMap));
        }
//    /**
//     * 增加单个库存单的数量(入库时调用)
//     * @return
//     */
//    @PostMapping("/increase")
//   public ResponseEntity<AjaxResult> increaseQuantity(@RequestBody InboundOrderDetailInfoDTO inboundOrderDetailInfoDTO) {
//            if (inventoryService.increaseInventory(inboundOrderDetailInfoDTO.getSku(),inboundOrderDetailInfoDTO.getQuantity())) {
//                return ResponseEntity.ok(AjaxResult.success("库存修改成功"));
//            }
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(AjaxResult.error("库存修改失败"));
//        }
//
//    /**
//     * 减少单个库存单的数量(出库时调用)
//     * @return
//     */
//    @PostMapping("/decrease")
//    public ResponseEntity<AjaxResult> decreaseQuantity(@RequestBody InboundOrderDetailInfoDTO inboundOrderDetailDTO ) {
//        if (inventoryService.decreaseInventory(inboundOrderDetailDTO.getSku(),inboundOrderDetailDTO.getQuantity())) {
//            return ResponseEntity.ok(AjaxResult.success("库存修改成功"));
//        }
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(AjaxResult.error("库存修改失败"));
//    }


    /**
     * 添加库存记录
     *
     * <p>创建新的库存记录，需满足：
     * <ul>
     *   <li>SKU不能重复</li>
     *   <li>数量必须有效</li>
     * </ul>
     *
     * @param inventoryDTO 库存数据，包含：
     *                     <ul>
     *                       <li>sku - 商品唯一标识</li>
     *                       <li>quantity - 库存数量</li>
     *                     </ul>
     * @return 响应实体，包含：
     *         <ul>
     *           <li>成功：状态码200+空数据</li>
     *           <li>失败：状态码400+错误原因</li>
     *         </ul>
     */
    @PostMapping("/add")
    @PreAuthorize("hasRole('操作员') or hasRole('管理员')")
    public ResponseEntity<AjaxResult> addInventory(@RequestBody InventoryDTO inventoryDTO){
        Inventory inventory = inventoryService.getBySku(inventoryDTO.getSku());
        if (inventory!=null){
            //库存单号重复了
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error("该种货物已存在"));
        }else {
            inventory = new Inventory(null,inventoryDTO.getSku(),inventoryDTO.getQuantity());
            Boolean b = inventoryService.addOneInventory(inventory);
            if (b){
                return ResponseEntity.ok().body(AjaxResult.success());
            }else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error("添加失败"));
            }
        }
    }

    /**
     * 删除库存记录
     *
     * <p>根据SKU删除指定的库存记录
     *
     * @param sku 包含SKU的Map，格式：
     *            {
     *                "sku": "商品SKU"
     *            }
     * @return 响应实体，包含：
     *         <ul>
     *           <li>成功：状态码200+空数据</li>
     *           <li>失败：状态码400+错误原因</li>
     *         </ul>
     */
    @PostMapping("/delete")
    @PreAuthorize("hasRole('操作员') or hasRole('管理员')")
    public ResponseEntity<AjaxResult> deleteInventoryBySku(@RequestBody Map<String,String> sku){
        Inventory inventory = inventoryService.getBySku(sku.get("sku"));
        if (inventory==null){
            //未查找到对应的库存
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error("不存在该种库存"));
        }else {
            Boolean b = inventoryService.deleteBySku(sku.get("sku"));
            if (b){
                return ResponseEntity.ok().body(AjaxResult.success());
            }else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error("删除失败"));
            }
        }
    }



    /**
     * 分页查询库存记录
     *
     * <p>获取分页的库存列表，包含以下特性：
     * <ol>
     *   <li>页码有效性验证</li>
     *   <li>自动计算分页导航信息</li>
     *   <li>数据转换为VO对象</li>
     * </ol>
     *
     * @param pageNo 当前页码（从1开始）
     * @return 响应实体，包含：
     *         <ul>
     *           <li>成功：状态码200+分页数据(PageVo&lt;InventoryInfoVo&gt;)</li>
     *           <li>失败：状态码400+错误信息</li>
     *         </ul>
     * @see PageVo 分页数据包装类
     * @see InventoryInfoVo 库存信息VO
     */
    @GetMapping("/inventories/{pageNo}")
    @PreAuthorize("hasRole('操作员') or hasRole('管理员')")
    public ResponseEntity<AjaxResult> listInventoryByPage(@PathVariable int pageNo) {
        if (pageNo<=0){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error("页数小于1"));
        }
        System.out.println("请求的页数pageNo: "+pageNo);
        Page<Inventory> page = inventoryService.page(new Page<>(pageNo, Constants.PageSize));//一页显示10条数据
        System.out.println("获得的数据page: "+page);
        System.out.println("\n总页数getPages: "+page.getPages());
        /**
         * pageNo大于总页数时，getCurrent() 等于 pageNo,
         */
        System.out.println("\n当前页数getCurrent: "+page.getCurrent());
        System.out.println("\n库存单总数量getTotal: "+page.getTotal());
        if (page.getPages()==0){
            //无库存
            return ResponseEntity.ok().body(AjaxResult.success("当前库存为空",null));
        }
        if (pageNo>page.getPages()){
            //大于最大页数
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error("页数大于最大页数"));
        }
        //当前页数nowPage（大于等于1，小于等于 最大页数）
        int nowPage= (int) page.getCurrent();
        //若当前页数为第一页，则它的上一页（beforePage）设为 -1；
        int beforePage= nowPage==1?-1:nowPage-1;
        //若当前页数为最后一页，则它的下一页(nextPage)设为 -1；
        int nextPage= nowPage==(int)page.getPages()?-1:nowPage+1;
        //List<Inventory>转为List<InventoryPageVo>
        List<Inventory> inventories = page.getRecords();
        List<InventoryInfoVo> inventoryInfoVos = inventories.stream().map(inventory -> new InventoryInfoVo(
                inventory.getSku(),
                inventory.getQuantity()
        )).toList();

        PageVo<InventoryInfoVo> inventoryPageVo = new PageVo<InventoryInfoVo>(nowPage, beforePage, nextPage, (int) page.getPages(), inventoryInfoVos);
        System.out.println("InventoryPageVo: "+inventoryPageVo);
        return ResponseEntity.ok().body(AjaxResult.success(inventoryPageVo));
    }
}
