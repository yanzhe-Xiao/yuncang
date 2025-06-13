package com.xhz.yuncang.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 入库单明细信息视图对象
 * 用于向前端展示入库单明细的简化数据模型
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class InboundOrderDetailInfoVo {
    /**
     * 入库单编号
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