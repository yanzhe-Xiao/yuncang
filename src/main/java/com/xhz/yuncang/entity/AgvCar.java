package com.xhz.yuncang.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * AGV小车实体类
 * 用于存储AGV智能搬运小车的基本信息及运行状态数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@TableName("agv_car")
public class AgvCar {

    /**
     * 数据库主键ID，自动递增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 小车唯一编码标识
     * 用于区分不同AGV设备的唯一性编号
     */
    private String carNumber;

    /**
     * 小车当前运行状态
     * 可选值：空闲、任务中、维护中等
     */
    private String status;

    /**
     * 电池电量百分比
     * 范围：0-100
     */
    private Integer batteryLevel;

    /**
     * 携带商品的SKU编码
     * 关联商品的唯一标识
     */
    private String sku;

    /**
     * 携带商品的数量
     */
    private Long quantity;

    /**
     * 小车起点X坐标
     * 仓储地图中的水平位置坐标
     */
    private Double startX;

    /**
     * 小车起点Y坐标
     * 仓储地图中的垂直位置坐标
     */
    private Double startY;

    /**
     * 任务终点X坐标
     * 仓储地图中的水平位置坐标
     */
    private Double endX;

    /**
     * 任务终点Y坐标
     * 仓储地图中的垂直位置坐标
     */
    private Double endY;

    /**
     * 第一负责人用户ID
     * 负责该AGV的管理员用户标识
     */
    private String userId;

    /**
     * 最大载重量（单位：kg）
     * AGV小车的最大承重能力
     */
    private Double maxWeight;

    /**
     * 终点Z坐标
     * 三维空间中的垂直高度坐标（如货架层高）
     */
    private Double endZ;

    /**
     * 当前位置X坐标
     * 实时定位坐标
     */
    private Double locationX;

    /**
     * 当前位置Y坐标
     * 实时定位坐标
     */
    private Double locationY;

    /**
     * 创建时间
     * 数据插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     * 数据更新时自动填充
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


    /**
     * 基础信息构造函数
     * @param id 主键ID
     * @param carNumber 小车编号
     * @param status 运行状态
     * @param batteryLevel 电量
     * @param sku 商品SKU
     * @param quantity 商品数量
     * @param startX 起点X坐标
     * @param startY 起点Y坐标
     * @param endX 终点X坐标
     * @param endY 终点Y坐标
     * @param userId 负责人ID
     * @param maxWeight 最大载重量
     */
    public AgvCar(Long id, String carNumber, String status,
                  Integer batteryLevel, String sku, Long quantity,
                  Double startX, Double startY, Double endX, Double endY,
                  String userId, Double maxWeight) {
        this.id = id;
        this.carNumber = carNumber;
        this.status = status;
        this.batteryLevel = batteryLevel;
        this.sku = sku;
        this.quantity = quantity;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.userId = userId;
        this.maxWeight = maxWeight;
    }

    /**
     * 全属性构造函数
     * @param id 主键ID
     * @param carNumber 小车编号
     * @param status 运行状态
     * @param batteryLevel 电量
     * @param sku 商品SKU
     * @param quantity 商品数量
     * @param startX 起点X坐标
     * @param startY 起点Y坐标
     * @param endX 终点X坐标
     * @param endY 终点Y坐标
     * @param userId 负责人ID
     * @param maxWeight 最大载重量
     * @param endZ 终点Z坐标
     * @param locationX 当前X坐标
     * @param locationY 当前Y坐标
     */
    public AgvCar(Long id, String carNumber, String status, Integer batteryLevel,
                  String sku, Long quantity, Double startX, Double startY, Double endX,
                  Double endY, String userId, Double maxWeight, Double endZ, Double locationX,
                  Double locationY) {
        this.id = id;
        this.carNumber = carNumber;
        this.status = status;
        this.batteryLevel = batteryLevel;
        this.sku = sku;
        this.quantity = quantity;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.userId = userId;
        this.maxWeight = maxWeight;
        this.endZ = endZ;
        this.locationX = locationX;
        this.locationY = locationY;
    }
}