package com.xhz.yuncang.utils;

import com.xhz.yuncang.vo.UserVo;

/**
 * 使用 ThreadLocal:
 * 同一个线程中共享当前登录用户的信息
 * 在各层直接获取用户信息（无需参数传递）
 * ！！!注意：请求结束时要调remove，防止内存泄漏
 */
public class UserHolder {

    private static final ThreadLocal<UserVo> tl=new ThreadLocal<>();

    public static void saveUser(UserVo userVo){
        tl.set(userVo);
    }

    public static UserVo getUser(){
        return tl.get();
    }

    public static void removeUser(){
        tl.remove();
    }
}
