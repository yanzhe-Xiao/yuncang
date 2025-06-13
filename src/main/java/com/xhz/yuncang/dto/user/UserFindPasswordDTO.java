package com.xhz.yuncang.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 用户找回密码数据传输对象
 * 用于接收前端找回密码请求的数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserFindPasswordDTO {
    /**
     * 用户名
     */
    private String userName;

    /**
     * 注册邮箱
     */
    private String email;

    /**
     * 验证码
     */
    private String code;

    /**
     * 新密码（需加密存储）
     */
    private String newPassword;
}