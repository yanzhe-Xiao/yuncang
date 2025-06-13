package com.xhz.yuncang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xhz.yuncang.entity.ShelfInventory;
import com.xhz.yuncang.entity.StorageShelf;
import com.xhz.yuncang.mapper.ShelfInventoryMapper;
import com.xhz.yuncang.mapper.StorageShelfMapper;
import com.xhz.yuncang.service.ShelfInventoryService;
import com.xhz.yuncang.service.StorageShelfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StorageShelfServiceImpl extends ServiceImpl<StorageShelfMapper, StorageShelf>
        implements StorageShelfService {
    @Autowired
    private StorageShelfMapper storageShelfMapper;
    @Autowired
    private ShelfInventoryMapper shelfInventoryMapper;
//    private ShelfInventoryService shelfInventoryService;

    /**
     * 根据货架编号查找货架
     *
     * @param shelfCode 货架编号
     * @return 货架实体
     */
    @Override
    public StorageShelf findByShelfCode(String shelfCode) {
        return lambdaQuery()
                .eq(StorageShelf::getShelfCode, shelfCode)
                .one();
    }

    @Override
    public StorageShelf findById(Long id) {
        return lambdaQuery()
                .eq(StorageShelf::getId,id)
                .one();
    }

    /**
     * 创建新的货架
     *
     * @param storageShelf 货架实体
     * @return 是否创建成功
     */
    @Override
    public boolean createShelf(StorageShelf storageShelf) {
        // 检查货架编号是否已存在
        if (findByShelfCode(storageShelf.getShelfCode()) != null) {
            return false; // 货架编号已存在
        }
        return save(storageShelf);
    }

    /**
     * 更新货架信息
     *
     * @param storageShelf 货架实体
     * @return 是否更新成功
     */
    @Override
    public boolean updateShelf(StorageShelf storageShelf) {
        // 确保货架存在
        if (storageShelf.getShelfCode() == null || findByShelfCode(storageShelf.getShelfCode()) == null) {
            return false; // 货架不存在
        }
        return lambdaUpdate()
                .eq(StorageShelf::getShelfCode,storageShelf.getShelfCode())
                .update(storageShelf);
    }

    /**
     * 删除货架
     *
     * @param code 货架code
     * @return 是否删除成功
     */
    @Override
    public boolean deleteShelf(String code) {
        if(!storageShelfMapper.checkAllOrdersFinished())
            throw new IllegalArgumentException("当前所有订单存在未完成的情况，不可删除");
        if(!storageShelfMapper.isShelfEmptyOrAllZero(code)){
            throw new IllegalArgumentException("当前货架上仍有货物，不可删除");
        }
        if(shelfInventoryMapper.deleteShelfInventoryByShelfCode(code) != 0){
            throw new IllegalArgumentException("在删除货架商品表时发生错误，无法删除");
        }
        return lambdaUpdate()
                .eq(StorageShelf::getShelfCode, code)
                .remove();
    }

    public boolean deleteByShelfCode(String shelfCode){
        return lambdaUpdate()
                .eq(StorageShelf::getShelfCode,shelfCode)
                .remove();
    }

    /**
     * 根据位置查找货架
     *
     * @param locationX X轴位置
     * @param locationY Y轴位置
     * @param locationZ Z轴位置（层数）
     * @return 货架实体
     */
    @Override
    public List<StorageShelf> findByLocation(Double locationX, Double locationY, Double locationZ) {
        return lambdaQuery()
                .eq(locationX != null, StorageShelf::getLocationX, locationX)
                .eq(locationY != null, StorageShelf::getLocationY, locationY)
                .eq(locationZ != null, StorageShelf::getLocationZ, locationZ)
                .list();
    }

    /**
     * 查询所有货架
     *
     * @return 货架实体列表
     */
    @Override
    public List<StorageShelf> findAllShelves() {
        return lambdaQuery().list();
    }

    /**
     * 根据最大载重查询货架（大于指定重量）
     *
     * @param weights 最大载重（单位：kg）
     * @return 货架实体列表
     */
    @Override
    public List<StorageShelf> findByMaxWeights(Double weights) {
        return lambdaQuery()
                .ge(weights != null, StorageShelf::getMaxWeight, weights)
                .list();
    }

    /**
     * 根据尺寸查询货架
     *
     * @param length 长度（单位：m）
     * @param width  宽度（单位：m）
     * @param height 高度（单位：m）
     * @return 货架实体列表
     */
    @Override
    public List<StorageShelf> findBySize(Double length, Double width, Double height) {
        return lambdaQuery()
                .ge(length != null, StorageShelf::getLength, length)
                .ge(width != null, StorageShelf::getWidth, width)
                .ge(height != null, StorageShelf::getHeight, height)
                .list();
    }

    @Override
    public Page<StorageShelf> pageByShelfCode(String shelfCode, Page<StorageShelf> page) {
        QueryWrapper<StorageShelf> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("shelf_code", shelfCode);
        return storageShelfMapper.selectPage(page, queryWrapper);
    }

    @Override
    public Page<StorageShelf> pageByLocation(Double x,Double y,Double z,Page<StorageShelf> page){
        QueryWrapper<StorageShelf> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("location_x",x).eq("location_y", y).eq("location_z",z);
        return storageShelfMapper.selectPage(page, queryWrapper);
//        return null;
    }

    @Override
    public Page<StorageShelf> pageByMaxWeights(Double maxWeight, Page<StorageShelf> page){
        QueryWrapper<StorageShelf> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("max_weight", maxWeight);
        return storageShelfMapper.selectPage(page, queryWrapper);
    }

    @Override
    public Page<StorageShelf> pageBySize(Double length, Double width, Double height, Page<StorageShelf> page){
        QueryWrapper<StorageShelf> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("length", length).ge("width", width).ge("height", height);
        return storageShelfMapper.selectPage(page, queryWrapper);
    }

    @Override
    public Page<StorageShelf> pageAllShelves(Page<StorageShelf> page){
        return storageShelfMapper.selectPage(page, null);
    }

    /**
     * 根据weights获得当前可以承受的所有货架
     * @param weights 所需的最小载重
     * @param page 分页参数
     * @return 分页的StorageShelf记录
     */
    @Override
    public Page<StorageShelf> pageByCurrentWeights(Double weights, Page<StorageShelf> page) {
        if (weights == null || weights < 0) {
            throw new IllegalArgumentException("载重参数无效");
        }
        return storageShelfMapper.selectByCurrentWeights(weights, page);
    }

    @Override
    public Page<StorageShelf> pageByCurrentLength(Double length, Page<StorageShelf> page){
        if (length == null || length < 0) {
            throw new IllegalArgumentException("长度参数无效");
        }
        return storageShelfMapper.selectByCurrentLength(length, page);
    }


}