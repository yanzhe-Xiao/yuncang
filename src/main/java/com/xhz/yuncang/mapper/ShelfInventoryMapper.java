package com.xhz.yuncang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xhz.yuncang.entity.ShelfInventory;
import com.xhz.yuncang.vo.ProductSimpleVo;
import com.xhz.yuncang.vo.shelf.ProductShelfInventoryVo;
import com.xhz.yuncang.vo.shelf.ShelfInfoVo;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface ShelfInventoryMapper extends BaseMapper<ShelfInventory> {
    @Select("SELECT SUM(si.quantity * p.weight) as total_weight " +
            "FROM shelf_inventory si " +
            "JOIN product p ON si.sku = p.sku " +
            "WHERE si.shelf_code = #{shelfCode}")
    Map<String, Object> selectTotalWeightByShelfCode(@Param("shelfCode") String shelfCode);

    @Select("""
            SELECT SUM(si.quantity * p.length) as total_length
            FROM shelf_inventory si
                     JOIN product p ON si.sku = p.sku
            WHERE si.shelf_code = #{shelfCode}""")
    Map<String, Object> selectTotalLengthByShelfCode(@Param("shelfCode") String shelfCode);


    @Select("""
        DELETE FROM shelf_inventory WHERE shelf_code = #{shelfCode}
    """)
    int deleteShelfInventoryByShelfCode(@Param("shelfCode") String shelfCode);

    /**
     * 计算所有库存商品的总重量 (基于 inventory 表)。
     * @return 总重量，如果没有任何记录则返回 null。
     */
    @Select("SELECT SUM(i.max_weight) " +
            "FROM storage_shelf i ")
    Double sumTotalInventoryWeight();

    /**
     * 计算当前所有货架上商品的总重量 (基于 shelf_inventory 表)。
     * @return 当前货架商品总重量，如果没有任何记录则返回 null。
     */
    @Select("SELECT SUM(si.quantity * p.weight) " +
            "FROM shelf_inventory si " +
            "JOIN product p ON si.sku = p.sku")
    Double sumCurrentShelfInventoryWeight();

    /**
     * 获取当前货架上各商品的详情列表 (SKU, 名称, 该商品在所有货架上的总数量)。
     * MyBatis-Plus 通常能自动将下划线命名的列 (如 total_quantity_on_shelves) 映射到驼峰命名的Java属性 (totalQuantityOnShelves)。
     * 如果自动映射失败或需要更复杂的映射逻辑，可以使用 @Results 注解。
     * @return 商品详情列表。
     */
    @Select("SELECT " +
            "    p.name, " +
            "    SUM(si.quantity) AS total_quantity_on_shelves ," + // 使用别名确保映射正确
            "    p.weight "+
            "FROM " +
            "    shelf_inventory si " +
            "JOIN " +
            "    product p ON si.sku = p.sku " +
            "GROUP BY " +
            "    p.sku, p.name " +
            "ORDER BY " +
            "    p.name ASC")
    // 显式指定结果映射
     @Results({
        @Result(property = "productName", column = "name"),
        @Result(property = "productNumber", column = "total_quantity_on_shelves"),
        @Result(property = "productWeight", column = "weight")
     })
    List<ProductSimpleVo> findCurrentShelfProductDetails();

    /**
     * 我现在要所有货架的可视化
     *
     * 货架上存的东西
     *
     * @return
     */
    @Select("SELECT " +
            "   ss.location_x AS shelf_x, " +
            "   ss.location_y AS shelf_y, " +
            "   ss.location_z AS shelf_z, " +
            "   ss.shelf_code AS shelf_code, " +
            "   ss.max_weight AS max_weight, " +
            "   si.sku AS sku, " +
            "   p.name AS name, " +
            "   si.quantity AS quantity, " +
            "   (si.quantity * p.weight) AS weight " +
            "FROM " +
            "   storage_shelf ss " +
            "LEFT JOIN " + // 使用 LEFT JOIN 确保即使货架为空也显示货架信息
            "   shelf_inventory si ON ss.shelf_code = si.shelf_code " +
            "LEFT JOIN " +
            "   product p ON si.sku = p.sku")
    @Results({
            @Result(property = "shelfX", column = "shelf_x"),
            @Result(property = "shelfY", column = "shelf_y"),
            @Result(property = "shelfZ", column = "shelf_z"),
            @Result(property = "shelfCode", column = "shelf_code"),
            @Result(property = "maxWeight", column = "max_weight"),
            @Result(property = "sku", column = "sku"),
            @Result(property = "name", column = "name"),
            @Result(property = "quantity", column = "quantity"),
            @Result(property = "weight", column = "weight")
    })
    List<ShelfInfoVo> getAllShelfVisualizationData();

    /**
     * 分页查询商品及其在货架上的总库存信息
     *
     * @param page 分页对象，MyBatis-Plus会自动处理
     * @param name 商品名称，用于模糊查询（可选）
     * @return 分页后的商品库存视图对象列表
     */
    // 重要：在注解中使用动态SQL，必须用 <script> 标签包裹
    @Select({
            "<script>",
            "SELECT ",
            "   p.id AS id, ",
            "   p.name AS productName, ",
            // 核心修改：使用 SUM() 聚合总数量，并用 COALESCE 处理 null 值
            "   COALESCE(SUM(si.quantity), 0) AS productNumber, ",
            // 计算总重量，同样使用 COALESCE 处理
            "   COALESCE(SUM(si.quantity * p.weight), 0.00) AS productWeight ",
            "FROM ",
            "   product p ",
            "LEFT JOIN ",
            "   shelf_inventory si ON p.sku = si.sku ",
            // 使用 <where> 标签来智能处理 AND 前缀
            "<where> ",
            // 注意：Java字符串中包含双引号需要转义，即 "" -> \"\"
            "   <if test='name != null and name != \"\"'> ",
            "       AND p.name LIKE CONCAT('%', #{name}, '%') ",
            "   </if> ",
            "</where> ",
            "GROUP BY ",
            "   p.id, p.name ", // 按商品ID和名称分组，修复了 only_full_group_by 问题
            "ORDER BY ",
            "   p.id ASC",
            "</script>"
    })
    IPage<ProductShelfInventoryVo> getPaginatedProductShelfInventory(IPage<?> page, @Param("name") String name);
}