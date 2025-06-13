package com.xhz.yuncang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 销售订单明细实体类
 * 用于记录销售订单中具体商品的详细信息，与销售订单主表形成主从关系
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@TableName("sales_order_detail")
public class SalesOrderDetail {

    /**
     * 数据库主键ID，自动递增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单编号
     * 关联销售订单主表的唯一标识，用于建立主从关系
     */
    private String orderNumber;

    /**
     * 商品编码（SKU）
     * 标识订单中具体商品的唯一编码，关联商品基础信息表
     */
    private String sku;

    /**
     * 商品数量
     * 该SKU商品在订单中的购买数量
     */
    private Long quantity;
}