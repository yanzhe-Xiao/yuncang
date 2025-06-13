package com.xhz.yuncang.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 商品简化信息视图对象
 * 用于前端快速展示商品基本信息的数据模型
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProductSimpleVo {
    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品编号（SKU）
     */
    private String productNumber;

    /**
     * 商品重量（单位：kg）
     */
    private String productWeight;
}