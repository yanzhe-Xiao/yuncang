package com.xhz.yuncang.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 商品信息修改数据传输对象
 * 用于接收前端修改商品信息的请求数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProductInfoDTO {
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