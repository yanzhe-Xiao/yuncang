package com.xhz.yuncang.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 用户信息修改数据传输对象
 * 用于接收前端修改用户信息的请求数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserInfoDTO {
    /**
     * 用户名（登录名）
     */
    private String username;

    /**
     * 用户类型
     */
    private String userType;

    /**
     * 密码
     */
    private String password;

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