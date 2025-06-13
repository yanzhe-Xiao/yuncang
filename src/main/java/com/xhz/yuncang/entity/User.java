package com.xhz.yuncang.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 用户实体类
 * 用于存储系统用户的基本信息和认证信息
 */
@Data
@TableName("user")
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * 数据库主键ID，自动递增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户编号（唯一）
     * 系统内部使用的用户标识
     */
    private String userId;

    /**
     * 用户名
     * 用于登录系统的账号
     */
    private String username;

    /**
     * 用户类型
     * 可选值：管理员、操作员、客户
     */
    private String userType;

    /**
     * 密码（加密存储）
     * 用户登录密码的加密形式
     */
    private String password;

    /**
     * 昵称
     * 用户显示名称
     */
    private String nickname;

    /**
     * 手机号码
     * 用户联系电话
     */
    private String phone;

    /**
     * 性别
     * 可选值：男、女、保密
     */
    private String gender;

    // 注释掉的字段：
    // 建议启用以下字段以记录用户创建和修改时间
    // @TableField(fill = FieldFill.INSERT)
    // private Date createTime;
    //
    // @TableField(fill = FieldFill.INSERT_UPDATE)
    // private Date updateTime;
}