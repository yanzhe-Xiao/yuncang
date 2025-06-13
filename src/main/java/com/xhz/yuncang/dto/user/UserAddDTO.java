package com.xhz.yuncang.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 用户添加数据传输对象
 * 用于接收前端添加用户的请求数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserAddDTO {
    /**
     * 用户名（登录名）
     */
    private String username;

    /**
     * 密码（需加密存储）
     */
    private String password;

    /**
     * 用户类型
     */
    private String userType;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 性别
     */
    private String gender;
}