package com.xhz.yuncang.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * 销售订单添加数据传输对象
 * 用于接收前端创建销售订单的请求数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SalesOrderAddDTO {
    /**
     * 订单编号
     */
    private String orderNumber;

    /**
     * 用户ID
     */
    private String useId;

    /**
     * 订单明细列表
     */
    private List<SalesOrderDetailAddDTO> details;
}