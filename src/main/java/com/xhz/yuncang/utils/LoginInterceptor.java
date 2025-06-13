package com.xhz.yuncang.utils;

import com.xhz.yuncang.vo.UserVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LoginInterceptor implements HandlerInterceptor{
    // !!!注意这里不能注入，LoginInterceptor是自己写的类没加入IOC管理中
    private StringRedisTemplate stringRedisTemplate;

    public LoginInterceptor(StringRedisTemplate stringRedisTemplate){
        this.stringRedisTemplate=stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("=== 进入拦截器 ===");

        // 放行 OPTIONS 预检请求
        if (HttpMethod.OPTIONS.toString().equals(request.getMethod())){
            response.setStatus(HttpServletResponse.SC_OK);//返回 200
            return true;//放行，不执行后续逻辑
        }
        String contextPath = request.getContextPath();
        String servletPath = request.getServletPath();
        System.out.println("contextPath"+contextPath);
        System.out.println("servletPath"+servletPath);
        //1、获取请求头的token
        String tokenHead = request.getHeader("Authorization");
        if (tokenHead == null || !tokenHead.startsWith("Bearer ")){
            //没有Token 或  Token不符合要求的 ->未登录
            response.setStatus(401);
            System.out.println("没有该Token");
            System.out.println(tokenHead);
            return false;//拦截
        }
        //获取token（去掉头的Bearer 部分）
        String token=tokenHead.substring(7);
        System.out.println("token: "+token);
        //2、基于token获取redis的用户
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries("login:token:" + token);
        System.out.println("userMap: "+userMap);
        if (userMap.isEmpty()){
            //token有，但redis没有token，说明Token错误   或    redis 过期
            response.setStatus(401);
            System.out.println("redis没有该Token/redis过期");
            return false; //拦截
        }

        //redis中有token->说明是已登录的
        if (!JWTUtil.verify(token)){
            //redis里有token，但token 过期了
            System.out.println("token 过期了");
            //重新申请token（15min）
            Map<String, String> ssMap = TypeConversionUtil.oo2ssMap(userMap);
            String newToken = JWTUtil.getToken(ssMap, Constants.TOKEN_TTL);
            //获取redis剩余时间
            Long oldRedisTime = stringRedisTemplate.getExpire("login:token:" + token, TimeUnit.MINUTES);
            if (oldRedisTime==null||oldRedisTime<=0){
                //若剩余时间为非法值，则更新为30min
                oldRedisTime=Constants.REDIS_NO_REM_TTL.longValue();
            }
            //放入redis(redis用的是剩余时间)
            stringRedisTemplate.opsForHash().putAll("login:token:"+newToken,ssMap);
            stringRedisTemplate.expire("login:token:"+newToken,oldRedisTime, TimeUnit.MINUTES);
            //旧的redis销毁
            stringRedisTemplate.delete("login:token:" + token);
            // 6. 返回新Token到客户端
            response.setHeader("Authorization", "Bearer "+newToken);
        }else {
            //redis有,且token未过期
            System.out.println("===redis有,且token未过期===");
            System.out.println("userMap:  "+userMap);
            //获取redis剩余时间
            Long oldRedisTime2 = stringRedisTemplate.getExpire("login:token:" + token, TimeUnit.MINUTES);
            if (oldRedisTime2==null||oldRedisTime2<=30){
                //若剩余时间为非法值或不满30min，则更新为30min
                oldRedisTime2=Constants.REDIS_NO_REM_TTL.longValue();
            }
            //5、刷新redis有效期（30mih）
            //注意：token有效期刷新不了
            stringRedisTemplate.expire("login:token:" + token,oldRedisTime2, TimeUnit.MINUTES);
        }
        //存在，将HashMap转为UserDTO
        UserVo userVo = new UserVo(userMap.get("username").toString(), userMap.get("userType").toString());
        //4、 保存用户信息到ThreadLocal
        UserHolder.saveUser(userVo);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    /**
     *移除用户，防止内存泄漏！！！！！！！！！
     *
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        System.out.println("=== 退出拦截器 ===");
        UserHolder.removeUser();
    }
}
