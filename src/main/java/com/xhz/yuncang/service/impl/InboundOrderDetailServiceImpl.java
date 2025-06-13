package com.xhz.yuncang.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xhz.yuncang.entity.InboundOrderDetail;
import com.xhz.yuncang.mapper.InboundOrderDetailMapper;
import com.xhz.yuncang.service.InboundOrderDetailService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InboundOrderDetailServiceImpl extends ServiceImpl<InboundOrderDetailMapper, InboundOrderDetail>
        implements InboundOrderDetailService {
    @Override
    public List<InboundOrderDetail> listByOrderNumber(String orderNumber) {
        // 根据入库单编号查询所有明细项
        return lambdaQuery()
                .eq(InboundOrderDetail::getOrderNumber, orderNumber)
                .list();
    }

    @Override
    public boolean deleteByOrderNumberAndSku(String orderNumber, String sku) {
        // 根据入库单编号和SKU删除指定明细项
        return lambdaUpdate()
                .eq(InboundOrderDetail::getOrderNumber, orderNumber)
                .eq(InboundOrderDetail::getSku, sku)
                .remove();
    }

    @Override
    public boolean updateQuantity(String orderNumber, String sku, Long newQuantity) {
        // 根据入库单编号和SKU更新商品数量
        return lambdaUpdate()
                .eq(InboundOrderDetail::getOrderNumber, orderNumber)
                .eq(InboundOrderDetail::getSku, sku)
                .set(InboundOrderDetail::getQuantity, newQuantity)
                .update();
    }

    @Override
    public InboundOrderDetail getByOrderNumberAndSku(String orderNumber, String sku) {
        // 根据入库单编号和SKU查询单个明细项
        return lambdaQuery()
                .eq(InboundOrderDetail::getSku, sku)
                .eq(InboundOrderDetail::getOrderNumber, orderNumber)
                .one();
    }

    @Override
    public Boolean addOneInboundOrderDetail(InboundOrderDetail inboundOrderDetail) {
        return save(inboundOrderDetail);//新增入库单明细表
    }







}