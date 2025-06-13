package com.xhz.yuncang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xhz.yuncang.entity.SalesOrderDetail;

import java.util.List;

/**
 * 销售订单明细服务接口
 */
public interface SalesOrderDetailService extends IService<SalesOrderDetail> {

    /**
     * 添加销售订单明细
     * @param salesOrderDetail 订单明细实体
     * @return 是否添加成功
     */
    boolean addSalesOrderDetail(SalesOrderDetail salesOrderDetail);

    /**
     * 根据订单编号和商品 SKU 删除订单明细
     * @param orderNumber 订单编号
     * @param sku 商品 SKU
     * @return 是否删除成功
     */
    boolean removeSalesOrderDetailByOrderNumberAndSku(String orderNumber, String sku);

    /**
     * 根据订单编号和商品 SKU 更新订单明细
     * @param salesOrderDetail 订单明细实体
     * @return 是否更新成功
     */
    boolean updateSalesOrderDetailByOrderNumberAndSku(SalesOrderDetail salesOrderDetail);

    /**
     * 根据订单编号和商品 SKU 查询订单明细
     * @param orderNumber 订单编号
     * @param sku 商品 SKU
     * @return 订单明细实体
     */
    SalesOrderDetail getSalesOrderDetailByOrderNumberAndSku(String orderNumber, String sku);

    /**
     * 根据订单编号查询所有订单明细
     * @param orderNumber 订单编号
     * @return 订单明细列表
     */
    List<SalesOrderDetail> getSalesOrderDetailsByOrderNumber(String orderNumber);

    /**
     * 查询所有订单明细
     * @return 订单明细列表
     */
    List<SalesOrderDetail> getAllSalesOrderDetails();


    boolean removeSalesOrderDetailByOrderNumber(String orderNumber);
}