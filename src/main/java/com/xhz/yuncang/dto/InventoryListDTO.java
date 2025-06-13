package com.xhz.yuncang.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * 库存列表数据传输对象
 * 用于批量查询或操作多个商品库存信息的请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class InventoryListDTO {
    /**
     * 商品SKU列表
     */
    private List<String> sku;
}