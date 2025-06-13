package com.xhz.yuncang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xhz.yuncang.entity.OutboundOrder;
import com.xhz.yuncang.mapper.OutboundOrderMapper;
import com.xhz.yuncang.service.OutboundOrderService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OutboundOrderServiceImpl extends ServiceImpl<OutboundOrderMapper, OutboundOrder> implements OutboundOrderService {

    @Override
    public OutboundOrder findByOrderNumber(String orderNumber) {
        return lambdaQuery()
                .eq(OutboundOrder::getOrderNumber, orderNumber)
                .one();
    }

    @Override
    public List<OutboundOrder> findByStatus(String status) {
        return lambdaQuery()
                .eq(OutboundOrder::getStatus,status)
                .list();
    }

    @Override
    public Boolean addOneOutboundOrder(OutboundOrder outboundOrder) {
        return save(outboundOrder);
    }

    @Override
    public Boolean deleteByOrderNumber(String orderNumber) {
        return lambdaUpdate()
                .eq(OutboundOrder::getOrderNumber, orderNumber)
                .remove();
    }

    @Override
    public List<OutboundOrder> findAll() {
        return list();
    }

    @Override
    public Boolean deleteAll() {
        return remove(null);
    }

    @Override
    public Boolean updateByOrderNumber(OutboundOrder outboundOrder) {
        return lambdaUpdate()
                .eq(OutboundOrder::getOrderNumber, outboundOrder.getOrderNumber())
                .set(OutboundOrder::getPlannedDate, outboundOrder.getPlannedDate())
                .set(OutboundOrder::getUserId, outboundOrder.getUserId())
                .set(OutboundOrder::getStatus, outboundOrder.getStatus())
                .update();
    }

    @Override
    public boolean updateStatus(String orderNumber, String newStatus) {
        OutboundOrder order = findByOrderNumber(orderNumber);
        if (order == null) return false;
        order.setStatus(newStatus);
        return updateById(order);
    }

    @Override
    public OutboundOrder getByOrderNumber(String orderNumber) {
        return getOne(new QueryWrapper<OutboundOrder>().eq("order_number", orderNumber));
    }
}