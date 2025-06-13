package com.xhz.yuncang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xhz.yuncang.entity.InboundOrder;

import java.util.List;

public interface InboundOrderService extends IService<InboundOrder> {
    /**
     * 根据入库单名称查询单条记录
     * @param orderName 入库单名称（唯一）
     * @return 入库单实体
     */
    InboundOrder getByOrderName(String orderName);

    /**
     * 根据入库单编号查询单条记录
     * @param orderNumber
     * @return
     */
    InboundOrder getByOrderNumber(String orderNumber);

    /**
     * 根据入库单名称删除记录
     * @param orderName 入库单名称
     * @return 是否删除成功
     */
    boolean deleteByOrderName(String orderName);

    /**
     * 查询指定负责人的所有入库单
     * @param userId 负责人用户ID
     * @return 入库单列表
     */
    List<InboundOrder> listByUserId(String userId);

    /**
     * 查询指定状态的所有入库单
     * @param status 状态值
     * @return 入库单列表
     */
    List<InboundOrder> listByStatus(String status);

    /**
     * 更新入库单状态
     * @param orderNumber 入库单编号
     * @param newStatus 新状态值
     * @return 是否更新成功
     */
    boolean updateStatus(String orderNumber, String newStatus);

    /**
     * 新增入库单
     * @param inboundOrder 入库单实体
     * @return 是否新增成功
     */
    Boolean addOneInboundOrder(InboundOrder inboundOrder);



}