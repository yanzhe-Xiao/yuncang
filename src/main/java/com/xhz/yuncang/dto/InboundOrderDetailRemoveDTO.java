package com.xhz.yuncang.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 入库订单明细移除数据传输对象
 * 用于接收前端删除入库订单明细的请求数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class InboundOrderDetailRemoveDTO {
    /**
     * 入库订单编号
     */
    private String orderNumber;

    /**
     * 商品SKU
     */
    private String sku;
}