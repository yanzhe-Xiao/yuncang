package com.xhz.yuncang.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * <p>Package Name: com.xhz.yuncang.dto </p>
 * <p>Description:  </p>
 * <p>Create Time: 2025/6/9 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since jdk-21
 */
@Data
public class UpdateOrderRequestDTO {

    @NotBlank
    private String orderNumber; // 新的订单号

    // 允许details为空，表示清空明细
    private List<OrderDetailDTO> details;

    @Data
    public static class OrderDetailDTO {
        @NotBlank
        private String name; // 商品名称
        @NotNull
        private Long quantity; // 使用Long对应数据库BIGINT, 注意前端number要能转成Long
    }
}
