package com.xhz.yuncang.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xhz.yuncang.dto.InboundOrderDetailAddDTO;
import com.xhz.yuncang.dto.InboundOrderDetailInfoDTO;
import com.xhz.yuncang.dto.InboundOrderDetailRemoveDTO;
import com.xhz.yuncang.entity.InboundOrderDetail;
import com.xhz.yuncang.entity.Product;
import com.xhz.yuncang.service.InboundOrderDetailService;
import com.xhz.yuncang.service.InboundOrderService;
import com.xhz.yuncang.service.ProductService;
import com.xhz.yuncang.utils.AjaxResult;
import com.xhz.yuncang.utils.Constants;
import com.xhz.yuncang.vo.InboundOrderDetailInfoVo;
import com.xhz.yuncang.vo.PageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
/**
 * 入库单明细管理控制器
 *
 * <p>负责处理入库单明细的增删改查操作，提供以下核心功能：
 * <ul>
 *   <li>入库单明细的分页查询</li>
 *   <li>明细记录的添加、修改和删除</li>
 *   <li>批量查询入库单明细</li>
 *   <li>与入库单和商品的关联校验</li>
 * </ul>
 *
 * <p>数据校验规则：
 * <ul>
 *   <li>所有操作必须验证入库单存在性</li>
 *   <li>商品SKU必须有效</li>
 *   <li>数量必须为非负整数</li>
 * </ul>
 * @author WanCong
 * @see InboundOrderDetailService 入库单明细服务接口
 * @see InboundOrderService 入库单服务接口
 * @see ProductService 商品服务接口
 */
@RestController
@PreAuthorize("hasRole('管理员') or hasRole('操作员')")
public class InboundOrderDetailController {
    /**
     * 入库单明细服务实例
     */
    @Autowired
    private InboundOrderDetailService inboundOrderDetailService;
    /**
     * 入库单服务实例（用于验证orderNumber）
     */
    @Autowired
    private InboundOrderService inboundOrderService; // 验证 orderNumber
    /**
     * 商品服务实例（用于验证sku）
     */
    @Autowired
    private ProductService productService; // 验证 sku

    /**
     * 分页查询入库单明细
     *
     * <p>根据入库单编号获取分页明细数据，包含以下处理逻辑：
     * <ol>
     *   <li>验证页码有效性（必须大于0）</li>
     *   <li>验证订单编号非空</li>
     *   <li>执行分页查询</li>
     *   <li>构建分页导航信息</li>
     *   <li>转换数据为VO对象</li>
     * </ol>
     *
     * @param pageNo 当前页码（从1开始）
     * @param inOrder 包含订单编号的Map，格式：
     *                {
     *                    "orderNumber": "入库单编号"
     *                }
     * @return 响应实体，包含：
     *         <ul>
     *           <li>成功：状态码200+分页数据(PageVo&lt;InboundOrderDetailInfoVo&gt;)</li>
     *           <li>失败：状态码400+错误信息</li>
     *         </ul>
     * @throws Exception 当出现以下情况时返回400错误：
     *                               <ul>
     *                                 <li>页码小于等于0</li>
     *                                 <li>订单编号为空</li>
     *                                 <li>页码超过最大页数</li>
     *                               </ul>
     * @see PageVo 分页数据包装类
     * @see InboundOrderDetailInfoVo 明细信息VO
     */
    @PostMapping("/inboundOrderDetail/{pageNo}")
    public ResponseEntity<AjaxResult> showInboundOrderDetailsByPage(@PathVariable int pageNo, @RequestBody Map<String, String> inOrder) {
        if (pageNo <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error("页数小于 1"));
        }
        // 获取传入的 orderNumber
        String orderNumber = inOrder.get("orderNumber");
        if (orderNumber == null || orderNumber.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error("入库单编号不能为空"));
        }
        // 创建查询条件
        QueryWrapper<InboundOrderDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_number", orderNumber);

        // 分页查询
        Page<InboundOrderDetail> page = inboundOrderDetailService.page(new Page<>(pageNo, Constants.PageSize), queryWrapper);


        if (pageNo > page.getPages()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error("页数大于最大页数"));
        }

        int nowPage = (int) page.getCurrent();
        int beforePage = nowPage == 1 ? -1 : nowPage - 1;
        int nextPage = nowPage == (int) page.getPages() ? -1 : nowPage + 1;

        List<InboundOrderDetail> details = page.getRecords();
        List<InboundOrderDetailInfoVo> detailVoList = details.stream().map(detail -> new InboundOrderDetailInfoVo(
                detail.getOrderNumber(),
                detail.getSku(),
                detail.getQuantity()
        )).toList();

        PageVo<InboundOrderDetailInfoVo> detailPageVo = new PageVo<>(
                nowPage,
                beforePage,
                nextPage,
                (int) page.getPages(),
                detailVoList
        );

        return ResponseEntity.ok().body(AjaxResult.success(detailPageVo));
    }

    /**
     * 添加入库单明细
     *
     * <p>执行严格的业务校验后添加新明细，包括：
     * <ul>
     *   <li>入库单必须存在</li>
     *   <li>商品SKU必须有效</li>
     *   <li>数量必须为非负整数</li>
     *   <li>同一订单不能有重复SKU</li>
     * </ul>
     *
     * @param detailAddDTO 新增明细数据，包含：
     *                     <ul>
     *                       <li>orderNumber - 关联订单编号</li>
     *                       <li>sku - 商品唯一标识</li>
     *                       <li>quantity - 商品数量</li>
     *                     </ul>
     * @return 响应实体，包含：
     *         <ul>
     *           <li>成功：状态码200+空数据</li>
     *           <li>失败：状态码400+错误原因</li>
     *         </ul>
     * @throws Exception 当出现以下情况时返回400错误：
     *                               <ul>
     *                                 <li>订单不存在</li>
     *                                 <li>商品SKU无效</li>
     *                                 <li>数量格式错误</li>
     *                                 <li>明细已存在</li>
     *                               </ul>
     */
    @PostMapping("/inboundOrderDetail/add")
    public ResponseEntity<AjaxResult> addInboundOrderDetail(@RequestBody InboundOrderDetailAddDTO detailAddDTO) {
        // 验证 orderNumber 是否存在
        if (inboundOrderService.getByOrderNumber(detailAddDTO.getOrderNumber()) == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error("入库单编号不存在"));
        }

        // 验证 sku 是否存在
        if (productService.findBySku(detailAddDTO.getSku()) == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error("商品SKU不存在"));
        }

        // 验证 quantity 是否为非负整数
        if (!String.valueOf(detailAddDTO.getQuantity()).matches("^[0-9]+$")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error("商品数量必须为非负整数"));
        }

        // 验证是否已存在相同的 orderNumber 和 sku
        InboundOrderDetail existingDetail = inboundOrderDetailService.getByOrderNumberAndSku(
                detailAddDTO.getOrderNumber(), detailAddDTO.getSku());
        if (existingDetail != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error("该入库单明细已存在"));
        }

        // 创建新入库单明细
        InboundOrderDetail newDetail = new InboundOrderDetail(
                null, // id 由数据库自增
                detailAddDTO.getOrderNumber(),
                detailAddDTO.getSku(),
                detailAddDTO.getQuantity()
        );
        Boolean success = inboundOrderDetailService.addOneInboundOrderDetail(newDetail);
        if (success) {
            return ResponseEntity.ok().body(AjaxResult.success());
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error("添加失败"));
        }
    }

    /**
     * 修改入库单明细
     *
     * <p>更新现有明细的数量信息，需满足：
     * <ul>
     *   <li>明细必须存在</li>
     *   <li>关联订单和SKU不可修改</li>
     *   <li>新数量必须有效</li>
     * </ul>
     *
     * @param detailInfoDTO 更新数据，包含：
     *                      <ul>
     *                        <li>orderNumber - 订单编号（不可变）</li>
     *                        <li>sku - 商品SKU（不可变）</li>
     *                        <li>quantity - 新数量值</li>
     *                      </ul>
     * @return 响应实体，包含：
     *         <ul>
     *           <li>成功：状态码200+空数据</li>
     *           <li>失败：状态码400+错误原因</li>
     *         </ul>
     * @throws Exception 当出现以下情况时返回400错误：
     *                               <ul>
     *                                 <li>明细不存在</li>
     *                                 <li>数量格式无效</li>
     *                                 <li>更新操作失败</li>
     *                               </ul>
     */
    @PostMapping("/inboundOrderDetail/update")
    public ResponseEntity<AjaxResult> updateInboundOrderDetail(@RequestBody InboundOrderDetailInfoDTO detailInfoDTO) {
        // 验证 orderNumber 是否存在
        if (inboundOrderService.getByOrderNumber(detailInfoDTO.getOrderNumber()) == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error("入库单编号不存在"));
        }

        // 验证 sku 是否存在
        if (productService.findBySku(detailInfoDTO.getSku()) == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error("商品SKU不存在"));
        }

        // 验证是否已存在
        InboundOrderDetail existingDetail = inboundOrderDetailService.getByOrderNumberAndSku(
                detailInfoDTO.getOrderNumber(), detailInfoDTO.getSku());
        if (existingDetail == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error("入库单明细不存在"));
        }

        // 验证 quantity 是否为非负整数
        if (detailInfoDTO.getQuantity() != null && !String.valueOf(detailInfoDTO.getQuantity()).matches("^[0-9]+$")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error("商品数量必须为非负整数"));
        }

        // 更新入库单明细
        InboundOrderDetail updatedDetail = new InboundOrderDetail(
                existingDetail.getId(),
                detailInfoDTO.getOrderNumber(),
                detailInfoDTO.getSku(),
                detailInfoDTO.getQuantity() != null ? detailInfoDTO.getQuantity() : existingDetail.getQuantity()
        );
        Boolean success = inboundOrderDetailService.updateById(updatedDetail);
        if (success) {
            return ResponseEntity.ok().body(AjaxResult.success());
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error("修改失败"));
        }
    }

    /**
     * 删除入库单明细
     *
     * <p>根据订单编号和SKU删除指定明细记录
     *
     * @param detailRemoveDTO 删除条件，包含：
     *                        <ul>
     *                          <li>orderNumber - 订单编号</li>
     *                          <li>sku - 商品SKU</li>
     *                        </ul>
     * @return 响应实体，包含：
     *         <ul>
     *           <li>成功：状态码200+成功消息</li>
     *           <li>失败：状态码400+错误原因</li>
     *         </ul>
     * @throws Exception 当明细不存在或删除失败时返回400错误
     */
    @PostMapping("/inboundOrderDetail/remove")
    public ResponseEntity<AjaxResult> removeInboundOrderDetail(@RequestBody InboundOrderDetailRemoveDTO detailRemoveDTO) {
        InboundOrderDetail detail = inboundOrderDetailService.getByOrderNumberAndSku(
                detailRemoveDTO.getOrderNumber(), detailRemoveDTO.getSku());
        if (detail == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error("没有该入库单明细"));
        }

        Boolean success = inboundOrderDetailService.deleteByOrderNumberAndSku(
                detailRemoveDTO.getOrderNumber(), detailRemoveDTO.getSku());
        if (success) {
            return ResponseEntity.ok().body(AjaxResult.success("删除成功"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error("删除失败"));
        }
    }


    /**
     * 批量查询入库单明细
     *
     * <p>根据多个订单编号查询所有关联明细（不分页），用于批量操作场景
     *
     * @param orderNumbers 订单编号列表，格式：
     *                     ["orderNumber1", "orderNumber2", ...]
     * @return 响应实体，包含：
     *         <ul>
     *           <li>成功：状态码200+明细列表(List&lt;InboundOrderDetailInfoVo&gt;)</li>
     *           <li>失败：状态码400/404+错误信息</li>
     *         </ul>
     * @see InboundOrderDetailInfoVo 明细信息VO
     */
    @PostMapping("/inboundOrderDetail/findByOrderNumbers")
    public ResponseEntity<AjaxResult> findInboundOrderDetailsByOrderNumbers(@RequestBody List<String> orderNumbers) {
        try {
            if (orderNumbers == null || orderNumbers.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(AjaxResult.error(400, "入库单编号列表不能为空"));
            }

            List<InboundOrderDetail> details = inboundOrderDetailService.list(
                    new QueryWrapper<InboundOrderDetail>().in("order_number", orderNumbers));

            if (details.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(AjaxResult.error(404, "未找到匹配的入库单明细记录"));
            }

            List<InboundOrderDetailInfoVo> detailVoList = details.stream().map(detail -> new InboundOrderDetailInfoVo(
                    detail.getOrderNumber(),
                    detail.getSku(),
                    detail.getQuantity()
            )).collect(Collectors.toList());

            return ResponseEntity.ok().body(AjaxResult.success(detailVoList));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AjaxResult.error(400, e.getMessage()));
        }
    }
}