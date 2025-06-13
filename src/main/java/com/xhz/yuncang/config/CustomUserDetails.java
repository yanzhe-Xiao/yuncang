package com.xhz.yuncang.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import java.util.Collection;

/**
 * 自定义用户详情类
 * 继承Spring Security的User类，用于封装用户认证信息并扩展用户类型属性
 */
public class CustomUserDetails extends User {
    private final String userType; // 用户类型（如："管理员", "操作员", "客户"）

    /**
     * 构造方法（使用默认用户状态）
     *
     * @param username    用户名
     * @param password    密码
     * @param authorities 权限集合
     * @param userType    用户类型
     */
    public CustomUserDetails(String username, String password,
                             Collection<? extends GrantedAuthority> authorities,
                             String userType) {
        super(username, password, authorities);
        this.userType = userType;
    }

    /**
     * 构造方法（自定义用户状态）
     * @param username 用户名
     * @param password 密码
     * @param enabled 账户是否启用
     * @param accountNonExpired 账户是否未过期
     * @param credentialsNonExpired 凭证是否未过期
     * @param accountNonLocked 账户是否未锁定
     * @param authorities 权限集合
     * @param userType 用户类型
     */
    public CustomUserDetails(String username, String password, boolean enabled, boolean accountNonExpired,
                             boolean credentialsNonExpired, boolean accountNonLocked,
                             Collection<? extends GrantedAuthority> authorities, String userType) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.userType = userType;
    }

    /**
     * 获取用户类型
     * @return 用户类型字符串
     */
    public String getUserType() {
        return userType;
    }
}