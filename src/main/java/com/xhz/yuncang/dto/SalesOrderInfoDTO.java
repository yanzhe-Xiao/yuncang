package com.xhz.yuncang.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * 销售订单信息数据传输对象
 * 用于接收前端传递的完整订单信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SalesOrderInfoDTO {
    /**
     * 订单编号
     */
    private String orderNumber;

    /**
     * 用户ID
     */
    private String useId;

    /**
     * 订单商品列表
     */
    private List<OrderProductDTO> productList;
}