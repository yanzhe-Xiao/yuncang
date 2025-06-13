package com.xhz.yuncang.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xhz.yuncang.entity.ShelfInventory;
import com.xhz.yuncang.vo.ProductSimpleVo;
import com.xhz.yuncang.vo.shelf.ProductShelfInventoryVo;
import com.xhz.yuncang.vo.shelf.ShelfInfoVo;

import java.util.List;

/**
 * 货架库存服务接口
 */
public interface ShelfInventoryService extends IService<ShelfInventory> {

    /**
     * 新增货架库存记录
     * @param shelfInventory 货架库存实体
     */
    boolean addShelfInventory(ShelfInventory shelfInventory);

    /**
     * 删除货架库存记录
     * @param shelfCode 货架编号
     * @param sku 商品编码
     */
    boolean deleteShelfInventory(String shelfCode, String sku);

    /**
     * 删除该货架的所有库存记录
     * @param shelfCode
     */
    Integer deleteShelfInventory(String shelfCode);

    /**
     * 修改货架库存记录
     *
     * @param shelfInventory 货架库存实体
     * @return
     */
    boolean updateShelfInventory(ShelfInventory shelfInventory);

    /**
     * 根据货架编号和商品编码查询货架库存记录
     * @param shelfCode 货架编号
     * @param sku 商品编码
     * @return 货架库存实体
     */
    ShelfInventory findByShelfCodeAndSku(String shelfCode, String sku);

    /**
     * 查询所有货架库存记录
     * @return 货架库存列表
     */
    List<ShelfInventory> findAll();

    /**
     * 根据货架编号查询货架库存记录
     * @param shelfCode 货架编号
     * @return 货架库存列表
     */
    List<ShelfInventory> findByShelfCode(String shelfCode);

    /**
     * 根据商品编码查询货架库存记录
     * @param sku 商品编码
     * @return 货架库存列表
     */
    List<ShelfInventory> findBySku(String sku);

    /**
     * 根据sku来进行分页
     * @param sku
     * @param page
     * @return
     */
    Page<ShelfInventory> pageBySku(String sku, Page<ShelfInventory> page);

    /**
     * 根据shelfCode进行分页
     * @param shelfCode
     * @param page
     * @return
     */
    Page<ShelfInventory> pageByShelfCode(String shelfCode, Page<ShelfInventory> page);

    /**
     * 根据shelfCode和sku进行分页
     * @param shelfCode
     * @param sku
     * @param page
     * @return
     */
    Page<ShelfInventory> pageByShelfCodeAndSku(String shelfCode, String sku, Page<ShelfInventory> page);

    /**
     * 根据货架编号查询当前总重量
     * @param shelfCode 当前查询的货架编码
     * @return 返回当前货架上的总重量
     */
    Double getCurrentTotalWeightByShelfCode(String shelfCode);

    /**
     * 判断当前货架是否可以继续添加该商品
     * @param shelfCode 货架编码
     * @param sku 要添加的商品编码
     * @return
     */
    Boolean isWeightAvailable(String shelfCode,String sku);

    /**
     * 判断当前货架长宽高是否可以继续添加该商品
     * @param shelfCode 货架编码
     * @param sku 要添加的商品编码
     * @return 如果可以返回True，如果不可以返回False
     */
    Boolean isLengthAvailable(String shelfCode,String sku);

    Double getAllStorageWeights();
    Double getCurrentStorageWeights();
    List<ProductSimpleVo> getCurrentProductsDetails();
    List<ShelfInfoVo> getAllShelves();

    IPage<ProductShelfInventoryVo> getAggregatedShelfInventory(Page<ProductShelfInventoryVo> page, String name);


}