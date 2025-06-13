package com.xhz.yuncang.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * AGV小车信息视图对象
 * 用于前端展示和数据传输的AGV小车数据模型
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AgvCarInfoVo {
    /**
     * AGV小车ID
     */
    private String id;

    /**
     * 小车编号（唯一标识）
     */
    private String carNumber;

    /**
     * 当前状态（如：空闲、任务中、维护中）
     */
    private String status;

    /**
     * 负责人用户ID
     */
    private String userId;

    /**
     * 当前电量百分比
     */
    private Integer batteryLevel;

    /**
     * 最大载重量（单位：kg）
     */
    private Double maxWeight;

    /**
     * 当前X坐标位置
     */
    private Double locationX;

    /**
     * 当前Y坐标位置
     */
    private Double locationY;

    /**
     * 小车起点X坐标
     */
    private Double startX;

    /**
     * 小车起点Y坐标
     */
    private Double startY;

    /**
     * 任务终点X坐标
     */
    private Double endX;

    /**
     * 任务终点Y坐标
     */
    private Double endY;

    /**
     * 任务终点Z坐标（高度）
     */
    private Double endZ;

    /**
     * 携带商品SKU
     */
    private String sku;

    /**
     * 携带商品数量
     */
    private Long quantity;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}