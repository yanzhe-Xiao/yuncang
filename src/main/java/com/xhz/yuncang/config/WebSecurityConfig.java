package com.xhz.yuncang.config;

import com.alibaba.fastjson2.JSON;
import com.xhz.yuncang.service.impl.UserDetailsServiceImpl;
import com.xhz.yuncang.utils.JWTUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.ConcurrentSessionFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;

import java.io.IOException;
import java.util.HashMap;

/**
 * Spring Security配置类
 * 配置应用的安全策略，包括认证、授权和过滤器链
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // 保持，用于方法级安全 @PreAuthorize 等
public class WebSecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter; // 注入JWT 过滤器



    /**
     * 配置安全过滤器链
     * @param http HttpSecurity对象
     * @return 安全过滤器链
     * @throws Exception 配置过程中可能出现的异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)

                // 核心改变：将会话管理策略设置为 STATELESS (无状态)
                // 这会完全禁用 HttpSession，Spring Security 不会创建或使用它
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login", "/register","/api/email/send-password-reset-code","/user/find-password").permitAll()
                        .anyRequest().authenticated()
//                                .anyRequest().permitAll()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        // 只需添加你的 JWT 过滤器
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // 不再需要 UserDetailsServiceImpl 的注入，除非其他地方用
        // 不再需要 ConcurrentSessionFilter
        // 不再需要 SessionRegistry
        // 不再需要 HttpSessionEventPublisher
        // 不再需要 CustomSessionInformationExpiredStrategy

        return http.build();
    }

    /**
     * 配置认证管理器
     * @param authenticationConfiguration 认证配置
     * @return 认证管理器
     * @throws Exception 配置过程中可能出现的异常
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }



}