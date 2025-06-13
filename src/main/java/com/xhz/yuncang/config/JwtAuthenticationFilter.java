package com.xhz.yuncang.config;

import com.alibaba.fastjson2.JSON;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.xhz.yuncang.service.impl.UserDetailsServiceImpl;
import com.xhz.yuncang.utils.Constants;
import com.xhz.yuncang.utils.JWTUtil;
// import com.xhz.yuncang.config.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User; // 可以用 Spring Security 默认的 User
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT认证过滤器
 * 用于从HTTP请求中提取JWT令牌，验证其有效性，并将用户信息设置到Spring Security上下文中
 */
@Component // 将其声明为 Spring Bean
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // Redis中存储的token键前缀，需与登录时存入的前缀一致
    private static final String REDIS_TOKEN_KEY_PREFIX = Constants.LOGIN_TOKEN_PREFIX; //  <-- 请根据你的实际情况修改!

    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl; // 注入UserDetailsService (用于登录认证)
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 对登录接口直接放行，不进行JWT校验
        if (request.getRequestURI().equals("/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = extractJwtFromRequest(request);

        // 如果没有 token 或者 SecurityContext 中已有认证信息，则直接放行
        if (!StringUtils.hasText(jwt) || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // --- 核心校验逻辑 ---
        if (JWTUtil.verify(jwt)) {
            String redisKey = Constants.LOGIN_TOKEN_PREFIX + jwt;
            // 检查 Token 是否在 Redis 中存在
            if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(redisKey))) {
                // Token 有效且存在于 Redis，构建认证信息
                String username = JWTUtil.getTokenInfo(jwt).getClaim("username").asString();

                // 从数据库或缓存中加载完整的 UserDetails，以获取权限等信息
                UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                // 认证成功，继续过滤器链
                filterChain.doFilter(request, response);
                return; // 处理完毕，返回
            }
        }

        // --- 统一处理所有认证失败的情况 ---
        // 走到这里，意味着：
        // 1. JWT 格式或签名无效
        // 2. JWT 在 Redis 中不存在（被挤下线或已过期）
        System.out.println("JWT 认证失败，将返回 401 Unauthorized。");

        // 设置响应状态为 401 Unauthorized
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");

        // 创建一个更明确的错误信息体
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("code", 401);
        errorDetails.put("message", "认证失败或会话已过期，请重新登录。"); // 统一提示信息

        response.getWriter().write(JSON.toJSONString(errorDetails));

        // 注意：不调用 filterChain.doFilter(request, response)，直接返回，中断请求。
    }

    /**
     * 从HTTP请求头中提取JWT令牌
     * @param request HTTP请求
     * @return JWT令牌字符串（不含Bearer前缀），若不存在则返回null
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // 去掉 "Bearer " 前缀
        }
        return null;
    }
}