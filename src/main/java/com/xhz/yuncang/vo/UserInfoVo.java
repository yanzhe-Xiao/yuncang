package com.xhz.yuncang.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 用户信息视图对象
 * 用于向前端展示用户基本信息的数据模型
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserInfoVo {
    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名（登录名）
     */
    private String username;

    /**
     * 用户类型（如：管理员、操作员、客户）
     */
    private String userType;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 性别（男、女、保密）
     */
    private String gender;
}