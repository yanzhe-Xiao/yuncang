package com.xhz.yuncang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 商品信息实体类
 * 对应数据库表 product，存储商品基础信息及物理属性
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@TableName("product")
public class Product {

    /**
     * 数据库主键ID，自动递增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商品名称
     * 用于展示和识别的商品标题
     */
    private String name;

    /**
     * 商品编号（SKU）
     * 商品的唯一标识代码，用于系统内精确区分不同商品
     */
    private String sku;

    /**
     * 商品描述
     * 对商品特性、功能、用途等的详细说明
     */
    private String description;

    /**
     * 商品重量（单位：kg）
     * 商品的物理重量，用于物流计算和仓储规划
     */
    private Double weight;

    /**
     * 商品长度（单位：cm）
     * 商品的外形长度尺寸
     */
    private Double length;

    /**
     * 商品宽度（单位：cm）
     * 商品的外形宽度尺寸
     */
    private Double width;

    /**
     * 商品高度（单位：cm）
     * 商品的外形高度尺寸
     */
    private Double height;
}