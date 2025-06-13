package com.xhz.yuncang.controller;

import com.xhz.yuncang.dto.email.EmailVerificationRequestDto;
import com.xhz.yuncang.dto.email.TargetEmailDto;
import com.xhz.yuncang.entity.User;
import com.xhz.yuncang.service.UserService;
import com.xhz.yuncang.utils.AjaxResult;
import com.xhz.yuncang.utils.Constants;
import com.xhz.yuncang.utils.SendMailUtil;
import org.slf4j.Logger; // 导入 SLF4J Logger
import org.slf4j.LoggerFactory; // 导入 SLF4J LoggerFactory
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>Package Name: com.xhz.yuncang.controller </p>
 * <p>Description: 用于发送和验证邮件验证码的控制器。 </p>
 * <p>Create Time: 2025/6/3 </p>
 * <p>Update Time: 2025/6/3 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.1
 * @since jdk-8
 */
@RestController
@RequestMapping("/api/email")
public class SendEmailController {

    // 初始化 Logger
    private static final Logger logger = LoggerFactory.getLogger(SendEmailController.class);

    private final JavaMailSenderImpl javaMailSender;
    private final SendMailUtil sendMailUtil;
    private final UserService userService;

    @Autowired
    public SendEmailController(JavaMailSenderImpl javaMailSender, SendMailUtil sendMailUtil, UserService userService) {
        this.javaMailSender = javaMailSender;
        this.sendMailUtil = sendMailUtil;
        this.userService = userService;
    }

    /**
     * 为未登录用户（例如密码重置）发送验证码。
     * 需要前端提供用户名和该用户名注册的邮箱。
     * @param targetEmailDto 包含 targetUserName 和 targetEmail
     * @return ResponseEntity 包含操作结果
     */
    @PostMapping("/send-password-reset-code") // 端点名称更明确
    public ResponseEntity<AjaxResult> sendPasswordResetCode(@RequestBody TargetEmailDto targetEmailDto) {
        // 1. 参数校验
        if (targetEmailDto.getTargetUserName() == null || targetEmailDto.getTargetUserName().trim().isEmpty() ||
                targetEmailDto.getTargetEmail() == null || targetEmailDto.getTargetEmail().trim().isEmpty()) {
            logger.warn("发送密码重置验证码请求缺少用户名或邮箱。");
            return ResponseEntity.badRequest().body(AjaxResult.error(400, "必须提供用户名和邮箱。"));
        }

        String username = targetEmailDto.getTargetUserName();
        String providedEmail = targetEmailDto.getTargetEmail();

        // 2. 根据用户名查找用户
        User user = userService.findByUname(username);
        if (user == null) {
            logger.warn("尝试为不存在的用户 {} 发送密码重置验证码。", username);
            // 出于安全考虑，不明确提示“用户不存在”，而是统一提示操作失败或信息不匹配
            return ResponseEntity.badRequest().body(AjaxResult.error(404, "用户名或邮箱信息不正确。"));
        }

        // 3. 校验提供的邮箱是否与用户的注册邮箱匹配
        // 假设 User 实体有 getEmail() 方法
        if (user.getPhone() == null || !user.getPhone().equalsIgnoreCase(providedEmail)) {
            logger.warn("用户 {} 提供的邮箱 {} 与其注册邮箱不匹配。", username, providedEmail);
            return ResponseEntity.badRequest().body(AjaxResult.error(400, "用户名或邮箱信息不正确。"));
        }

        // 4. 生成并发送验证码
        SimpleMailMessage message = new SimpleMailMessage();
        String code = sendMailUtil.generateVerifyCode(6);

        message.setFrom(Constants.SENDER_EMAIL);
        message.setTo(user.getPhone()); // 确保发送到用户数据库中记录的邮箱
        message.setSubject("[YunCang密码重置] Verification Code");
        message.setText("【YunCang密码重置】您正在进行密码重置操作，验证码为：" + code +
                "，有效时间为5分钟。如果不是您本人操作，请忽略此邮件。\n" +
                "【YunCang Password Reset】You are resetting your password. Your verification code is: " + code +
                ", valid for 5 minutes. If you did not request this, please ignore this email.");

        try {
            javaMailSender.send(message);
            logger.info("密码重置验证码已成功发送至用户 {} 的邮箱: {}", username, user.getPhone());
            // 将验证码保存到Redis，与用户的邮箱关联
            sendMailUtil.saveCodeToRedis(user.getPhone(), code);
            return ResponseEntity.ok(AjaxResult.success("验证码已发送至您的注册邮箱，请注意查收。"));
        } catch (MailSendException e) {
            logger.error("发送密码重置邮件至 {} 失败: 邮件服务器错误或邮箱地址问题。", user.getPhone(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(500, "发送验证邮件失败，请稍后重试。"));
        } catch (Exception e) {
            logger.error("发送密码重置邮件至 {} 时发生意外错误: ", user.getPhone(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(AjaxResult.error(500, "发送邮件时发生意外错误，请联系管理员。"));
        }
    }

//    /**
//     * 验证邮箱和验证码（例如用于密码重置流程）。
//     * @param verificationRequest 包含 email 和 code
//     * @return ResponseEntity 包含操作结果
//     */
//    @PostMapping("/verify-code")
//    public ResponseEntity<AjaxResult> verifyCode(@RequestBody EmailVerificationRequestDto verificationRequest) {
//        if (verificationRequest.getEmail() == null || verificationRequest.getEmail().isEmpty() ||
//                verificationRequest.getCode() == null || verificationRequest.getCode().isEmpty()) {
//            return ResponseEntity.badRequest().body(AjaxResult.error(400, "必须提供邮箱和验证码。"));
//        }
//
//        boolean isValid = sendMailUtil.verifyCodeFromRedis(
//                verificationRequest.getEmail(),
//                verificationRequest.getCode()
//        );
//
//        if (isValid) {
//            logger.info("邮箱 {} 的验证码验证成功。", verificationRequest.getEmail());
//            // 在密码重置流程中，验证成功后，前端通常会引导用户进入设置新密码的页面
//            // 这里后端确认验证码有效即可。
//            return ResponseEntity.ok(AjaxResult.success("验证码正确。"));
//        } else {
//            logger.warn("邮箱 {} 的验证码验证失败。验证码无效或已过期。", verificationRequest.getEmail());
//            return ResponseEntity.badRequest().body(AjaxResult.error(400, "验证码无效或已过期。"));
//        }
//    }
}