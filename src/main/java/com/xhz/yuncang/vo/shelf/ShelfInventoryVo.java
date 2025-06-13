package com.xhz.yuncang.vo.shelf;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 货架库存视图对象
 * 用于展示货架层与库存关联的基本信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShelfInventoryVo {
    /**
     * 货架库存ID（数据库主键）
     */
    private Long id;

    /**
     * 货架层编码
     */
    private String shelfCode;

    /**
     * 存储商品SKU
     */
    private String sku;

    /**
     * 存储商品数量
     */
    private Long quantity;
}