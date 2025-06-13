package com.xhz.yuncang.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 用户登录数据传输对象
 * 用于接收前端用户登录请求的数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserLoginDTO {
    /**
     * 用户名（登录名）
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 用户类型
     */
    private String userType;

    /**
     * 是否七天内免登录
     */
    private Boolean remember;
}