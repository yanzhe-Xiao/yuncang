package com.xhz.yuncang.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 入库订单明细信息数据传输对象
 * 用于返回入库订单明细的详细信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class InboundOrderDetailInfoDTO {
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