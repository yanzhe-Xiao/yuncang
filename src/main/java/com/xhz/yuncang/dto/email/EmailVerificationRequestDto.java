package com.xhz.yuncang.dto.email;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 邮箱验证码验证请求数据传输对象
 * 用于接收前端验证邮箱验证码的请求数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EmailVerificationRequestDto {
    /**
     * 目标邮箱地址
     */
    private String email;

    /**
     * 验证码
     */
    private String code;
}