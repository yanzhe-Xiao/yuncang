package com.xhz.yuncang.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 销售订单实体类
 * 用于记录客户订单的基本信息和状态流转
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@TableName("sales_order")
public class SalesOrder {

    /**
     * 数据库主键ID，自动递增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单编号
     * 系统生成的唯一标识，用于订单追踪和查询
     */
    private String orderNumber;

    /**
     * 客户ID
     * 关联用户表，标识订单的创建者
     */
    private String userId;

    /**
     * 订单创建时间
     * 插入记录时自动填充，记录订单生成的时间戳
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}