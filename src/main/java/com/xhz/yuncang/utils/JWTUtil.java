package com.xhz.yuncang.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;

public class JWTUtil {
    private static final String SIGN="!@#E3v_*0d^m%";

    private static final Algorithm ALGORITHM=Algorithm.HMAC256(SIGN);

    private static final TimeZone UTC_ZONE = TimeZone.getTimeZone("UTC");

    /**
     * 生成Token
     * !!!:注意这里数据（value）需要全部转化为String类型
     * @param map k，v  Map <String,String>
     * @param min 过期时间（分钟）
     * @return  token
     *
     */
    public static String getToken(Map<String ,String> map,int min){
        Calendar calendar = Calendar.getInstance(UTC_ZONE);
        calendar.add(Calendar.MINUTE,min);

        JWTCreator.Builder builder = JWT.create();
        map.forEach(builder::withClaim);

        return builder.withExpiresAt(calendar.getTime()).sign(ALGORITHM);
    }

    /**
     * 验证token
     */
    public static boolean verify(String token){
        try {
            JWT.require(ALGORITHM).build().verify(token);
        } catch (JWTVerificationException e) {
            return false;
        }
        return true;
    }

    /**
     * 获取token内的信息
     */
    public static DecodedJWT getTokenInfo(String token){
        return JWT.require(ALGORITHM).build().verify(token);
    }
}
