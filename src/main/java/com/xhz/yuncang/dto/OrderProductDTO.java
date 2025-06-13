package com.xhz.yuncang.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 订单商品数据传输对象
 * 用于表示订单中包含的商品及其数量信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OrderProductDTO {
    /**
     * 商品SKU
     */
    private String sku;

    /**
     * 商品数量
     */
    private Integer quantity;
}