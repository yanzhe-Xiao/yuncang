package com.xhz.yuncang.dto.email;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 目标邮箱数据传输对象
 * 用于封装需要发送邮件的目标用户信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TargetEmailDto {
    /**
     * 目标用户名称
     */
    private String targetUserName;

    /**
     * 目标邮箱地址
     */
    private String targetEmail;
}