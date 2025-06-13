package com.xhz.yuncang.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 商品移除数据传输对象
 * 用于接收前端删除商品的请求数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProductRemoveDTO {
    /**
     * 商品SKU（唯一标识）
     */
    private String sku;
}