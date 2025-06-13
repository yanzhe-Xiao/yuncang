package com.xhz.yuncang.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 入库订单明细添加数据传输对象
 * 用于接收前端添加入库订单明细的请求数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class InboundOrderDetailAddDTO {
    /**
     * 入库订单编号
     */
    private String orderNumber;

    /**
     * 商品SKU
     */
    private String sku;

    /**
     * 入库数量
     */
    private Long quantity;
}