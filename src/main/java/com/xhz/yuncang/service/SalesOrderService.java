package com.xhz.yuncang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xhz.yuncang.entity.SalesOrder;

import java.util.List;

/**
 * 销售订单服务接口
 */
public interface SalesOrderService extends IService<SalesOrder> {

    /**
     * 添加销售订单
     * @param salesOrder 订单实体
     * @return 是否添加成功
     */
    boolean addSalesOrder(SalesOrder salesOrder);

    SalesOrder findByOrderNo(String orderNumber);


    /**
     * 根据订单编号删除销售订单
     * @param orderNumber 订单编号
     * @return 是否删除成功
     */
    boolean deleteSalesOrderByOrderNumber(String orderNumber);

    /**
     * 根据订单编号更新销售订单
     * @param salesOrder 订单实体
     * @return 是否更新成功
     */
    boolean updateSalesOrderByOrderNumber(SalesOrder salesOrder);

    /**
     * 根据订单编号查询销售订单
     * @param orderNumber 订单编号
     * @return 订单实体
     */
    SalesOrder getSalesOrderByOrderNumber(String orderNumber);

    /**
     * 查询所有销售订单
     * @return 订单列表
     */
    List<SalesOrder> getAllSalesOrders();
}