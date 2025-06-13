package com.xhz.yuncang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 系统提醒实体类
 * 用于记录系统生成的各类提醒信息，支持业务流程的自动化通知
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Remind {

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    /**
     * 状态
     */
    private String status;

    /**
     * 提醒消息内容
     * 简要描述提醒的核心信息
     */
    private String message;

    /**
     * 提醒上下文信息
     * 包含业务相关的详细信息（如JSON格式数据）
     */
    private String context;

    /**
     * 提醒创建时间
     * 自动记录提醒生成的时间戳
     */
    private LocalDateTime createTime;

    /**
     * 提醒处理状态
     */
    private String processed;
}