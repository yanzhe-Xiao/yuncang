package com.xhz.yuncang.vo;

import com.xhz.yuncang.dto.SalesOrderDetailAddDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 销售订单信息视图对象
 * 用于向前端展示销售订单完整信息的数据模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SalesOrderInfoVo {
    /**
     * 订单ID（数据库主键）
     */
    private String id;

    /**
     * 订单编号（唯一）
     */
    private String orderNumber;

    /**
     * 创建者用户ID
     */
    private String userId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 订单状态（如：未开始、进行中、已完成）
     */
    private String status;

    /**
     * 订单明细列表
     */
    private List<SalesOrderDetailAddDTO> details;

    /**
     * 关联的出库单ID
     */
    private String outBoundId;

    /**
     * 出库操作人ID
     */
    private String outBoundUser;
}