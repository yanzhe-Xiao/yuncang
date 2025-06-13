package com.xhz.yuncang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 货架层实体类
 * 用于存储仓库货架的基础信息及空间属性，支持仓储布局规划与库存定位
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@TableName("storage_shelf")
public class StorageShelf {

    /**
     * 数据库主键ID，自动递增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 货架层编号
     * 系统生成的唯一标识
     */
    private String shelfCode;

    /**
     * 最大载重（单位：kg）
     * 货架层的安全承重上限，用于库存分配时的载重校验
     */
    private Double maxWeight;

    /**
     * 长度（单位：cm）
     * 货架层的水平长度尺寸，用于空间利用率计算
     */
    private Double length;

    /**
     * 宽度（单位：cm）
     * 货架层的水平宽度尺寸，用于商品摆放规划
     */
    private Double width;

    /**
     * 高度（单位：cm）
     * 货架层的垂直高度尺寸，决定可堆叠商品的高度限制
     */
    private Double height;

    /**
     * 位置X轴坐标
     * 仓储空间中货架层的水平位置（二维定位）
     */
    private Double locationX;

    /**
     * 位置Y轴坐标
     * 仓储空间中货架层的垂直位置（二维定位）
     */
    private Double locationY;

    /**
     * 位置Z轴坐标（层数）
     * 三维空间中货架层的垂直高度坐标，标识货架所在的层级
     */
    private Double locationZ;
}