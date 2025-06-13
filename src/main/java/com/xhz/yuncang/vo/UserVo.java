package com.xhz.yuncang.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 用户认证响应视图对象
 * 用于登录认证成功后返回用户基本信息和认证令牌
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserVo {
    /**
     * 用户名（登录名）
     */
    private String username;

    /**
     * 用户类型（如：admin-管理员，operator-操作员）
     */
    private String userType;

    /**
     * JWT认证令牌
     */
    private String token;

    /**
     * 是否需要管理员同意
     */
    private boolean isNeedAuth;

    /**
     * 仅初始化用户名和用户类型的构造函数
     * @param username 用户名
     * @param userType 用户类型
     */
    public UserVo(String username, String userType) {
        this.username = username;
        this.userType = userType;
    }
}