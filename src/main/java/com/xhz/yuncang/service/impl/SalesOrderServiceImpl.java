package com.xhz.yuncang.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xhz.yuncang.entity.SalesOrder;
import com.xhz.yuncang.mapper.SalesOrderMapper;
import com.xhz.yuncang.service.SalesOrderService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 销售订单服务实现类
 */
@Service
public class SalesOrderServiceImpl extends ServiceImpl<SalesOrderMapper, SalesOrder> implements SalesOrderService {

    @Override
    public boolean addSalesOrder(SalesOrder salesOrder) {
        if (salesOrder == null || !StringUtils.hasText(salesOrder.getOrderNumber())) {
            return false;
        }
        return save(salesOrder);
    }

    @Override
    public SalesOrder findByOrderNo(String orderNumber) {
        return lambdaQuery()
                .eq(SalesOrder::getOrderNumber,orderNumber)
                .one();
    }

    @Override
    public boolean deleteSalesOrderByOrderNumber(String orderNumber) {
        if (!StringUtils.hasText(orderNumber)) {
            return false;
        }
        return lambdaUpdate().eq(SalesOrder::getOrderNumber, orderNumber).remove();
    }

    @Override
    public boolean updateSalesOrderByOrderNumber(SalesOrder salesOrder) {
        if (salesOrder == null || !StringUtils.hasText(salesOrder.getOrderNumber())) {
            return false;
        }
        return lambdaUpdate()
                .eq(SalesOrder::getOrderNumber, salesOrder.getOrderNumber())
                .set(SalesOrder::getUserId, salesOrder.getUserId())
                .set(SalesOrder::getCreateTime, salesOrder.getCreateTime())
                .update();
    }

    @Override
    public SalesOrder getSalesOrderByOrderNumber(String orderNumber) {
        if (!StringUtils.hasText(orderNumber)) {
            return null;
        }
        return lambdaQuery().eq(SalesOrder::getOrderNumber, orderNumber).one();
    }

    @Override
    public List<SalesOrder> getAllSalesOrders() {
        return list();
    }
}