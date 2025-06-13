package com.xhz.yuncang.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * 用户分页响应视图对象
 * 用于向前端返回用户列表的分页数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserListPageVo {
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
     * 当前页用户信息列表
     */
    private List<UserInfoVo> userInfoVoList;
}