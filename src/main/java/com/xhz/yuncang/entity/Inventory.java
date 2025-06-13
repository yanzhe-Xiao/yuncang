package com.xhz.yuncang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 库存实体类
 * 用于存储仓库中商品的库存数量信息，反映实时库存状态
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@TableName("inventory")
public class Inventory {

    /**
     * 数据库主键ID，自动递增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商品编码（SKU）
     * 标识库存商品的唯一编码，关联商品基础信息表
     */
    private String sku;

    /**
     * 当前库存数量
     * 该SKU商品在仓库中的实际库存数量
     */
    private Long quantity;
}