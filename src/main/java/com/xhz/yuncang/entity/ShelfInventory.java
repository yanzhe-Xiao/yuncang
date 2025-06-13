package com.xhz.yuncang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 货架层与商品关系实体类
 * 用于记录商品在仓库货架中的具体存储位置及数量信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@TableName("shelf_inventory")
public class ShelfInventory {

    /**
     * 数据库主键ID，自动递增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 货架层编号
     * 格式：货架号-层号（如：S001-2 表示第1个货架第2层）
     */
    private String shelfCode;

    /**
     * 商品编码（SKU）
     * 关联商品基础信息表的唯一标识
     */
    private String sku;

    /**
     * 存储数量
     * 该货架层上存放的特定SKU商品的当前数量
     */
    private Long quantity;
}