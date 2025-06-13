package com.xhz.yuncang.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xhz.yuncang.entity.Product;
import com.xhz.yuncang.entity.ShelfInventory;
import com.xhz.yuncang.service.ShelfInventoryService;
import com.xhz.yuncang.utils.AjaxResult;
import com.xhz.yuncang.utils.Constants;
import com.xhz.yuncang.vo.PageVo;
import com.xhz.yuncang.vo.shelf.ProductShelfInventoryVo;
import com.xhz.yuncang.vo.shelf.ShelfInventoryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 货架库存控制器
 *
 * <p>负责处理货架库存的增删改查操作，提供分页查询和库存汇总功能。
 * 管理货架与商品库存的关联关系，支持按货架编号、商品编码等多条件查询。
 *
 * <p>主要功能：
 * <ul>
 *   <li>货架库存记录的创建、更新和删除</li>
 *   <li>按货架编号、商品编码或组合条件分页查询库存</li>
 *   <li>获取所有货架库存的分页列表</li>
 *   <li>商品在所有货架上的总库存汇总查询</li>
 * </ul>
 *
 * @author yanzhexiao
 * @version 1.0
 * @see ShelfInventoryService 货架库存服务接口
 * @see ShelfInventory 货架库存实体类
 * @see ShelfInventoryVo 货架库存视图对象
 * @see ProductShelfInventoryVo 商品货架库存汇总视图对象
 */
@RestController
@RequestMapping("/shelf-inventory")
@PreAuthorize("hasRole('管理员') or hasRole('操作员')")
public class ShelfInventoryController {

    private final ShelfInventoryService shelfInventoryService;

    @Autowired
    public ShelfInventoryController(ShelfInventoryService shelfInventoryService) {
        this.shelfInventoryService = shelfInventoryService;
    }

    /**
     * 新增货架库存记录
     * @param shelfInventory 货架库存实体
     * @return 操作结果
     */
    @PostMapping("/create")
    public ResponseEntity<AjaxResult> addShelfInventory(@RequestBody ShelfInventory shelfInventory) {
        try {
            shelfInventoryService.addShelfInventory(shelfInventory);
            return ResponseEntity.ok(AjaxResult.success("新增货架库存成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(AjaxResult.error(400, e.getMessage()));
        }
    }

    /**
     * 删除货架库存记录
     * @param shelfInventory 包含货架编号和商品编码的请求体
     * @return 操作结果
     */
    @PostMapping("/remove")
    public ResponseEntity<AjaxResult> removeShelfInventory(@RequestBody Map<String, String> shelfInventory) {
        try {
            String shelfCode = shelfInventory.get("shelfCode");
            String sku = shelfInventory.get("sku");
            ShelfInventory existing = shelfInventoryService.findByShelfCodeAndSku(shelfCode, sku);
            if (existing == null) {
                return ResponseEntity.ok(AjaxResult.error(404, "记录不存在"));
            }
            shelfInventoryService.deleteShelfInventory(shelfCode, sku);
            return ResponseEntity.ok(AjaxResult.success("删除货架库存成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(AjaxResult.error(400, e.getMessage()));
        }
    }

    /**
     * 修改货架库存记录
     * @param shelfInventory 货架库存实体
     * @return 操作结果
     */
    @PutMapping("/update")
    public ResponseEntity<AjaxResult> updateShelfInventory(@RequestBody ShelfInventory shelfInventory) {
        try {
            shelfInventoryService.updateShelfInventory(shelfInventory);
            return ResponseEntity.ok(AjaxResult.success("更新货架库存成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(AjaxResult.error(400, e.getMessage()));
        }
    }

    /**
     * 根据货架编号和商品编码查询货架库存记录（分页）
     * 注意：
     * (1) 支持分页，每页显示 Constants.PageSize 条数据
     * (2) 若页数（pageNo）小于等于0，或大于总页数，返回 400
     * (3) 若当前页为第一页，上一页（beforePage）设为 -1
     * (4) 若当前页为最后一页，下一页（nextPage）设为 -1
     * (5) 返回数据使用 ShelfInventoryVo
     * @param shelfMap 包含货架编号（shelfCode）和商品编码（sku）的请求体
     * @param pageNo 要显示的第几页
     * @return 分页的货架库存记录
     */
    @PostMapping("/findByShelfCodeAndSku/{pageNo}")
    public ResponseEntity<AjaxResult> findByShelfCodeAndSku(@RequestBody Map<String, String> shelfMap, @PathVariable int pageNo) {
        try {
            String shelfCode = shelfMap.get("shelfCode");
            String sku = shelfMap.get("sku");
            if (shelfCode == null || shelfCode.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(AjaxResult.error(400, "货架编号不能为空"));
            }
            if (sku == null || sku.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(AjaxResult.error(400, "商品编码不能为空"));
            }

            if (pageNo <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(AjaxResult.error("页数小于1"));
            }

            Page<ShelfInventory> page = shelfInventoryService.pageByShelfCodeAndSku(shelfCode, sku, new Page<>(pageNo, Constants.PageSize));
            PageVo<ShelfInventoryVo> shelfInventoryPageVo = utilsPagination(page);
            return ResponseEntity.ok().body(AjaxResult.success(shelfInventoryPageVo));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AjaxResult.error(400, e.getMessage()));
        }
    }

    /**
     * 根据货架编号查询货架库存记录（分页）
     * 注意：
     * (1) 支持分页，每页显示 Constants.PageSize 条数据
     * (2) 若页数（pageNo）小于等于0，或大于总页数，返回 400
     * (3) 若当前页为第一页，上一页（beforePage）设为 -1
     * (4) 若当前页为最后一页，下一页（nextPage）设为 -1
     * (5) 返回数据使用 ShelfInventoryVo
     * @param shelfMap 包含货架编号（shelfCode）的请求体
     * @param pageNo 要显示的第几页
     * @return 分页的货架库存记录
     */
    @PostMapping("/findByShelfCode/{pageNo}")
    public ResponseEntity<AjaxResult> findByShelfCode(@RequestBody Map<String, String> shelfMap, @PathVariable int pageNo) {
        try {
            String shelfCode = shelfMap.get("shelfCode");
            if (shelfCode == null || shelfCode.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(AjaxResult.error(400, "货架编号不能为空"));
            }

            if (pageNo <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(AjaxResult.error("页数小于1"));
            }

            Page<ShelfInventory> page = shelfInventoryService.pageByShelfCode(shelfCode, new Page<>(pageNo, Constants.PageSize));
            PageVo<ShelfInventoryVo> shelfInventoryPageVo = utilsPagination(page);
            return ResponseEntity.ok().body(AjaxResult.success(shelfInventoryPageVo));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AjaxResult.error(400, e.getMessage()));
        }
    }


    /**
     * 根据sku查询货架库存记录（分页）
     * 注意：
     * (1) 支持分页，每页显示 Constants.PageSize 条数据
     * (2) 若页数（pageNo）小于等于0，或大于总页数，返回 400
     * (3) 若当前页为第一页，上一页（beforePage）设为 -1
     * (4) 若当前页为最后一页，下一页（nextPage）设为 -1
     * (5) 返回数据使用 ShelfInventoryVo
     * @param shelfMap 包含货架编号（shelfCode）的请求体
     * @param pageNo 要显示的第几页
     * @return 分页的货架库存记录
     */
    @PostMapping("/findBySku/{pageNo}")
    public ResponseEntity<AjaxResult> findBySku(@RequestBody Map<String, String> shelfMap, @PathVariable int pageNo) {
        try {
            String sku = shelfMap.get("sku");
            if (sku == null || sku.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(AjaxResult.error(400, "货架编号不能为空"));
            }

            if (pageNo <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(AjaxResult.error("页数小于1"));
            }

            Page<ShelfInventory> page = shelfInventoryService.pageBySku(sku, new Page<>(pageNo, Constants.PageSize));
            PageVo<ShelfInventoryVo> shelfInventoryPageVo = utilsPagination(page);
            return ResponseEntity.ok().body(AjaxResult.success(shelfInventoryPageVo));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AjaxResult.error(400, e.getMessage()));
        }
    }

    /**
     * 查询所有货架库存记录（分页）
     * 注意：
     * (1) 支持分页，每页显示 Constants.PageSize 条数据
     * (2) 若页数（pageNo）小于等于0，或大于总页数，返回 400
     * (3) 若当前页为第一页，上一页（beforePage）设为 -1
     * (4) 若当前页为最后一页，下一页（nextPage）设为 -1
     * (5) 返回数据使用 ShelfInventoryVo
     * @param pageNo 要显示的第几页
     * @return 分页的货架库存记录
     */
    @GetMapping("/shelfInventory/{pageNo}")
    public ResponseEntity<AjaxResult> findAllByPage(@PathVariable int pageNo) {
        if (pageNo <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AjaxResult.error("页数小于1"));
        }

        Page<ShelfInventory> page = shelfInventoryService.page(new Page<>(pageNo, Constants.PageSize));

        if (pageNo > page.getPages()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AjaxResult.error("页数大于最大页数"));
        }
        PageVo<ShelfInventoryVo> shelfInventoryPageVo = utilsPagination(page);
        return ResponseEntity.ok().body(AjaxResult.success(shelfInventoryPageVo));
    }

    public PageVo<ShelfInventoryVo> utilsPagination(Page<ShelfInventory> page){
        // 当前页
        int nowPage = (int) page.getCurrent();
        // 上一页：第一页时设为 -1
        int beforePage = nowPage == 1 ? -1 : nowPage - 1;
        // 下一页：最后一页时设为 -1
        int nextPage = nowPage == (int) page.getPages() ? -1 : nowPage + 1;

        // 将 ShelfInventory 转为 ShelfInventoryVo
        List<ShelfInventory> shelves = page.getRecords();
        List<ShelfInventoryVo> shelfInventoryVoList = shelves.stream().map(shelf -> new ShelfInventoryVo(
                shelf.getId(),
                shelf.getShelfCode(),
                shelf.getSku(),
                shelf.getQuantity()
        )).toList();

        // 构造分页 VO
        PageVo<ShelfInventoryVo> shelfInventoryPageVo = new PageVo<>(
                nowPage,
                beforePage,
                nextPage,
                (int) page.getPages(),
                shelfInventoryVoList
        );
        return shelfInventoryPageVo;
    }

    /**
     * 分页获取商品在所有货架上的总库存信息，支持按商品名称模糊查询
     * @param current 当前页码，默认为1
     * @param pageSize 每页显示数量，默认为10
     * @param name 商品名称，用于模糊查询，默认为空字符串
     * @return 分页后的商品聚合库存列表及分页信息
     */
    @GetMapping
    // @PreAuthorize("hasRole('USER')")
    public ResponseEntity<AjaxResult> getInventoryShelfSummaryByPage(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "") String name // 商品名称，用于模糊查询
    ) {
        // 1. 参数校验
        if (current <= 0) {
            return ResponseEntity.badRequest().body(AjaxResult.error(400, "当前页码必须大于0"));
        }
        if (pageSize <= 0) {
            return ResponseEntity.badRequest().body(AjaxResult.error(400, "每页数量必须大于0"));
        }

        // 2. 构建分页对象 (注意泛型是 ProductShelfInventoryVo)
        Page<ProductShelfInventoryVo> page = new Page<>(current, pageSize);

        // 3. 执行分页查询 (调用新的Service方法)
        IPage<ProductShelfInventoryVo> resultPage = shelfInventoryService.getAggregatedShelfInventory(page, name);

        // 4. 处理查询结果
        if (resultPage.getRecords().isEmpty()) {
            Map<String, Object> emptyResponseMap = new HashMap<>();
            emptyResponseMap.put("prev", -1);
            emptyResponseMap.put("next", -1);
            emptyResponseMap.put("total", 0L);
            emptyResponseMap.put("list", List.of());
            return ResponseEntity.ok().body(AjaxResult.success("当前条件下库存汇总为空", emptyResponseMap));
        }

        if (current > resultPage.getPages() && resultPage.getPages() > 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AjaxResult.error(400, "请求的页码超出最大页数"));
        }

        // 5. 数据已是VO格式，直接从resultPage获取
        List<ProductShelfInventoryVo> inventoryVoList = resultPage.getRecords();

        // 6. 构建响应 Map
        Map<String, Object> responseMap = new HashMap<>();
        long currentPageNum = resultPage.getCurrent();
        long totalPages = resultPage.getPages();

        responseMap.put("prev", currentPageNum <= 1 ? -1 : currentPageNum - 1);
        responseMap.put("next", currentPageNum >= totalPages ? -1 : currentPageNum + 1);
        responseMap.put("total", resultPage.getTotal()); // 总记录数
        responseMap.put("list", inventoryVoList);     // VO列表

        return ResponseEntity.ok().body(AjaxResult.success(responseMap));
    }


}