package com.xhz.yuncang.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

/**
 * 出库单信息视图对象
 * 用于向前端展示出库单核心信息的数据模型
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OutboundOrderInfoVo {
    /**
     * 出库单编号
     */
    private String orderNumber;

    /**
     * 预计出库日期
     */
    private LocalDate plannedDate;

    /**
     * 操作员ID
     */
    private String userId;

    /**
     * 出库单状态（如：未开始、进行中、已完成）
     */
    private String status;
}