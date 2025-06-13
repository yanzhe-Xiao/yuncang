package com.xhz.yuncang.vo.shelf;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 货架层信息视图对象
 * 用于向前端展示单个货架层的详细信息及存储状态
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ShelfInfoVo {
    /**
     * 货架X坐标（位置标识）
     */
    private String shelfX;

    /**
     * 货架Y坐标（位置标识）
     */
    private String shelfY;

    /**
     * 货架Z坐标（层数标识）
     */
    private String shelfZ;

    /**
     * 货架层编码
     */
    private String shelfCode;

    /**
     * 最大承重（单位：克）
     */
    private Long maxWeight;

    /**
     * 存储商品SKU
     */
    private String sku;

    /**
     * 存储商品名称
     */
    private String name;

    /**
     * 存储商品数量
     */
    private Long quantity;

    /**
     * 当前总重量（单位：克，格式化字符串）
     */
    private String weight;
}