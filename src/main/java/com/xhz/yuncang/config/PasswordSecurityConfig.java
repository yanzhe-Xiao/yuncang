package com.xhz.yuncang.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码安全配置类
 * 配置Spring Security的密码编码器，用于密码加密和验证
 */
@Configuration
public class PasswordSecurityConfig {
    /**
     * 配置密码编码器
     * 使用BCrypt算法进行密码加密，强度因子设置为10
     * @return BCrypt密码编码器实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 强度因子影响哈希计算的性能和安全性，默认值为10
        return new BCryptPasswordEncoder(10);
    }
}