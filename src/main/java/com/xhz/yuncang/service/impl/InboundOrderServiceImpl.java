package com.xhz.yuncang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xhz.yuncang.entity.InboundOrder;
import com.xhz.yuncang.mapper.InboundOrderMapper;
import com.xhz.yuncang.service.InboundOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InboundOrderServiceImpl extends ServiceImpl<InboundOrderMapper, InboundOrder>
        implements InboundOrderService {

    @Autowired
    private InboundOrderMapper inboundOrderMapper;

    @Override
    public InboundOrder getByOrderName(String orderName) {
        // 根据唯一的入库单名称查询单条记录
        return lambdaQuery()
                .eq(InboundOrder::getOrderName, orderName)
                .one();
    }
    @Override
    public InboundOrder getByOrderNumber(String orderNumber) {
        // 根据唯一的入库单编号查询单条记录
        return lambdaQuery()
                .eq(InboundOrder::getOrderNumber, orderNumber)
                .one();
    }

    @Override
    public boolean deleteByOrderName(String orderName) {
        // 查询 order_number
        String orderNumber = inboundOrderMapper.getOrderNumberByOrderName(orderName);
        if (orderNumber == null) {
            // order_name 不存在
            return false;
        }
        //删除 inbound_order 记录
        int deletedOrder = inboundOrderMapper.deleteInboundOrderByOrderName(orderName);
        if(deletedOrder == 0){
            return false;
        }
//        int deletedOrderDetail = inboundOrderMapper.deleteInboundOrderDetailsByOrderNumber(orderNumber);
        // 删除 inbound_order_detail 记录
        inboundOrderMapper.deleteInboundOrderDetailsByOrderNumber(orderNumber);

        // 始终返回 true，因为删除操作已成功（即使 detail 表没有记录）
        return true;
    }


    @Override
    public List<InboundOrder> listByUserId(String userId) {
        // 查询指定负责人的所有入库单
        return lambdaQuery()
                .eq(InboundOrder::getUserId, userId)
                .list();
    }

    @Override
    public List<InboundOrder> listByStatus(String status) {
        // 查询指定状态的所有入库单
        return lambdaQuery()
                .eq(InboundOrder::getStatus, status)
                .list();
    }

    @Override
    public boolean updateStatus(String orderNumber, String newStatus) {
        // 更新指定入库单的状态
        return lambdaUpdate()
                .eq(InboundOrder::getOrderNumber, orderNumber)
                .set(InboundOrder::getStatus, newStatus)
                .update();
    }

    @Override
    public Boolean addOneInboundOrder(InboundOrder inboundOrder) {
        return save(inboundOrder);
    }
}


