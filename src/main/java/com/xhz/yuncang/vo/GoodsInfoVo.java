package com.xhz.yuncang.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 商品信息视图对象
 * 用于向前端展示商品详细信息的数据模型（管理员）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class GoodsInfoVo {
    /**
     * 商品SKU（唯一标识）
     */
    private String sku;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 商品重量（单位：kg）
     */
    private Double weight;

    /**
     * 商品长度（单位：cm）
     */
    private Double length;

    /**
     * 商品宽度（单位：cm）
     */
    private Double width;

    /**
     * 商品高度（单位：cm）
     */
    private Double height;
}