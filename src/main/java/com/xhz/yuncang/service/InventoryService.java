package com.xhz.yuncang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xhz.yuncang.entity.InboundOrder;
import com.xhz.yuncang.entity.Inventory;

import java.util.List;

public interface InventoryService extends IService<Inventory> {
    /**
     * 根据SKU查询库存记录
     * @param sku 商品SKU
     * @return 库存实体
     */
    Inventory getBySku(String sku);

    /**
     * 根据SKU删除库存记录
     * @param sku 商品SKU
     * @return 是否删除成功
     */
    boolean deleteBySku(String sku);

    /**
     * 增加某种货物数量
     * @param sku
     * @param newQuantity
     * @return
     */
    boolean increaseInventory(String sku, Long newQuantity);

    /**
     * 减少某种货物数量
     * @param sku
     * @param newQuantity
     * @return
     */
    boolean decreaseInventory(String sku, Long newQuantity);

    /**
     * 批量查询SKU对应的库存记录
     * @param skuList SKU列表
     * @return 库存记录列表
     */
    List<Inventory> listBySkus(List<String> skuList);

    List<Inventory> listAll();

    /**
     * 新增库存记录
     * @param inventory 库存实体
     * @return 是否新增成功
     */
    Boolean addOneInventory(Inventory inventory);
}