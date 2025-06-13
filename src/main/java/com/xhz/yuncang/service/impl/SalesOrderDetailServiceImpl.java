package com.xhz.yuncang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xhz.yuncang.entity.SalesOrderDetail;
import com.xhz.yuncang.mapper.SalesOrderDetailMapper;
import com.xhz.yuncang.service.SalesOrderDetailService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 销售订单明细服务实现类
 */
@Service
public class SalesOrderDetailServiceImpl extends ServiceImpl<SalesOrderDetailMapper, SalesOrderDetail>
        implements SalesOrderDetailService {

    @Override
    public boolean addSalesOrderDetail(SalesOrderDetail salesOrderDetail) {
        if (salesOrderDetail == null || !StringUtils.hasText(salesOrderDetail.getOrderNumber())
                || !StringUtils.hasText(salesOrderDetail.getSku()) || salesOrderDetail.getQuantity() == null) {
            return false;
        }
        return save(salesOrderDetail);
    }

    @Override
    public boolean removeSalesOrderDetailByOrderNumberAndSku(String orderNumber, String sku) {
        if (!StringUtils.hasText(orderNumber) || !StringUtils.hasText(sku)) {
            return false;
        }
        QueryWrapper<SalesOrderDetail> wrapper = new QueryWrapper<>();
        wrapper.eq("order_number", orderNumber).eq("sku", sku);
        return remove(wrapper);
    }

    @Override
    public boolean updateSalesOrderDetailByOrderNumberAndSku(SalesOrderDetail salesOrderDetail) {
        if (salesOrderDetail == null || !StringUtils.hasText(salesOrderDetail.getOrderNumber())
                || !StringUtils.hasText(salesOrderDetail.getSku())) {
            return false;
        }
        QueryWrapper<SalesOrderDetail> wrapper = new QueryWrapper<>();
        wrapper.eq("order_number", salesOrderDetail.getOrderNumber()).eq("sku", salesOrderDetail.getSku());
        return update(salesOrderDetail, wrapper);
    }

    @Override
    public SalesOrderDetail getSalesOrderDetailByOrderNumberAndSku(String orderNumber, String sku) {
        if (!StringUtils.hasText(orderNumber) || !StringUtils.hasText(sku)) {
            return null;
        }
        QueryWrapper<SalesOrderDetail> wrapper = new QueryWrapper<>();
        wrapper.eq("order_number", orderNumber).eq("sku", sku);
        return getOne(wrapper);
    }

    @Override
    public List<SalesOrderDetail> getSalesOrderDetailsByOrderNumber(String orderNumber) {
        if (!StringUtils.hasText(orderNumber)) {
            return List.of();
        }
        QueryWrapper<SalesOrderDetail> wrapper = new QueryWrapper<>();
        wrapper.eq("order_number", orderNumber);
        return list(wrapper);
    }

    @Override
    public List<SalesOrderDetail> getAllSalesOrderDetails() {
        return list();
    }

    @Override
    public boolean removeSalesOrderDetailByOrderNumber(String orderNumber) {
        QueryWrapper<SalesOrderDetail> wrapper = new QueryWrapper<>();
        wrapper.eq("order_number", orderNumber);
        return remove(wrapper);
    }
}