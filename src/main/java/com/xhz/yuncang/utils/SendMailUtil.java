package com.xhz.yuncang.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component; // 声明为Spring组件

import java.util.Random;
import java.util.concurrent.TimeUnit; // 导入 TimeUnit

/**
 * <p>Package Name: com.xhz.yuncang.utils </p>
 * <p>Description: 发送邮箱及验证码处理的工具类 </p>
 * <p>Create Time: 2025/6/3 </p>
 * <p>Update Time: 2025/6/3 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.1
 * @since jdk-21
 */
@Component // 将此类声明为Spring管理的组件
public class SendMailUtil {

    private final StringRedisTemplate stringRedisTemplate; // 注入StringRedisTemplate

    // Redis中验证码键的前缀
    private static final String VERIFICATION_CODE_KEY_PREFIX = Constants.EMAIL_VERIFICATION_CODE_KEY_PREFIX;
    // 验证码有效期，单位：分钟
    private static final long VERIFICATION_CODE_EXPIRATION_MINUTES = Constants.EMAIL_VERIFICATION_CODE_EXPIRATION_MINUTES;

    @Autowired // 通过构造函数注入
    public SendMailUtil(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 生成指定长度的数字验证码
     * @param n 验证码长度
     * @return 生成的验证码字符串
     */
    public String generateVerifyCode(int n) {
        Random r = new Random();
        StringBuilder sb = new StringBuilder(); // 推荐使用 StringBuilder
        for (int i = 0; i < n; i++) {
            int ran1 = r.nextInt(10);
            sb.append(ran1); // 直接追加数字，String.valueOf() 不是必须的
        }
        // System.out.println("Generated Code: " + sb.toString()); // 日志输出，可选
        return sb.toString();
    }

    /**
     * 将验证码的MD5哈希值保存到Redis，并设置过期时间。
     * @param email 用于关联验证码的邮箱地址（或其他唯一标识）
     * @param code  要保存的明文验证码
     */
    public void saveCodeToRedis(String email, String code) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty.");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Code cannot be null or empty.");
        }

        String hashedCode = MD5Utils.encrypt(code); // 对验证码进行md5加密
        String redisKey = VERIFICATION_CODE_KEY_PREFIX + email;

        // 将加密后的验证码存入Redis，并设置5分钟过期
        stringRedisTemplate.opsForValue().set(redisKey, hashedCode, VERIFICATION_CODE_EXPIRATION_MINUTES, TimeUnit.MINUTES);
        // System.out.println("Saved to Redis: Key=" + redisKey + ", HashedValue=" + hashedCode + ", Expires in " + VERIFICATION_CODE_EXPIRATION_MINUTES + " minutes");
    }

    /**
     * 验证用户输入的验证码是否正确且在有效期内。
     * @param email     关联验证码的邮箱地址（或其他唯一标识）
     * @param inputCode 用户输入的明文验证码
     * @return 如果验证码正确且未过期则返回 true，否则返回 false。
     */
    public boolean verifyCodeFromRedis(String email, String inputCode) {
        if (email == null || email.trim().isEmpty()) {
            // System.err.println("Verification failed: Email is null or empty.");
            return false;
        }
        if (inputCode == null || inputCode.trim().isEmpty()) {
            // System.err.println("Verification failed: Input code is null or empty for email: " + email);
            return false;
        }

        String redisKey = VERIFICATION_CODE_KEY_PREFIX + email;
        String storedHashedCode = stringRedisTemplate.opsForValue().get(redisKey);

        if (storedHashedCode == null) {
            // 验证码不存在（可能已过期或从未发送）
            // System.out.println("Verification failed: Code not found or expired for email: " + email);
            return false;
        }

        String inputHashedCode = MD5Utils.encrypt(inputCode);

        if (storedHashedCode.equals(inputHashedCode)) {
            // 验证成功，从Redis中删除该验证码，防止重复使用
            stringRedisTemplate.delete(redisKey);
            // System.out.println("Verification successful for email: " + email + ". Code deleted from Redis.");
            return true;
        } else {
            // 验证码不匹配
            // System.out.println("Verification failed: Code mismatch for email: " + email);
            return false;
        }
    }
}