package com.xhz.yuncang.vo.shelf;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 货架基本信息视图对象
 * 用于向前端展示货架的物理属性和位置信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorageShelfVo {
    /**
     * 货架ID（数据库主键）
     */
    private Long id;

    /**
     * 货架编码
     */
    private String shelfCode;

    /**
     * X坐标位置（单位：cm）
     */
    private Double locationX;

    /**
     * Y坐标位置（单位：cm）
     */
    private Double locationY;

    /**
     * Z坐标位置（单位：cm，表示高度）
     */
    private Double locationZ;

    /**
     * 最大承重（单位：千克）
     */
    private Double maxWeight;

    /**
     * 长度（单位：cm）
     */
    private Double length;

    /**
     * 宽度（单位：cm）
     */
    private Double width;

    /**
     * 高度（单位：cm）
     */
    private Double height;
}