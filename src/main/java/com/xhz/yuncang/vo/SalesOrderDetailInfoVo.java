package com.xhz.yuncang.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 销售订单明细信息视图对象
 * 用于向前端展示销售订单中商品明细的数据模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SalesOrderDetailInfoVo {
    /**
     * 销售订单编号
     */
    private String orderNumber;

    /**
     * 商品SKU
     */
    private String sku;

    /**
     * 商品数量
     */
    private Long quantity;
}