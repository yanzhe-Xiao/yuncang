package com.xhz.yuncang.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xhz.yuncang.entity.ShelfInventory;
import com.xhz.yuncang.entity.StorageShelf;
import com.xhz.yuncang.service.ShelfInventoryService;
import com.xhz.yuncang.service.StorageShelfService;
import com.xhz.yuncang.utils.AjaxResult;
import com.xhz.yuncang.utils.Constants;
import com.xhz.yuncang.vo.PageVo;
import com.xhz.yuncang.vo.shelf.StorageShelfVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
/**
 * 仓储货架控制器
 *
 * <p>负责处理仓储货架的创建、删除、更新和查询操作，提供多维度的货架查询功能。
 * 管理货架的基本信息及空间位置关系，支持按货架编号、位置、尺寸和承重等多条件查询。
 *
 * <p>主要功能：
 * <ul>
 *   <li>货架的创建、更新和删除</li>
 *   <li>按货架编号、位置坐标、尺寸和承重能力查询货架</li>
 *   <li>分页获取所有货架信息</li>
 *   <li>货架状态检查（是否为空）</li>
 * </ul>
 *
 * @author yanzheXiao
 * @version 1.0
 * @see StorageShelfService 货架服务接口
 * @see StorageShelf 货架实体类
 * @see StorageShelfVo 货架视图对象
 * @see ShelfInventoryService 货架库存服务接口
 */
@RestController
//@RequestMapping("/shelf")
@PreAuthorize("hasRole('管理员') or hasRole('操作员')")
public class StorageShelfController {
    /**
     * 货架服务实例
     */
    private final StorageShelfService storageShelfService;
    /**
     * 构造函数，依赖注入货架服务
     *
     * @param storageShelfService 货架服务实现
     */
    @Autowired
    public StorageShelfController(StorageShelfService storageShelfService) {
        this.storageShelfService = storageShelfService;
    }
    /**
     * 货架库存服务实例
     */
    @Autowired
    private ShelfInventoryService shelfInventoryService;

    /**
     * 创建货架
     *
     * <p>创建多层货架结构，根据locationZ参数自动创建指定层数的货架，每层货架编号格式为"基础编号_层数"。
     *
     * <p>可能的错误情况：
     * <ul>
     *   <li>货架信息不完整（缺少必要字段）</li>
     *   <li>货架编号已存在</li>
     *   <li>locationZ参数无效</li>
     * </ul>
     *
     * @param storageShelf 货架实体，必须包含：
     *                     <ul>
     *                       <li>shelfCode - 基础货架编号</li>
     *                       <li>maxWeight - 最大承重</li>
     *                       <li>locationZ - 货架层数</li>
     *                       <li>其他尺寸和位置信息</li>
     *                     </ul>
     * @return 操作结果响应实体，包含：
     *         <ul>
     *           <li>成功状态和消息</li>
     *           <li>或错误状态和详细错误信息</li>
     *         </ul>
     */
    @PostMapping("/shelf")
    public ResponseEntity<AjaxResult> createShelf(@RequestBody StorageShelf storageShelf) {
        System.out.println("storageShelf: "+storageShelf);
        if (storageShelf==null||storageShelf.getShelfCode()==null||storageShelf.getMaxWeight()==null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"信息不全"));
        }
        StorageShelf shelfLayer = storageShelfService.findByShelfCode(storageShelf.getShelfCode() + "_1");
        if (shelfLayer==null){
            //没有冲突可以添加
            for (int i=0;i<storageShelf.getLocationZ();i++){
                StorageShelf shelf = new StorageShelf(null,storageShelf.getShelfCode()+"_"+(i+1),storageShelf.getMaxWeight(),
                        storageShelf.getLength(),storageShelf.getWidth(),storageShelf.getHeight(), storageShelf.getLocationX(),
                        storageShelf.getLocationY(), (double)(i+1));
                storageShelfService.createShelf(shelf);
            }
            return ResponseEntity.ok(AjaxResult.success("创建成功"));
        }else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"货架信息重复"));
        }
    }

    /**
     * 删除货架
     *
     * <p>删除指定ID的货架及其所有层级（如：A_1, A_2等），仅当货架所有层级为空时允许删除。
     *
     * <p>可能的错误情况：
     * <ul>
     *   <li>货架不存在</li>
     *   <li>货架上有物品</li>
     *   <li>货架关联未完成订单</li>
     * </ul>
     *
     * @param id 要删除的货架ID
     * @return 操作结果响应实体，包含：
     *         <ul>
     *           <li>成功状态和消息</li>
     *           <li>或错误状态和详细错误信息</li>
     *         </ul>
     */
    @DeleteMapping("/shelf/{id}")
    public ResponseEntity<AjaxResult> removeShelf(@PathVariable Long id) {
        //FIXME 增加当前货架上是否有物品且当前所有订单已完成(FINISHED 5/29/10:42)
        StorageShelf shelf = storageShelfService.findById(id);
        if (shelf==null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"没有该货架"));
        }
        //有
        //判断是否为空
        String shelfCode = shelf.getShelfCode();
        int lastUnderscoreIndex = shelfCode.lastIndexOf('_');
        String result = shelfCode.substring(0, lastUnderscoreIndex);
        for (int i=1;i<=10;i++){
            List<ShelfInventory> shelfCode1 = shelfInventoryService.findByShelfCode(result + "_" + i);
            if (!shelfCode1.isEmpty()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"货架上有物品，不可删除"));
            }else {
                System.out.println(shelfCode1);
//                break;
            }
        }
        //没有物品，可以删除
        for (int i=0;;i++){
            StorageShelf byShelfCode = storageShelfService.findByShelfCode(result + "_" + (i + 1));
            if (byShelfCode!=null){
                storageShelfService.deleteByShelfCode(byShelfCode.getShelfCode());
            }else {
                break;
            }
        }
        return ResponseEntity.ok(AjaxResult.success("删除成功"));
    }

    /**
     * 更新货架信息
     *
     * <p>更新指定货架及其所有层级的信息，仅当货架所有层级为空时允许更新。
     *
     * <p>可能的错误情况：
     * <ul>
     *   <li>货架信息不完整</li>
     *   <li>货架不存在</li>
     *   <li>货架上有物品</li>
     * </ul>
     *
     * @param id 要更新的货架ID
     * @param storageShelf 包含更新信息的货架实体
     * @return 操作结果响应实体，包含：
     *         <ul>
     *           <li>成功状态和消息</li>
     *           <li>或错误状态和详细错误信息</li>
     *         </ul>
     */
    @PutMapping("/shelf/{id}")
    public ResponseEntity<AjaxResult> updateShelf(
            @PathVariable Long id,
            @RequestBody StorageShelf storageShelf) {
        if (storageShelf==null||storageShelf.getShelfCode()==null||storageShelf.getMaxWeight()==null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"货架信息不全"));
        }
        StorageShelf shelfLayer = storageShelfService.findById(id);
        if (shelfLayer==null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"没有该货架"));
        }
        //只有货架为空，才可更新
        String shelfCode = shelfLayer.getShelfCode();
        int lastUnderscoreIndex = shelfCode.lastIndexOf('_');
        String result = shelfCode.substring(0, lastUnderscoreIndex);
        for (int i=1;i<=10;i++){
            List<ShelfInventory> shelfCode1 = shelfInventoryService.findByShelfCode(result + "_" + i);
            if (!shelfCode1.isEmpty()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"货架上有物品，不可更改"));
            }else {
                System.out.println(shelfCode1);
//                break;
            }
        }
        //空的货架
        for (int i=1;;i++){
            StorageShelf shelf = storageShelfService.findByShelfCode(result + "_" + i);
            if (shelf!=null){
                shelf.setLocationX(storageShelf.getLocationX());
                shelf.setLocationY(storageShelf.getLocationY());
                shelf.setMaxWeight(storageShelf.getMaxWeight());
                shelf.setWidth(storageShelf.getWidth());
                shelf.setLength(storageShelf.getLength());
                shelf.setHeight(storageShelf.getHeight());
                System.out.println("shelf: "+shelf);
                storageShelfService.updateShelf(shelf);
            }else {
                return ResponseEntity.ok(AjaxResult.success("修改成功"));
            }
        }
    }

    /**
     * 根据货架编号查询货架（分页）
     *
     * <p>按货架编号模糊查询货架信息，支持分页。
     *
     * <p>响应数据结构：
     * <ul>
     *   <li>prev - 上一页页码(若无则为-1)</li>
     *   <li>next - 下一页页码(若无则为-1)</li>
     *   <li>total - 总记录数</li>
     *   <li>list - 当前页数据列表，包含：
     *     <ul>
     *       <li>id - 货架ID</li>
     *       <li>shelfCode - 货架编号</li>
     *       <li>locationX/Y/Z - 货架坐标</li>
     *       <li>maxWeight - 最大承重</li>
     *       <li>length/width/height - 货架尺寸</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * @param current 当前页码，默认为1
     * @param pageSize 每页显示数量，默认为10
     * @param shelfCode 货架编号模糊查询条件，默认为空字符串
     * @return 分页响应实体，包含：
     *         <ul>
     *           <li>分页信息</li>
     *           <li>货架数据列表</li>
     *           <li>或错误状态和消息</li>
     *         </ul>
     */
    @GetMapping("/shelf")
    public ResponseEntity<AjaxResult> findByShelfCode(
            @RequestParam(defaultValue = "1")int current,
            @RequestParam(defaultValue = "10")int pageSize,
            @RequestParam(defaultValue = "")String shelfCode
    ) {
        if (current<=0){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error("页数小于1"));
        }
        Page<StorageShelf> page = new Page<>(current, pageSize);
        QueryWrapper<StorageShelf> queryWrapper = new QueryWrapper<>();

        if (!shelfCode.isEmpty()) {
            queryWrapper.like("shelf_code", shelfCode);
        }
        Page<StorageShelf> result = storageShelfService.page(page, queryWrapper);
        if (result.getPages() == 0) {
            return ResponseEntity.ok().body(AjaxResult.success("当前货架为空", ""));
        }
        if (current>result.getPages()){
            //大于最大页数
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"页数大于最大页数"));
        }
        int nowPage = (int) result.getCurrent();
        int beforePage = nowPage == 1 ? -1 : nowPage - 1;
        int nextPage = nowPage == (int) result.getPages() ? -1 : nowPage + 1;


        List<StorageShelf> shelves = result.getRecords();
        List<StorageShelfVo> shelfVoList = shelves.stream().map(shelf -> new StorageShelfVo(
                shelf.getId(),
                shelf.getShelfCode(),
                shelf.getLocationX(),
                shelf.getLocationY(),
                shelf.getLocationZ(),
                shelf.getMaxWeight(),
                shelf.getLength(),
                shelf.getWidth(),
                shelf.getHeight()
        )).toList();

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("prev", beforePage);
        responseMap.put("next", nextPage);
        responseMap.put("total", result.getTotal());
        responseMap.put("list", shelfVoList);
        return ResponseEntity.ok().body(AjaxResult.success(responseMap));
    }

    /**
     * 根据位置查询货架（分页）
     * 注意：
     * (1) 支持分页，每页显示 Constants.PageSize 条数据
     * (2) 若页数（pageNo）小于等于0，或大于总页数，返回 400
     * (3) 若当前页为第一页，上一页（beforePage）设为 -1
     * (4) 若当前页为最后一页，下一页（nextPage）设为 -1
     * (5) 返回数据使用 StorageShelfVo
     * @param payload JSON 请求体，包含 locationX, locationY, locationZ
     * @param pageNo 要显示的第几页
     * @return 分页的货架记录
     */
    @PostMapping("/select/location/{pageNo}")
    public ResponseEntity<AjaxResult> findByLocation(@RequestBody Map<String, Object> payload, @PathVariable int pageNo) {
        try {
            Double locationX = payload.get("locationX") != null ? Double.valueOf(payload.get("locationX").toString()) : null;
            Double locationY = payload.get("locationY") != null ? Double.valueOf(payload.get("locationY").toString()) : null;
            Double locationZ = payload.get("locationZ") != null ? Double.valueOf(payload.get("locationZ").toString()) : null;

            if (pageNo <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(AjaxResult.error(400, "页数小于1"));
            }

            Page<StorageShelf> page = storageShelfService.pageByLocation(locationX, locationY, locationZ, new Page<>(pageNo, Constants.PageSize));

            if (pageNo > page.getPages()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(AjaxResult.error(400, "页数大于最大页数"));
            }
            PageVo<StorageShelfVo> shelfPageVo = utilsPagination(page);
            return ResponseEntity.ok().body(AjaxResult.success(shelfPageVo));
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400, "位置参数格式错误"));
        }
    }

    /**
     * 查询所有货架（分页）
     * 注意：
     * (1) 支持分页，每页显示 Constants.PageSize 条数据
     * (2) 若页数（pageNo）小于等于0，或大于总页数，返回 400
     * (3) 若当前页为第一页，上一页（beforePage）设为 -1
     * (4) 若当前页为最后一页，下一页（nextPage）设为 -1
     * (5) 返回数据使用 StorageShelfVo
     * @param pageNo 要显示的第几页
     * @return 分页的货架记录
     */
    @GetMapping("/select/{pageNo}")
    public ResponseEntity<AjaxResult> findAllShelves(@PathVariable int pageNo) {
        if (pageNo <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AjaxResult.error(400, "页数小于1"));
        }
        Page<StorageShelf> page = storageShelfService.pageAllShelves(new Page<>(pageNo, Constants.PageSize));

        if (pageNo > page.getPages()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AjaxResult.error(400, "页数大于最大页数"));
        }
        PageVo<StorageShelfVo> shelfPageVo = utilsPagination(page);
        return ResponseEntity.ok().body(AjaxResult.success(shelfPageVo));
    }

    /**
     * 根据最大载重查询货架（大于指定重量，分页）
     * 注意：
     * (1) 支持分页，每页显示 Constants.PageSize 条数据
     * (2) 若页数（pageNo）小于等于0，或大于总页数，返回 400
     * (3) 若当前页为第一页，上一页（beforePage）设为 -1
     * (4) 若当前页为最后一页，下一页（nextPage）设为 -1
     * (5) 返回数据使用 StorageShelfVo
     * @param payload JSON 请求体，包含 weights
     * @param pageNo 要显示的第几页
     * @return 分页的货架记录
     */
    @PostMapping("/select/max/weights/{pageNo}")
    public ResponseEntity<AjaxResult> findByMaxWeights(@RequestBody Map<String, Object> payload, @PathVariable int pageNo) {
        try {
            Double weights = payload.get("weights") != null ? Double.valueOf(payload.get("weights").toString()) : null;
            if (weights == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(AjaxResult.error(400, "最大载重不能为空"));
            }
            if (pageNo <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(AjaxResult.error(400, "页数小于1"));
            }

            Page<StorageShelf> page = storageShelfService.pageByMaxWeights(weights, new Page<>(pageNo, Constants.PageSize));

            if (pageNo > page.getPages()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(AjaxResult.error(400, "页数大于最大页数"));
            }
            PageVo<StorageShelfVo> shelfPageVo = utilsPagination(page);
            return ResponseEntity.ok().body(AjaxResult.success(shelfPageVo));
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400, "最大载重参数格式错误"));
        }
    }

    /**
     * 根据尺寸查询货架（分页）
     * 注意：
     * (1) 支持分页，每页显示 Constants.PageSize 条数据
     * (2) 若页数（pageNo）小于等于0，或大于总页数，返回 400
     * (3) 若当前页为第一页，上一页（beforePage）设为 -1
     * (4) 若当前页为最后一页，下一页（nextPage）设为 -1
     * (5) 返回数据使用 StorageShelfVo
     * @param payload JSON 请求体，包含 length, width, height
     * @param pageNo 要显示的第几页
     * @return 分页的货架记录
     */
    @PostMapping("/select/size/{pageNo}")
    public ResponseEntity<AjaxResult> findBySize(@RequestBody Map<String, Object> payload, @PathVariable int pageNo) {
        try {
            Double length = payload.get("length") != null ? Double.valueOf(payload.get("length").toString()) : null;
            Double width = payload.get("width") != null ? Double.valueOf(payload.get("width").toString()) : null;
            Double height = payload.get("height") != null ? Double.valueOf(payload.get("height").toString()) : null;

            if (pageNo <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(AjaxResult.error(400, "页数小于1"));
            }

            Page<StorageShelf> page = storageShelfService.pageBySize(length, width, height, new Page<>(pageNo, Constants.PageSize));

            if (pageNo > page.getPages()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(AjaxResult.error(400, "页数大于最大页数"));
            }
            PageVo<StorageShelfVo> shelfPageVo = utilsPagination(page);
            return ResponseEntity.ok().body(AjaxResult.success(shelfPageVo));
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400, "尺寸参数格式错误"));
        }
    }

    /**
     * 根据weights获得当前可以承受的所有货架（分页）
     * 注意：
     * (1) 支持分页，每页显示 Constants.PageSize 条数据
     * (2) 若页数（pageNo）小于等于0，或大于总页数，返回 400
     * (3) 若当前页为第一页，上一页（beforePage）设为 -1
     * (4) 若当前页为最后一页，下一页（nextPage）设为 -1
     * (5) 返回数据使用 StorageShelfVo
     * @param payload JSON 请求体，包含 weights 和 pageNo
     * @return 分页的货架记录
     */
    @PostMapping("/findByCurrentWeights/{pageNo}")
    public ResponseEntity<AjaxResult> findByCurrentWeights(@RequestBody Map<String, Object> payload,@PathVariable Integer pageNo) {
        try {
            Double weights = payload.get("weights") != null ? Double.valueOf(payload.get("weights").toString()) : null;

            if (weights == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(AjaxResult.error(400, "载重参数不能为空"));
            }
            if (pageNo == null || pageNo <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(AjaxResult.error(400, "页数无效"));
            }
            Page<StorageShelf> page = storageShelfService.pageByCurrentWeights(weights, new Page<>(pageNo, Constants.PageSize));
            if (pageNo > page.getPages()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(AjaxResult.error(400, "页数大于最大页数"));
            }
            PageVo<StorageShelfVo> shelfPageVo = utilsPagination(page);
            return ResponseEntity.ok().body(AjaxResult.success(shelfPageVo));
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AjaxResult.error(400, "载重或页数参数格式错误"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AjaxResult.error(400, e.getMessage()));
        }
    }

    public PageVo<StorageShelfVo> utilsPagination(Page<StorageShelf> page){
        int nowPage = (int) page.getCurrent();
        int beforePage = nowPage == 1 ? -1 : nowPage - 1;
        int nextPage = nowPage == (int) page.getPages() ? -1 : nowPage + 1;

        List<StorageShelf> shelves = page.getRecords();
        List<StorageShelfVo> shelfVoList = shelves.stream().map(shelf -> new StorageShelfVo(
                shelf.getId(),
                shelf.getShelfCode(),
                shelf.getLocationX(),
                shelf.getLocationY(),
                shelf.getLocationZ(),
                shelf.getMaxWeight(),
                shelf.getLength(),
                shelf.getWidth(),
                shelf.getHeight()
        )).collect(Collectors.toList());

        PageVo<StorageShelfVo> shelfPageVo = new PageVo<>(
                nowPage,
                beforePage,
                nextPage,
                (int) page.getPages(),
                shelfVoList
        );
        return shelfPageVo;
    }

}