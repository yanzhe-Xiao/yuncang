package com.xhz.yuncang.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 出库单数据传输对象
 * 用于表示出库单的基本信息和状态
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OutDTO {
    /**
     * 订单编号
     */
    private String orderNumber;

    /**
     * 出库单ID
     */
    private String outBoundId;

    /**
     * 出库单状态（如：未开始、进行中、已完成）
     */
    private String status;
}