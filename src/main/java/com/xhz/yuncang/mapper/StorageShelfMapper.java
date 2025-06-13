package com.xhz.yuncang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xhz.yuncang.entity.StorageShelf;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface StorageShelfMapper extends BaseMapper<StorageShelf> {
    /**
     * 查找对于参数weights下，当前可以容纳该重量的货架
     * @param weights
     * @param page
     * @return
     */
    @Select("SELECT ss.* " +
            "FROM storage_shelf ss " +
            "LEFT JOIN ( " +
            "    SELECT si.shelf_code, SUM(si.quantity * p.weight) as current_weight " +
            "    FROM shelf_inventory si " +
            "    JOIN product p ON si.sku = p.sku " +
            "    GROUP BY si.shelf_code " +
            ") cw ON ss.shelf_code = cw.shelf_code " +
            "WHERE ss.max_weight >= #{weights} + COALESCE(cw.current_weight, 0)")
    Page<StorageShelf> selectByCurrentWeights(@Param("weights") Double weights, Page<StorageShelf> page);

    /**
     * 查找对于当前长度下，可以容纳该货物的货架
     * @param length
     * @param page
     * @return
     */
    @Select("SELECT ss.* " +
            "FROM storage_shelf ss " +
            "LEFT JOIN ( " +
            "    SELECT si.shelf_code, SUM(si.quantity * p.length) as current_length " +
            "    FROM shelf_inventory si " +
            "    JOIN product p ON si.sku = p.sku " +
            "    GROUP BY si.shelf_code " +
            ") cw ON ss.shelf_code = cw.shelf_code " +
            "WHERE ss.max_weight >= #{length} + COALESCE(cw.current_length, 0)")
    Page<StorageShelf> selectByCurrentLength(@Param("length") Double length, Page<StorageShelf> page);

    /**
     * 判断当前所有货架的状态是否都是已完成
     * @return
     */
    @Select("""
        SELECT 
            (
                (NOT EXISTS (SELECT 1 FROM outbound_order) OR 
                 NOT EXISTS (SELECT 1 FROM outbound_order WHERE status != '已完成'))
                AND
                (NOT EXISTS (SELECT 1 FROM inbound_order) OR 
                 NOT EXISTS (SELECT 1 FROM inbound_order WHERE status != '已完成'))
            ) AS all_finished
        """)
    boolean checkAllOrdersFinished();

    /**
     * 判断当前货架上的商品是否为空
     * @param shelfCode
     * @return
     */
    @Select("""
        SELECT 
            (
                NOT EXISTS (SELECT 1 FROM shelf_inventory WHERE shelf_code = #{shelfCode})
                OR
                NOT EXISTS (SELECT 1 FROM shelf_inventory WHERE shelf_code = #{shelfCode} AND quantity != 0)
            ) AS all_zero_or_empty
        """)
    boolean isShelfEmptyOrAllZero(@Param("shelfCode") String shelfCode);

}