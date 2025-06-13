package com.xhz.yuncang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 入库单明细实体类
 * 用于存储入库单中具体商品的详细信息，与入库单主表形成主从关联关系
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@TableName("inbound_order_detail")
public class InboundOrderDetail {

    /**
     * 数据库主键ID，自动递增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 入库单编号
     * 关联入库单主表的唯一标识，用于建立主从表数据关联
     */
    private String orderNumber;

    /**
     * 商品编号（SKU）
     * 入库商品的唯一标识，关联商品基础信息表
     */
    private String sku;

    /**
     * 商品数量
     * 该SKU商品的实际入库数量
     */
    private Long quantity;
}