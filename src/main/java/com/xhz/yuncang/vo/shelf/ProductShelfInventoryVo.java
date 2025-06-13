package com.xhz.yuncang.vo.shelf;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 商品货架库存视图对象
 * 用于展示商品在货架上的库存汇总信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductShelfInventoryVo {
    /**
     * 商品ID（对应product表的主键）
     */
    private Long id;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品编号（SKU）
     */
    private Long productNumber;

    /**
     * 商品在货架上的总重量（单位：kg）
     */
    private BigDecimal productWeight;
}