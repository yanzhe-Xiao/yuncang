package com.xhz.yuncang.config;

import com.xhz.yuncang.utils.LoginInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web应用配置类
 * 配置跨域资源共享(CORS)和请求拦截器
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    /**
     * 此处用来前后端跨域问题的解决
     * @CrossOrigin 也可以用来跨域，但是cookie的传递无法解决
     *          即每次从前端的请求的cookie不是同一个
     * 如果请求需要携带 Cookie（withCredentials: true），则后端必须明确指定 Access-Control-Allow-Origin
     *         为具体的域名（如 http://localhost:5173），不能使用通配符 *
     *
     * 前端要设置：  axios.defaults.withCredentials = true;
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:13677") // 替换为前端实际域名和端口
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true) // 允许携带 Cookie
                .exposedHeaders(HttpHeaders.AUTHORIZATION) // 关键：暴露 Authorization 头
                .maxAge(3600);
    }

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 指定拦截器拦截哪些前端发到后端的路径
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor(stringRedisTemplate))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/register",
                        "/login",
                        "/api/email/**",
                        "/email/**",
                        "/user/**"
                );
    }
}
