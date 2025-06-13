package com.xhz.yuncang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xhz.yuncang.entity.InboundOrderDetail;

import java.util.List;

public interface InboundOrderDetailService extends IService<InboundOrderDetail> {
    /**
     * 根据入库单编号查询明细列表
     * @param orderNumber 入库单编号
     * @return 明细列表
     */
    List<InboundOrderDetail> listByOrderNumber(String orderNumber);

    /**
     * 根据入库单编号和SKU删除明细项
     * @param orderNumber 入库单编号
     * @param sku 商品SKU
     * @return 是否删除成功
     */
    boolean deleteByOrderNumberAndSku(String orderNumber, String sku);

    /**
     * 更新指定明细项的商品数量
     * @param orderNumber 入库单编号
     * @param sku 商品SKU
     * @param newQuantity 新数量
     * @return 是否更新成功
     */
    boolean updateQuantity(String orderNumber, String sku, Long newQuantity);

    /**
     * 根据入库单编号和SKU查询单个明细项
     * @param orderNumber 入库单编号
     * @param sku 商品SKU
     * @return 明细项实体
     */
    InboundOrderDetail getByOrderNumberAndSku(String orderNumber, String sku);

    /**
     * 新增入库单明细
     * @param inboundOrderDetail 明细实体
     * @return 是否新增成功
     */
    Boolean addOneInboundOrderDetail(InboundOrderDetail inboundOrderDetail);


}