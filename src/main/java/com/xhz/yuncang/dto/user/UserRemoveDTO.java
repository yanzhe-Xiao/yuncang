package com.xhz.yuncang.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 用户移除数据传输对象
 * 用于接收前端删除用户的请求数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserRemoveDTO {
    /**
     * 用户名（登录名）
     */
    private String username;

    /**
     * 用户类型
     */
    private String userType;
}