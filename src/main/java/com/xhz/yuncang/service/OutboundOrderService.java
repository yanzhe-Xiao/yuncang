package com.xhz.yuncang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xhz.yuncang.entity.OutboundOrder;

import java.util.List;

public interface OutboundOrderService extends IService<OutboundOrder> {

    OutboundOrder findByOrderNumber(String orderNumber);


    List<OutboundOrder> findByStatus(String status);

    Boolean addOneOutboundOrder(OutboundOrder outboundOrder);

    Boolean deleteByOrderNumber(String orderNumber);

    List<OutboundOrder> findAll();

    Boolean deleteAll();

    Boolean updateByOrderNumber(OutboundOrder outboundOrder);

    boolean updateStatus(String orderNumber, String newStatus);

    OutboundOrder getByOrderNumber(String orderNumber);
}