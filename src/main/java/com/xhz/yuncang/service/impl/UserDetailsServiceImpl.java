package com.xhz.yuncang.service.impl;

import com.xhz.yuncang.config.CustomUserDetails;
import com.xhz.yuncang.entity.User;
import com.xhz.yuncang.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>Package Name: com.xhz.yuncang.service.impl </p>
 * <p>Description:  </p>
 * <p>Create Time: 2025/6/2 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since jdk-21
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserService userService; // 注入您现有的 UserService (或者直接用 UserRepository)

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.findByUname(username);
        if(user == null) {
            throw new UsernameNotFoundException("用户 " + username + " 不存在");
        }
        // --- 阻止userId带#的用户登录 ---
        // 检查 userId 是否以 "#_" 开头
        if (user.getUserId() != null && user.getUserId().startsWith("#_")) {
            // 如果是，直接抛出 DisabledException，认证流程会立即中止
            throw new DisabledException("该账户已被系统禁用，无法登录。");
        }
        String userTypeFromDb = user.getUserType(); // 获取 "管理员", "操作员", 或 "客户"
        List<GrantedAuthority> authorities = new ArrayList<>(); //
        authorities.add(new SimpleGrantedAuthority("ROLE_"+userTypeFromDb));

        // 返回 CustomUserDetails 实例
        return new CustomUserDetails(
                user.getUsername(),
                user.getPassword(),   // 数据库中存储的、已加密的密码
                authorities,          // 构建好的权限列表
                userTypeFromDb        // 原始的 userType 字符串
        );
    }
}
