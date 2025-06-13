package com.xhz.yuncang.vo.shelf;

import com.xhz.yuncang.vo.ProductSimpleVo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * 仓库存储状态视图对象
 * 用于向前端返回仓库整体存储情况的统计数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AllStorageVo {
    /**
     * 仓库总存储容量（单位：kg）
     */
    private Double allStorageWeights;

    /**
     * 当前已使用存储量（单位：kg）
     */
    private Double currentStorageWeights;

    /**
     * 库存商品简要信息列表
     */
    private List<ProductSimpleVo> details;
}