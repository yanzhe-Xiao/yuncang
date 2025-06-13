package com.xhz.yuncang.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 用户注册数据传输对象
 * 用于接收前端用户注册请求的数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserRegisterDTO {
    /**
     * 用户类型
     */
    private String userType;

    /**
     * 用户名（登录名）
     */
    private String username;

    /**
     * 密码（需加密存储）
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 性别
     */
    private String gender;

    /**
     * 手机号码
     */
    private String phone;
}