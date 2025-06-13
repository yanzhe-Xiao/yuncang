package com.xhz.yuncang.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 库存数据传输对象
 * 用于表示商品的当前库存信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class InventoryDTO {
    /**
     * 商品SKU（唯一标识）
     */
    private String sku;

    /**
     * 当前库存数量
     */
    private Long quantity;
}