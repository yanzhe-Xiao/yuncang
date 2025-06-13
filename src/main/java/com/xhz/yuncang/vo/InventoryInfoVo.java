package com.xhz.yuncang.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 库存信息视图对象
 * 用于前端展示和数据传输的库存简要信息模型
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class InventoryInfoVo {
    /**
     * 商品SKU（唯一标识）
     */
    private String sku;

    /**
     * 当前库存数量
     */
    private Long quantity;
}