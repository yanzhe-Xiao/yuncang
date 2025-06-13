package com.xhz.yuncang.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * 通用分页响应视图对象
 * 用于封装分页查询结果，支持泛型以适应不同类型的数据返回
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PageVo<T> {

    /**
     * 当前页码
     */
    private Integer nowPage;

    /**
     * 上一页页码
     */
    private Integer beforePage;

    /**
     * 下一页页码
     */
    private Integer nextPage;

    /**
     * 总页数
     */
    private Integer totalPage;

    /**
     * 当前页数据列表
     */
    private List<T> records;
}