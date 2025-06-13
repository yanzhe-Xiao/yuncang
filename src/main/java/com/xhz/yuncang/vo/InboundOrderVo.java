package com.xhz.yuncang.vo;

import com.xhz.yuncang.dto.SalesOrderDetailAddDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 入库单视图对象
 * 用于前端展示和数据传输的入库单完整信息模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class InboundOrderVo {
    private Long id;
    private String orderName;

    /**
     * 入库单编号（唯一）
     */
    private String orderNumber;

    /**
     * 创建日期
     */
    private LocalDateTime createDate;

    /**
     * 负责人用户ID
     */
    private String userId;

    /**
     * 单据状态（如：未开始、进行中、已完成）
     */
    private String status;

    /**
     * 入库单明细列表
     */
    private List<SalesOrderDetailAddDTO> details;
}