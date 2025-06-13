package com.xhz.yuncang.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xhz.yuncang.dto.*;
import com.xhz.yuncang.entity.Inventory;
import com.xhz.yuncang.entity.Product;
import com.xhz.yuncang.entity.Remind;
import com.xhz.yuncang.entity.User;
import com.xhz.yuncang.service.ProductService;
import com.xhz.yuncang.service.InventoryService;
import com.xhz.yuncang.service.RemindService;
import com.xhz.yuncang.utils.AjaxResult;
import com.xhz.yuncang.utils.Constants;
import com.xhz.yuncang.utils.MD5Utils;
import com.xhz.yuncang.utils.UserHolder;
import com.xhz.yuncang.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
/**
 * 商品管理控制器
 *
 * <p>负责处理商品信息的全生命周期管理，提供以下核心功能：
 * <ul>
 *   <li>商品信息的分页查询（消费者/管理员视图）</li>
 *   <li>商品的添加、修改和删除</li>
 *   <li>商品与库存的联动管理</li>
 * </ul>
 *
 * <p>业务规则：
 * <ul>
 *   <li>商品名称必须唯一</li>
 *   <li>删除商品需检查库存数量</li>
 *   <li>分页查询需验证页码有效性</li>
 * </ul>
 * @author bls
 * @see ProductService 商品服务接口
 * @see InventoryService 库存服务接口
 */
@RestController
public class ProductController {
    /**
     * 商品服务实例
     */
    @Autowired
    private ProductService productService;

    /**
     * 库存服务实例
     */
    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private RemindService remindService;
    /**
     * 分页查询商品信息（消费者视图）
     *
     * <p>获取分页的商品列表，支持按名称模糊查询，包含以下处理：
     * <ol>
     *   <li>验证页码有效性</li>
     *   <li>执行分页查询</li>
     *   <li>构建分页导航信息</li>
     *   <li>转换数据为ProductInfoVo</li>
     * </ol>
     *
     * @param current 当前页码（从1开始，默认1）
     * @param pageSize 每页记录数（默认10）
     * @param name 商品名称模糊查询条件
     * @return 响应实体，包含：
     *         <ul>
     *           <li>成功：状态码200+分页数据</li>
     *           <li>失败：状态码400+错误信息</li>
     *         </ul>
     * @see ProductInfoVo 商品信息VO（消费者视图）
     */
    @GetMapping("/product")
    public ResponseEntity<AjaxResult> showProductsByPage(
            @RequestParam(defaultValue = "1")int current,
            @RequestParam(defaultValue = "10")int pageSize,
            @RequestParam(defaultValue = "")String name
    ){
        if (current<=0){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error("页数小于1"));
        }
        Page<Product> page = new Page<>(current, pageSize);
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();

        if (!name.isEmpty()) {
            queryWrapper.like("name", name);
        }
        Page<Product> result = productService.page(page, queryWrapper);
        UserVo user = UserHolder.getUser();
        if (result.getPages() == 0) {
            remindService.saveRemind(new Remind(null,Constants.REMIND_INFO,"货物种类为空","于"+ LocalDateTime.now()+",时刻,"+
                    user.getUserType()+user.getUsername()+"发现没有货物，建议添加3中货物",
                    LocalDateTime.now(),"0"));
            return ResponseEntity.ok().body(AjaxResult.success("当前产品为空", ""));
        }
        if (current>result.getPages()){
            //大于最大页数
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"页数大于最大页数"));
        }
        int nowPage = (int) result.getCurrent();
        int beforePage = nowPage == 1 ? -1 : nowPage - 1;
        int nextPage = nowPage == (int) result.getPages() ? -1 : nowPage + 1;

        //List<Product>转为List<ProductListPageVo>
        List<Product> products = result.getRecords();
        List<ProductInfoVo> productInfoVoList = products.stream().map(product -> new ProductInfoVo(
                product.getId().toString(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getWeight(),
                product.getLength(),
                product.getWidth(),
                product.getHeight()
        )).toList();

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("prev", beforePage);
        responseMap.put("next", nextPage);
        responseMap.put("total", result.getTotal());
        responseMap.put("list", productInfoVoList);
        return ResponseEntity.ok().body(AjaxResult.success(responseMap));
    }


    /**
     * 分页查询货物信息（管理员视图）
     *
     * <p>获取分页的货物详细信息，包含以下处理：
     * <ol>
     *   <li>验证页码有效性</li>
     *   <li>执行分页查询</li>
     *   <li>构建分页导航信息</li>
     *   <li>转换数据为GoodsInfoVo</li>
     * </ol>
     *
     * @param pageNo 当前页码（从1开始）
     * @return 响应实体，包含：
     *         <ul>
     *           <li>成功：状态码200+分页数据(PageVo&lt;GoodsInfoVo&gt;)</li>
     *           <li>失败：状态码400+错误信息</li>
     *         </ul>
     * @see GoodsInfoVo 货物信息VO（管理员视图）
     */
    @PreAuthorize("hasRole('管理员') or hasRole('操作员')")
    @GetMapping("/goods/{pageNo}")
    public ResponseEntity<AjaxResult> showGoodsByPage(@PathVariable int pageNo){
        if (pageNo<=0){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error("页数小于1"));
        }
        System.out.println("请求的页数pageNo: "+pageNo);
        Page<Product> page = productService.page(new Page<>(pageNo, Constants.PageSize));//一页显示10条数据
        System.out.println("获得的数据page: "+page);
        System.out.println("\n总页数getPages: "+page.getPages());
        /**
         * pageNo大于总页数时，getCurrent() 等于 pageNo,
         */
        System.out.println("\n当前页数getCurrent: "+page.getCurrent());
        System.out.println("\n用户总数量getTotal: "+page.getTotal());
        if (page.getPages()==0){
            return ResponseEntity.ok().body(AjaxResult.success("当前货物为空",""));
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
        //List<Product>转为List<ProductListPageVo>
        List<Product> products = page.getRecords();
        List<GoodsInfoVo> productInfoVoList = products.stream().map(product -> new GoodsInfoVo(
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getWeight(),
                product.getLength(),
                product.getWidth(),
                product.getHeight()
        )).toList();

        PageVo<GoodsInfoVo> productListPageVo = new PageVo<>(nowPage, beforePage, nextPage, (int) page.getPages(), productInfoVoList);
        System.out.println("productListPageVo: "+productListPageVo);
        return ResponseEntity.ok().body(AjaxResult.success(productListPageVo));
    }



    /**
     * 修改商品信息
     *
     * <p>根据SKU更新商品信息，需满足：
     * <ul>
     *   <li>商品必须存在</li>
     *   <li>商品名称不能重复</li>
     *   <li>所有必填字段必须完整</li>
     * </ul>
     *
     * @param sku 商品SKU（路径参数）
     * @param productInfoDTO 商品更新数据，包含：
     *                       <ul>
     *                         <li>name - 商品名称</li>
     *                         <li>description - 商品描述</li>
     *                         <li>weight - 商品重量</li>
     *                         <li>length/width/height - 商品尺寸</li>
     *                       </ul>
     * @return 响应实体，包含：
     *         <ul>
     *           <li>成功：状态码200+空数据</li>
     *           <li>失败：状态码400/500+错误原因</li>
     *         </ul>
     */
    @PreAuthorize("hasRole('管理员') or hasRole('操作员')")
    @PutMapping("/product/{sku}")
    public ResponseEntity<AjaxResult> updateProduct(@PathVariable("sku") String sku,
                                                    @RequestBody ProductInfoDTO productInfoDTO){
        System.out.println("productInfoDTO: "+productInfoDTO);
        if (productInfoDTO.getName()==null||productInfoDTO.getWeight()==null||productInfoDTO.getWidth()==null||
            productInfoDTO.getLength()==null||productInfoDTO.getHeight()==null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"信息不完整"));
        }
        Product product = productService.findBySku(sku);
        if (product==null){
            //没有该货物
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"货物不存在"));
        }else {
            Product product1 = productService.findByName(productInfoDTO.getName());
            if (product1!=null&& !Objects.equals(productInfoDTO.getName(), product.getName())){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"货物名称相同"));
            }
            Boolean bool = productService.updateProductInfoBySku(sku, productInfoDTO.getName(),
                    productInfoDTO.getDescription(), productInfoDTO.getWeight(), productInfoDTO.getLength(),
                    productInfoDTO.getWidth(), productInfoDTO.getHeight());
            if (bool){
                UserVo user = UserHolder.getUser();
                remindService.saveRemind(new Remind(null,Constants.REMIND_SUCCESS,"成功修改货物信息","于"+ LocalDateTime.now()+",时刻,"+
                        user.getUserType()+user.getUsername()+"成功修改货物信息，现货物名称为"+productInfoDTO.getName(),
                        LocalDateTime.now(),"1"));
                return ResponseEntity.ok().body(AjaxResult.success());
            }else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(AjaxResult.error(500,"修改失败"));
            }
        }
    }

    /**
     * 添加商品
     *
     * <p>创建新商品并初始化库存记录，需满足：
     * <ul>
     *   <li>商品名称必须唯一</li>
     *   <li>所有必填字段必须完整</li>
     *   <li>自动创建关联的库存记录（初始数量为0）</li>
     * </ul>
     *
     * @param productAddDto 商品添加数据，包含：
     *                      <ul>
     *                        <li>sku - 商品编码</li>
     *                        <li>name - 商品名称</li>
     *                        <li>description - 商品描述</li>
     *                        <li>weight - 商品重量</li>
     *                        <li>length/width/height - 商品尺寸</li>
     *                      </ul>
     * @return 响应实体，包含：
     *         <ul>
     *           <li>成功：状态码200+空数据</li>
     *           <li>失败：状态码400/500+错误原因</li>
     *         </ul>
     */
    @PostMapping("/product")
    @PreAuthorize("hasRole('管理员') or hasRole('操作员')")
    public ResponseEntity<AjaxResult> addProduct(@RequestBody ProductAddDTO productAddDto){
        System.out.println("productAddDto: "+productAddDto);
        if (productAddDto.getSku()==null||productAddDto.getName()==null||productAddDto.getWeight()==null||
        productAddDto.getLength()==null||productAddDto.getHeight()==null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error("信息不完整"));
        }
        Product product = productService.findByName(productAddDto.getName());
        if (product!=null){
            //货物名称重了
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error("货物已存在"));
        }else {
            Product newProduct = new Product(null,productAddDto.getName(), productAddDto.getSku(), productAddDto.getDescription(),
                    productAddDto.getWeight(), productAddDto.getLength(), productAddDto.getWidth(), productAddDto.getHeight());
            Boolean b = productService.addOneProduct(newProduct);
            if (b){
                // 在库存表中添加记录，库存数量为 0
                Inventory inventory = new Inventory(null, productAddDto.getSku(), 0L);
                Boolean inventoryAdded = inventoryService.addOneInventory(inventory);
                if (inventoryAdded) {
                    UserVo user = UserHolder.getUser();
                    remindService.saveRemind(new Remind(null,Constants.REMIND_SUCCESS,"成功添加货物","于"+LocalDateTime.now()+",时刻,"+
                            user.getUserType()+user.getUsername()+"成功添加了货物"+productAddDto.getName(),
                            LocalDateTime.now(),"1"));
                    return ResponseEntity.ok().body(AjaxResult.success());
                } else {
                    // 库存添加失败，可考虑回滚产品添加操作
                    productService.deleteBySku(productAddDto.getSku());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(AjaxResult.error(500,"库存添加失败"));
                }
            }else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(AjaxResult.error(500,"添加失败"));
            }
        }
    }

    /**
     * 删除商品
     *
     * <p>根据ID删除商品及关联库存记录，需满足：
     * <ul>
     *   <li>商品必须存在</li>
     *   <li>关联库存数量必须为0</li>
     * </ul>
     *
     * @param id 商品ID（路径参数）
     * @return 响应实体，包含：
     *         <ul>
     *           <li>成功：状态码200+成功消息</li>
     *           <li>失败：状态码400+错误原因</li>
     *         </ul>
     */
    @DeleteMapping("product/{id}")
    @PreAuthorize("hasRole('管理员') or hasRole('操作员')")
    public ResponseEntity<AjaxResult> removeProduct(@PathVariable String id) {
        Product product = productService.findById(id);
        if (product == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"没有该货物"));
        } else {
            // 检查库存数量
            String sku = product.getSku();
            Inventory inventory = inventoryService.getBySku(sku);
            if (inventory != null && inventory.getQuantity() > 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error("库存不为 0，不能删除该货物"));
            }
            //有，删除
            Boolean b = productService.deleteBySku(sku);
            if (b) {
                // 删除库存记录
                Boolean inventoryDeleted = inventoryService.deleteBySku(sku);
                if (inventoryDeleted) {
                    UserVo user = UserHolder.getUser();
                    remindService.saveRemind(new Remind(null,Constants.REMIND_SUCCESS,"成功删除货物","于"+LocalDateTime.now()+",时刻,"+
                            user.getUserType()+user.getUsername()+"成功删除了货物"+product.getName(),
                            LocalDateTime.now(),"1"));
                    return ResponseEntity.ok().body(AjaxResult.success("删除成功"));
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error("库存记录删除失败"));
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error("删除失败"));
            }
        }
    }

    /**
     * 根据SKU批量查询商品（已弃用）
     *
     * <p>注意：该方法未在实际业务中使用
     *
     * @param skus SKU列表
     * @return 响应实体，包含：
     *         <ul>
     *           <li>成功：状态码200+商品列表</li>
     *           <li>失败：状态码400/404+错误信息</li>
     *         </ul>
     * @deprecated 该方法未在实际业务中使用
     */
    @PostMapping("/findBySkus")
    public ResponseEntity<AjaxResult> findProductsBySkus(@RequestBody List<String> skus) {
        try {
            if (skus == null || skus.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(AjaxResult.error(400, "商品编码列表不能为空"));
            }

            List<Product> products = productService.findProductsBySkus(skus);

            if (products.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(AjaxResult.error(404, "未找到匹配的商品记录"));
            }

            // 将 Product 转为 ProductVo
            List<GoodsInfoVo> productVoList = products.stream().map(product -> new GoodsInfoVo(
                    product.getSku(),
                    product.getName(),
                    product.getDescription(),
                    product.getWeight(),
                    product.getLength(),
                    product.getWidth(),
                    product.getHeight()
            )).collect(Collectors.toList());

            return ResponseEntity.ok().body(AjaxResult.success(productVoList));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AjaxResult.error(400, e.getMessage()));
        }
    }
}


