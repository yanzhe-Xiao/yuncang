package com.xhz.yuncang.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xhz.yuncang.entity.ShelfInventory;
import com.xhz.yuncang.entity.StorageShelf;

import java.util.List;
import java.util.stream.DoubleStream;

public interface StorageShelfService extends IService<StorageShelf> {
    StorageShelf findByShelfCode(String shelfCode);

    StorageShelf findById(Long id);

    boolean createShelf(StorageShelf storageShelf);

    boolean updateShelf(StorageShelf storageShelf);

    boolean deleteShelf(String code);

    boolean deleteByShelfCode(String shelfCode);

    List<StorageShelf> findByLocation(Double locationX, Double locationY, Double locationZ);

    List<StorageShelf> findAllShelves();
    List<StorageShelf> findByMaxWeights(Double weights);
    List<StorageShelf> findBySize(Double length,Double width,Double height);

    /**
     * 根据shelfcode分页
     * @param shelfCode
     * @param page
     * @return
     */
    Page<StorageShelf> pageByShelfCode(String shelfCode, Page<StorageShelf> page);

    /**
     * 根据位置进行分页
     * @param locationX
     * @param locationY
     * @param locationZ
     * @param page
     * @return
     */
    Page<StorageShelf> pageByLocation(Double locationX, Double locationY, Double locationZ, Page<StorageShelf> page);

    /**
     * 对所有的货架进行分页
     * @param page
     * @return
     */
    Page<StorageShelf> pageAllShelves(Page<StorageShelf> page);

    /**
     * 根据最大载重量进行分页
     * @param weights
     * @param page
     * @return
     */
    Page<StorageShelf> pageByMaxWeights(Double weights, Page<StorageShelf> page);

    /**
     * 根据尺寸进行分页
     * @param length
     * @param width
     * @param height
     * @param page
     * @return
     */
    Page<StorageShelf> pageBySize(Double length, Double width, Double height, Page<StorageShelf> page);

    /**
     * 根据weights获得当前可以承受的所有货架
     * @param weights
     * @param page
     * @return
     */
    Page<StorageShelf> pageByCurrentWeights(Double weights, Page<StorageShelf> page);

    /**
     * 根据length获得当前可以承受的所有货架
     * @param length
     * @param page
     * @return
     */
    Page<StorageShelf> pageByCurrentLength(Double length, Page<StorageShelf> page);
}