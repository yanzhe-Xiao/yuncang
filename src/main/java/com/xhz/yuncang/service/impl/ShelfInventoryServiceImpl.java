package com.xhz.yuncang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xhz.yuncang.entity.Product;
import com.xhz.yuncang.entity.ShelfInventory;
import com.xhz.yuncang.entity.StorageShelf;
import com.xhz.yuncang.mapper.ShelfInventoryMapper;
import com.xhz.yuncang.service.ShelfInventoryService;
import com.xhz.yuncang.service.StorageShelfService;
import com.xhz.yuncang.vo.ProductSimpleVo;
import com.xhz.yuncang.vo.shelf.AllShelfInfoVo;
import com.xhz.yuncang.vo.shelf.ProductShelfInventoryVo;
import com.xhz.yuncang.vo.shelf.ShelfInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 货架库存服务实现类
 */
@Service
@Transactional
public class ShelfInventoryServiceImpl extends ServiceImpl<ShelfInventoryMapper, ShelfInventory> implements ShelfInventoryService {

    @Autowired
    private ShelfInventoryMapper shelfInventoryMapper;
    @Autowired
    private ProductServiceImpl productService;
    @Autowired
    private StorageShelfService storageShelfService;
    @Override
    public boolean addShelfInventory(ShelfInventory shelfInventory) {
        String code = shelfInventory.getShelfCode();
        String sku = shelfInventory.getSku();
        // 检查是否已存在记录
        ShelfInventory existing = findByShelfCodeAndSku(shelfInventory.getShelfCode(),shelfInventory.getSku());
        if (existing != null) {
            throw new IllegalArgumentException("该货架编号和商品编码的记录已存在，如要添加请使用update修改数量");
        }
        /////Todo 判断有无该商品 (OK)
        Product bySku = productService.findBySku(shelfInventory.getSku());
        if (bySku == null){
            throw new IllegalArgumentException("不存在该商品");
        }
        //判断有无货架
        StorageShelf shelf = storageShelfService.findByShelfCode(shelfInventory.getShelfCode());
        if(shelf == null){
            throw new IllegalArgumentException("不存在该货架");
        }
        if(shelf.getHeight() < bySku.getHeight() ||shelf.getWidth() < bySku.getWidth()||!isLengthAvailable(code,sku)){
            throw new IllegalArgumentException("该货架尺寸不允许");
        }
        if(!isWeightAvailable(code, sku)){
            throw new IllegalArgumentException("该货物已超过货架能承受的最大重量");
        }
        // 插入新记录
        return this.save(shelfInventory);
    }

    @Override
    public boolean deleteShelfInventory(String shelfCode, String sku) {
        // 校验输入参数
        if (shelfCode == null || sku == null) {
            throw new IllegalArgumentException("货架编号和商品编码不能同时为空");
        }
        // 删除记录
        LambdaQueryWrapper<ShelfInventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShelfInventory::getShelfCode, shelfCode)
                .eq(ShelfInventory::getSku, sku);
        return this.remove(wrapper);
    }

    @Override
    public Integer deleteShelfInventory(String shelfCode){
        QueryWrapper<ShelfInventory> shelfInventoryQueryWrapper = new QueryWrapper<>();
        shelfInventoryQueryWrapper.eq("shelf_code", shelfCode);
        return shelfInventoryMapper.delete(shelfInventoryQueryWrapper);
    }

    @Override
    public boolean updateShelfInventory(ShelfInventory shelfInventory) {
        ShelfInventory existing = findByShelfCodeAndSku(shelfInventory.getShelfCode(),shelfInventory.getSku());
        if (existing == null) {
            throw new IllegalArgumentException("记录不存在，无法更新");
        }
        if(existing.getQuantity().equals(shelfInventory.getQuantity())){
            throw new IllegalArgumentException("数据未改变，不进行更新");
        }
        if(existing.getQuantity() < 0.0){
            throw new IllegalArgumentException("数量不能为负");
        }
        return lambdaUpdate()
                .eq(ShelfInventory::getShelfCode,shelfInventory.getShelfCode())
                .eq(ShelfInventory::getSku,shelfInventory.getSku())
                .update(shelfInventory);
    }

    @Override
    public ShelfInventory findByShelfCodeAndSku(String shelfCode, String sku) {
        // 校验输入参数
        if (shelfCode == null || sku == null) {
            throw new IllegalArgumentException("货架编号和商品编码不能为空");
        }
        return lambdaQuery()
                .eq(ShelfInventory::getShelfCode, shelfCode)
                .eq(ShelfInventory::getSku, sku)
                .one();
    }

    @Override
    public List<ShelfInventory> findAll() {
        // 查询所有货架库存记录
        return this.list();
    }

    @Override
    public List<ShelfInventory> findByShelfCode(String shelfCode) {
        // 校验输入参数
        if (shelfCode == null) {
            throw new IllegalArgumentException("货架编号不能为空");
        }

        // 根据货架编号查询
        LambdaQueryWrapper<ShelfInventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShelfInventory::getShelfCode, shelfCode);
        return this.list(wrapper);
    }

    @Override
    public List<ShelfInventory> findBySku(String sku) {
        // 校验输入参数
        if (sku == null) {
            throw new IllegalArgumentException("商品编码不能为空");
        }

        // 根据商品编码查询
        LambdaQueryWrapper<ShelfInventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShelfInventory::getSku, sku);
        return this.list(wrapper);
    }

    @Override
    public Page<ShelfInventory> pageBySku(String sku, Page<ShelfInventory> page) {
        QueryWrapper<ShelfInventory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sku", sku);
        return shelfInventoryMapper.selectPage(page, queryWrapper);
    }

    @Override
    public Page<ShelfInventory> pageByShelfCode(String shelfCode, Page<ShelfInventory> page) {
        QueryWrapper<ShelfInventory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("shelf_code", shelfCode);
        return shelfInventoryMapper.selectPage(page, queryWrapper);
    }

    public Page<ShelfInventory> pageByShelfCodeAndSku(String shelfCode, String sku, Page<ShelfInventory> page) {
        QueryWrapper<ShelfInventory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("shelf_code", shelfCode).eq("sku", sku);
        return shelfInventoryMapper.selectPage(page, queryWrapper);
    }

    @Override
    public Double getCurrentTotalWeightByShelfCode(String shelfCode) {
        if (shelfCode == null || shelfCode.isEmpty()) {
            throw new IllegalArgumentException("货架编号不能为空");
        }
        // 调用 Mapper 方法获取总重量
        Map<String, Object> result = shelfInventoryMapper.selectTotalWeightByShelfCode(shelfCode);
        // 处理空结果或无匹配记录
        if (result == null || result.get("total_weight") == null) {
            return 0.0;
        }
        // 将 total_weight 转换为 Double
        return ((Number) result.get("total_weight")).doubleValue();
    }

    @Override
    public Boolean isWeightAvailable(String shelfCode,String sku){
        Double currentTotalWeight = getCurrentTotalWeightByShelfCode(shelfCode);
        Product product = productService.findBySku(sku);
        StorageShelf shelf = storageShelfService.findByShelfCode(shelfCode);
        if (currentTotalWeight + product.getWeight() <= shelf.getMaxWeight()){
            return true;
        }
        return false;
    }


    @Override
    public Boolean isLengthAvailable(String shelfCode,String sku){
        if(shelfCode == null || shelfCode.isEmpty()){
            throw new IllegalArgumentException("货架编号不能为空");
        }
        Map<String,Object> result = shelfInventoryMapper.selectTotalLengthByShelfCode(shelfCode);
        if(result == null || result.get("total_length") == null){
            return true;
        }
        Double alreadyLength = ((Number) result.get("total_length")).doubleValue();
        Double length = productService.findBySku(sku).getLength();
        return length + alreadyLength < storageShelfService.findByShelfCode(shelfCode).getLength();
    }

    /**
     * 获取系统中所有记录在案的商品总重量 (基于 inventory 表)。
     */
    @Override
    public Double getAllStorageWeights() {
        // SQL SUM 函数在没有匹配行时可能返回 NULL，这里处理一下，返回 0.0
        return Optional.ofNullable(shelfInventoryMapper.sumTotalInventoryWeight()).orElse(0.0);
    }

    /**
     * 获取当前所有货架上存放的商品总重量。
     */
    @Override
    public Double getCurrentStorageWeights() {
        return Optional.ofNullable(shelfInventoryMapper.sumCurrentShelfInventoryWeight()).orElse(0.0);
    }

    /**
     * 获取当前货架上各商品的详情列表 (SKU, 名称, 货架总数量)。
     */
    @Override
    public List<ProductSimpleVo> getCurrentProductsDetails() {
        return shelfInventoryMapper.findCurrentShelfProductDetails();
    }

    @Override
    public List<ShelfInfoVo> getAllShelves() {
        return shelfInventoryMapper.getAllShelfVisualizationData();
    }

    @Override
    public IPage<ProductShelfInventoryVo> getAggregatedShelfInventory(Page<ProductShelfInventoryVo> page, String name) {
        // baseMapper 是 ServiceImpl 中注入的 ProductMapper 实例
        return baseMapper.getPaginatedProductShelfInventory(page, name);
    }

}
