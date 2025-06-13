package com.xhz.yuncang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

/**
 * 出库单实体类
 * 用于记录商品出库业务的相关信息，与销售订单、库存管理模块关联
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@TableName("outbound_order")
public class OutboundOrder {

    /**
     * 数据库主键ID，自动递增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 出库单编号
     * 系统生成的唯一标识，关联销售订单号或其他业务单号
     */
    private String orderNumber;

    /**
     * 预计出库时间
     * 计划执行出库操作的日期
     */
    private LocalDate plannedDate;

    /**
     * 操作员ID
     * 执行出库操作的用户标识，关联系统用户表
     */
    private String userId;

    /**
     * 出库单状态
     * 可选值：未开始、进行中、已完成等
     */
    private String status;
}