package com.xhz.yuncang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xhz.yuncang.entity.InboundOrder;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;
@Mapper
public interface InboundOrderMapper extends BaseMapper<InboundOrder> {

    // 查询 order_number 根据 order_name
    @Select("SELECT order_number FROM inbound_order WHERE order_name = #{orderName}")
    String getOrderNumberByOrderName(@Param("orderName") String orderName);

    // 删除 inbound_order 记录
    @Delete("DELETE FROM inbound_order WHERE order_name = #{orderName}")
    int deleteInboundOrderByOrderName(@Param("orderName") String orderName);

    // 删除 inbound_order_detail 记录
    @Delete("DELETE FROM inbound_order_detail WHERE order_number = #{orderNumber}")
    int deleteInboundOrderDetailsByOrderNumber(@Param("orderNumber") String orderNumber);
}
