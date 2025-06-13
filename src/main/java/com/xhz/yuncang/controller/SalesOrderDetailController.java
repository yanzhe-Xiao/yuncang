package com.xhz.yuncang.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xhz.yuncang.dto.SalesOrderDetailDTO;
import com.xhz.yuncang.entity.SalesOrderDetail;
import com.xhz.yuncang.service.SalesOrderDetailService;
import com.xhz.yuncang.utils.AjaxResult;
import com.xhz.yuncang.utils.Constants;
import com.xhz.yuncang.vo.PageVo;
import com.xhz.yuncang.vo.SalesOrderDetailInfoVo;
import com.xhz.yuncang.vo.UserInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
/**
 * 销售订单明细管理控制器
 *
 * <p>负责处理销售订单明细的查询操作，主要功能包括：
 * <ul>
 *   <li>订单明细的分页查询</li>
 *   <li>按订单号筛选明细记录</li>
 * </ul>
 *
 * <p>数据特性：
 * <ul>
 *   <li>严格关联销售订单</li>
 *   <li>分页查询需验证页码有效性</li>
 * </ul>
 * @author xhz
 * @see SalesOrderDetailService 销售订单明细服务
 */
@RestController
public class SalesOrderDetailController {
    /**
     * 销售订单明细服务实例
     */
    @Autowired
    private SalesOrderDetailService salesOrderDetailService;

    /**
     * 分页查询订单明细
     *
     * <p>根据订单号获取分页的订单明细数据，包含以下处理：
     * <ol>
     *   <li>验证页码有效性（必须大于0）</li>
     *   <li>检查订单是否存在明细</li>
     *   <li>执行分页查询</li>
     *   <li>构建分页导航信息</li>
     *   <li>转换数据为VO对象</li>
     * </ol>
     *
     * @param pageNo 当前页码（从1开始）
     * @param salesOrderDetailDTO 订单查询条件，包含：
     *                           <ul>
     *                             <li>orderNumber - 订单编号</li>
     *                           </ul>
     * @return 响应实体，包含：
     *         <ul>
     *           <li>成功：状态码200+分页数据(PageVo&lt;SalesOrderDetailInfoVo&gt;)</li>
     *           <li>失败：状态码400+错误信息</li>
     *         </ul>
     * @see PageVo 分页数据包装类
     */
    @PostMapping("/salesOrderDetail/{pageNo}")
    public ResponseEntity<AjaxResult> getSalesOrderDetail(@PathVariable int pageNo,
                                                          @RequestBody SalesOrderDetailDTO salesOrderDetailDTO){
        if (pageNo<=0){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"页数小于1"));
        }
        String orderNumber = salesOrderDetailDTO.getOrderNumber();
        List<SalesOrderDetail> details = salesOrderDetailService.getSalesOrderDetailsByOrderNumber(orderNumber);
        if (details.isEmpty()){
            //没有物品
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"没有物品"));
        }else {
            Page<SalesOrderDetail> page = salesOrderDetailService.page(
                    new Page<>(pageNo, Constants.PageSize),
                    new QueryWrapper<SalesOrderDetail>().eq("order_number", salesOrderDetailDTO.getOrderNumber())
            );
            if (pageNo>page.getPages()){
                //大于最大页数
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"页数大于最大页数"));
            }
            //当前页数nowPage（大于等于1，小于等于 最大页数）
            int nowPage= (int) page.getCurrent();
            //若当前页数为第一页，则它的上一页（beforePage）设为 -1；
            int beforePage= nowPage==1?-1:nowPage-1;
            //若当前页数为最后一页，则它的下一页(nextPage)设为 -1；
            int nextPage= nowPage==(int)page.getPages()?-1:nowPage+1;
            List<SalesOrderDetail> records = page.getRecords();
            List<SalesOrderDetailInfoVo> list = records.stream().map(record -> new SalesOrderDetailInfoVo(
                    record.getOrderNumber(), record.getSku(), record.getQuantity()
            )).toList();

            PageVo<SalesOrderDetailInfoVo> orderDetailInfoVo = new PageVo<>(nowPage,beforePage,nextPage,(int)page.getPages(),list);
            System.out.println(orderDetailInfoVo);
            return ResponseEntity.ok().body(AjaxResult.success(orderDetailInfoVo));
        }
    }

}
