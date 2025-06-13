package com.xhz.yuncang.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 销售订单明细添加数据传输对象
 * 用于接收前端添加销售订单明细的请求数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SalesOrderDetailAddDTO {
    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品SKU
     */
    private String sku;

    /**
     * 商品数量
     */
    private Integer quantity;
}