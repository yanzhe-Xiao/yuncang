package com.xhz.yuncang.vo.shelf;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 全部货架信息视图对象
 * 用于向前端返回所有货架详细信息的集合数据模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllShelfInfoVo {
    /**
     * 货架信息列表
     */
    private List<ShelfInfoVo> details;
}