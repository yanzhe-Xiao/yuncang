package com.xhz.yuncang.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

/**
 * 入库订单数据传输对象
 * 用于接收前端创建入库订单的请求数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class InboundOrderDTO {
    /**
     * 入库单名称
     */
    private String orderName;

    /**
     * 负责人用户ID
     */
    private String userId;

    /**
     * 入库商品明细列表
     */
    private List<SalesOrderDetailAddDTO> details;
}