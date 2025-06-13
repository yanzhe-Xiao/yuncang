package com.xhz.yuncang.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 销售订单明细数据传输对象
 * 用于表示销售订单中的一个明细项
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SalesOrderDetailDTO {
    /**
     * 订单编号
     */
    private String orderNumber;
}