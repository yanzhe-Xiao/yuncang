package com.xhz.yuncang.vo.path;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * 全部搬运车路径信息视图对象
 * 用于向前端返回所有搬运车的路径规划集合数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AllCarPath {
    /**
     * 搬运车路径详情列表
     */
    private List<CarStorageToShelf> details;
}