package com.xhz.yuncang.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 商品添加数据传输对象
 * 用于接收前端添加商品的请求数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProductAddDTO {
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
     * 商品重量（单位：千克）
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