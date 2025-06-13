package com.xhz.yuncang.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 入库单实体类
 * 用于存储仓库入库业务的单据信息及状态数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@TableName("inbound_order")
public class InboundOrder {

    /**
     * 数据库主键ID，自动递增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 入库单据名称
     * 用于标识入库单的业务类型或主题（如：原材料入库单、成品入库单）
     */
    private String orderName;

    /**
     * 入库单据编号
     * 系统生成的唯一标识，用于追踪入库业务流程
     */
    private String orderNumber;

    /**
     * 入库日期
     * 单据创建时自动填充的时间戳（精确到秒）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 负责人用户ID
     * 负责该入库单处理的管理员用户标识
     */
    private String userId;

    /**
     * 单据状态
     * 可选值：未开始、进行中、已完成等
     */
    private String status;

    /**
     * 核心属性构造函数
     * @param id 主键ID
     * @param orderName 单据名称
     * @param orderNumber 单据编号
     * @param userId 负责人ID
     * @param status 单据状态
     */
    public InboundOrder(Long id, String orderName, String orderNumber, String userId, String status) {
        this.id = id;
        this.orderName = orderName;
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.status = status;
    }
}