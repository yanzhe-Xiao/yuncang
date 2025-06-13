package com.xhz.yuncang.controller;

import com.xhz.yuncang.service.ShelfInventoryService;
import com.xhz.yuncang.utils.AjaxResult;
import com.xhz.yuncang.vo.shelf.AllShelfInfoVo;
import com.xhz.yuncang.vo.shelf.AllStorageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 库存可视化控制器
 *
 * <p>提供仓库库存和货架信息的可视化数据查询功能，用于展示仓库的实时存储状态。
 * 所有接口返回结果统一使用{@link AjaxResult}封装响应数据。
 *
 * <p>主要功能包括：
 * <ul>
 *   <li>获取仓库库存总体信息（总容量、已使用量、货物详情）</li>
 *   <li>获取所有货架的详细信息</li>
 * </ul>
 *
 * @author YANZHE XIAO
 * @version 1.0
 * @since jdk-21
 * @see ShelfInventoryService 货架库存服务接口
 * @see AllStorageVo 库存可视化视图对象
 * @see AllShelfInfoVo 货架信息视图对象
 */
@RestController
@PreAuthorize("hasRole('管理员') or hasRole('操作员')")
//@RequestMapping()
public class AllStorageController {
    /**
     * 货架库存服务，用于获取库存和货架数据
     */
    @Autowired
    private ShelfInventoryService shelfInventoryService;

    /**
     * 获取仓库库存可视化数据
     *
     * <p>返回仓库的总体存储情况，包括：
     * <ul>
     *   <li>库存总承重能力</li>
     *   <li>当前已使用重量</li>
     *   <li>当前存储的货物详情列表（包含货物名称和数量）</li>
     * </ul>
     *
     * @return 包含库存可视化数据的响应实体，数据结构为：
     *         {
     *             "totalWeight": 总承重能力,
     *             "currentWeight": 当前已使用重量,
     *             "details": [
     *                 {
     *                     "name": "货物名称",
     *                     "quantity": 货物数量
     *                 },
     *                 ...
     *             ]
     *         }
     * @see AllStorageVo 库存可视化数据结构
     */
    @GetMapping("/show/storage")
    public ResponseEntity<AjaxResult> getStorage(){
        System.out.println("===========================");
        AllStorageVo allStorageVo = new AllStorageVo(shelfInventoryService.getAllStorageWeights(),
                shelfInventoryService.getCurrentStorageWeights(), shelfInventoryService.getCurrentProductsDetails());
        System.out.println(allStorageVo);
        return ResponseEntity.ok().body(AjaxResult.success(allStorageVo));
    }
    /**
     * 获取所有货架信息
     *
     * <p>返回系统中所有货架的详细信息，包括：
     * <ul>
     *   <li>货架编号</li>
     *   <li>货架位置坐标</li>
     *   <li>货架最大承重</li>
     *   <li>当前存储情况等</li>
     * </ul>
     *
     * @return 包含所有货架信息的响应实体，数据结构为：
     *         {
     *             "shelves": [
     *                 {
     *                     "shelfCode": "货架编号",
     *                     "shelfX": "X坐标",
     *                     "shelfY": "Y坐标",
     *                     "maxWeight": 最大承重,
     *                     ...
     *                 },
     *                 ...
     *             ]
     *         }
     * @see AllShelfInfoVo 货架信息数据结构
     */
    @GetMapping("/shelf/all")
    public ResponseEntity<AjaxResult> getAllShelfInfo(){
        AllShelfInfoVo allShelfInfoVo = new AllShelfInfoVo(shelfInventoryService.getAllShelves());
        System.out.println(allShelfInfoVo);
        return ResponseEntity.ok().body(AjaxResult.success(allShelfInfoVo));
    }

}
