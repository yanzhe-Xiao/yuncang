package com.xhz.yuncang.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xhz.yuncang.config.CustomUserDetails;
import com.xhz.yuncang.dto.user.*;
import com.xhz.yuncang.entity.Remind;
import com.xhz.yuncang.entity.User;
import com.xhz.yuncang.service.RemindService;
import com.xhz.yuncang.service.UserService;
import com.xhz.yuncang.utils.*;
import com.xhz.yuncang.vo.UserInfoVo;
import com.xhz.yuncang.vo.UserVo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
/**
 * 用户管理控制器
 *
 * <p>负责处理用户认证、注册、信息管理等核心功能，提供完整的用户生命周期管理。
 * 集成Spring Security实现安全认证，支持JWT令牌管理。
 *
 * <p>主要功能：
 * <ul>
 *   <li>用户登录认证（基于Spring Security）</li>
 *   <li>用户注册与信息管理</li>
 *   <li>密码找回与重置</li>
 *   <li>管理员用户管理功能</li>
 *   <li>用户信息查询</li>
 * </ul>
 *
 * @author xhz
 * @version 1.0
 * @see UserService 用户服务接口
 * @see CustomUserDetails 自定义用户详情
 * @see User 用户实体类
 * @see UserVo 用户视图对象
 */
@RestController
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(SendEmailController.class);
    /**
     * 用户服务实例
     */
    @Autowired
    private UserService userService;
    /**
     * Redis模板实例
     */
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    /**
     * 认证管理器实例
     */
    @Autowired
    private AuthenticationManager authenticationManager;
    /**
     * 根据用户名生成存储其当前活动Token的Redis键。
     *
     * @param username 用户名
     * @return Redis Key
     */
    private String getUserActiveTokenKey(String username) {
        // 使用一个清晰的前缀来区分这种键
        return "sso:user:active_token:" + username;
    }

    @Autowired
    private RemindService remindService;


    /**
     * 用户登录接口，采用纯Redis方案实现单点登录（会话挤出）。
     *
     * @param userLoginDTO 包含用户名、密码、用户类型和是否记住我的数据传输对象
     * @return 包含JWT Token的成功响应，或认证失败的错误响应
     */
    @PostMapping("/login")
    public ResponseEntity<AjaxResult> login(@RequestBody UserLoginDTO userLoginDTO) {
        System.out.println("UserController: login with pure Redis SSO: " + userLoginDTO);

        // 1. 创建 Spring Security 的 AuthenticationToken 用于认证
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userLoginDTO.getUsername(), userLoginDTO.getPassword());

        Authentication authentication;
        try {
            // 2. 使用 AuthenticationManager 进行认证，这会触发 UserDetailsServiceImpl 的 loadUserByUsername
            authentication = authenticationManager.authenticate(authenticationToken);
        } catch (BadCredentialsException e) {
            // 用户名或密码错误
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AjaxResult.error(401, "用户名或密码错误"));
        } catch (AuthenticationException e) {
            // 其他认证异常，例如用户被锁定、禁用等
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AjaxResult.error(401, "认证失败: " + e.getMessage()));
        }

        // 3. 认证成功，将认证信息存入 SecurityContextHolder
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 4. 从认证信息中获取自定义的 UserDetails 对象
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 5. 业务逻辑验证：检查请求的用户类型是否与数据库中存储的类型匹配
        if (!userDetails.getUserType().equals(userLoginDTO.getUserType())) {
            // 虽然用户名密码正确，但请求的 userType 不符
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AjaxResult.error(400, "用户类型不匹配"));
        }

        // --- 开始处理单点登录（会话挤出）的核心逻辑 ---

        // 6. 查找并使该用户的旧Token失效
        String userActiveTokenKey = getUserActiveTokenKey(userDetails.getUsername());
        String oldToken = stringRedisTemplate.opsForValue().get(userActiveTokenKey);

        if (StringUtils.hasText(oldToken)) {
            System.out.println("发现用户 '" + userDetails.getUsername() + "' 的旧Token，即将从Redis中删除: " + oldToken);
            // 删除存储Token详细信息的Hash
            stringRedisTemplate.delete(Constants.LOGIN_TOKEN_PREFIX + oldToken);
            // stringRedisTemplate.delete(userActiveTokenKey);
        }

        // 7. 生成新的JWT Token
        UserVo userVo = new UserVo(userDetails.getUsername(), userDetails.getUserType());
        Map<String, String> claimsMap = TypeConversionUtil.Obj2MapSS(userVo);
        String newToken = JWTUtil.getToken(claimsMap, Constants.TOKEN_TTL);

        long redisExpireTTL;
        // 根据“记住我”选项决定Redis中Token的过期时间
        if (userLoginDTO.getRemember()) {
            redisExpireTTL = Constants.REDIS_REM_TTL; // 7天
        } else {
            redisExpireTTL = Constants.REDIS_NO_REM_TTL; // 30分钟
        }

        // 8. 将新的Token信息存入Redis
        // 存储Token的详细信息，用于JWT过滤器验证
        stringRedisTemplate.opsForHash().putAll(Constants.LOGIN_TOKEN_PREFIX + newToken, claimsMap);
        stringRedisTemplate.expire(Constants.LOGIN_TOKEN_PREFIX + newToken, redisExpireTTL, TimeUnit.MINUTES);

        // 9. 更新 用户名 -> 新Token 的映射关系，并设置相同的过期时间
        stringRedisTemplate.opsForValue().set(userActiveTokenKey, newToken, redisExpireTTL, TimeUnit.MINUTES);

        System.out.println("为用户 '" + userDetails.getUsername() + "' 设置了新的有效Token，并已存入Redis。");
        String username = userLoginDTO.getUsername();
        remindService.saveRemind(new Remind(null, Constants.REMIND_SUCCESS, "用户成功登录", "于" + LocalDateTime.now() + ",时刻," +
                userLoginDTO.getUserType() + username + "成功登录了",
                LocalDateTime.now(), "1"));
//        return ResponseEntity.ok(AjaxResult.success(userVo));
        // --- 逻辑结束 ---

        // 10. 准备返回给前端的数据
        userVo.setToken(newToken);
        return ResponseEntity.ok(AjaxResult.success(userVo));
    }

    /**
     * 用户注册
     * （1）查找db，看看name是否有重名的
     * （2）有重名，返回400
     * （3）没有重名，则添加到db中
     * //     * @param userRegisterDTO  前端传给后端的数据
     *
     * @return UserVo
     */
    @PostMapping("/register")
    public ResponseEntity<AjaxResult> register(@RequestBody UserRegisterDTO userRegisterDTO) {
        if (userRegisterDTO == null || userRegisterDTO.getUsername() == null || userRegisterDTO.getUsername().isEmpty() ||
                userRegisterDTO.getUserType() == null || userRegisterDTO.getUserType().isEmpty() || userRegisterDTO.getPassword() == null ||
                userRegisterDTO.getPassword().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400, "信息不全"));
        }
        System.out.println(userRegisterDTO);
        User user = userService.findByUname(userRegisterDTO.getUsername());
        if (user != null) {
            //重名
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AjaxResult.error(400, "用户名重名"));
        }
//        //没有重名
//        String encrypted = MD5Utils.encrypt(userRegisterDTO.getPassword());
//        User userInserted = new User(null, "user_" + userRegisterDTO.getUsername(), userRegisterDTO.getUsername()
//                , userRegisterDTO.getUserType(), encrypted, null
//                , userRegisterDTO.getPhone(), null);
//        if (userService.addOneUser(userInserted)){
//            //添加成功
//            UserVo userVo = new UserVo(userInserted.getUsername(), userInserted.getUserType());
//            return ResponseEntity.ok().body(AjaxResult.success(userVo));
//        }else {
//            //添加失败
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(AjaxResult.error(500,"添加失败"));
//        }

        boolean b = userService.registerUserWithSpringSecurity(userRegisterDTO.getUsername(),
                userRegisterDTO.getUserType(),
                userRegisterDTO.getNickname(), userRegisterDTO.getPassword(), userRegisterDTO.getPhone(),
                userRegisterDTO.getGender());
        if (b) {
            UserVo userVo = new UserVo(userRegisterDTO.getUsername(), userRegisterDTO.getUserType());

            if (Objects.equals(userRegisterDTO.getUserType(), "客户")) {
                remindService.saveRemind(new Remind(null, Constants.REMIND_SUCCESS, "用户成功注册客户", "于" + LocalDateTime.now() + "时刻," +
                        "有一用户注册了权限是" + userRegisterDTO.getUserType() + "，用户名是" + userRegisterDTO.getUsername() + "的账号",
                        LocalDateTime.now(), "1"));
                userVo.setNeedAuth(true);
                return ResponseEntity.ok().body(AjaxResult.success(userVo));
            } else {
                remindService.saveRemind(new Remind(null, Constants.REMIND_ERROR, "用户注册管理员/操作员", "于" + LocalDateTime.now() + "时刻," +
                        "有一用户注册了权限是" + userRegisterDTO.getUserType() + "，用户名是" + userRegisterDTO.getUsername() + "的账号",
                        LocalDateTime.now(), "0"));
                userVo.setNeedAuth(false);
                return ResponseEntity.badRequest().body(AjaxResult.error(400,"管理员审核中..."));
            }
//            return ResponseEntity.ok().body(AjaxResult.success(userVo));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(AjaxResult.error(500, "添加失败"));
        }
    }


    /**
     * 返回用户管理的表（管理员）
     * 展示所有用户
     * ！！！注意： (1)要分页
     * (2)若页数（pageNo）小于等于0，或若页数（pageNo）大于总页数，返回400
     * (4)若当前页数为第一页，则它的上一页（beforePage）设为 -1；
     * (5)若当前页数为最后一页，则它的下一页(nextPage)设为 -1；
     * 所有后端发到前端的数据的数据类都必须是Vo类的（eg：User ×， UserInfoVo √）
     */
    @GetMapping("/user")
    @PreAuthorize("hasRole('管理员')")
    public ResponseEntity<AjaxResult> showUsersByPage(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "") String username
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            System.out.println("访问 /user 时的用户名: " + authentication.getName());
            System.out.println("访问 /user 时的用户权限: " + authentication.getAuthorities());
        } else {
            System.out.println("访问 /user 时用户未认证或认证信息为空");
        }
        if (current <= 0) {
            return ResponseEntity.badRequest().body(AjaxResult.error(400, "页数必须大于0"));
        }
        Page<User> page = new Page<>(current, pageSize);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        if (!username.isEmpty()) {
            queryWrapper.like("username", username);
        }

        Page<User> result = userService.page(page, queryWrapper);

        if (result.getPages() == 0) {
            return ResponseEntity.ok().body(AjaxResult.success("当前用户为空", ""));
        }
        if (current > result.getPages()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AjaxResult.error(400, "页数大于最大页数"));
        }

        int nowPage = (int) result.getCurrent();
        int beforePage = nowPage == 1 ? -1 : nowPage - 1;
        int nextPage = nowPage == (int) result.getPages() ? -1 : nowPage + 1;

        List<UserInfoVo> userInfoVoList = result.getRecords().stream()
                .map(user -> new UserInfoVo(
                        user.getId(),
                        user.getUsername(),
                        user.getUserType(),
                        user.getNickname(),
                        user.getPhone(),
                        user.getGender()
                )).toList();

        // 构建 ListResponse<T> 风格的响应数据
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("prev", beforePage);
        responseMap.put("next", nextPage);
        responseMap.put("total", result.getTotal());
        responseMap.put("list", userInfoVoList);

        return ResponseEntity.ok().body(AjaxResult.success(responseMap));
    }


    /**
     * 更新用户信息
     *
     * <p>更新指定用户的基本信息，用户名不可重复。
     *
     * <p>可能的错误情况：
     * <ul>
     *   <li>信息不全(400)</li>
     *   <li>用户不存在(400)</li>
     *   <li>用户名重复(400)</li>
     *   <li>更新失败(500)</li>
     * </ul>
     *
     * @param id 用户ID
     * @param userInfoDTO 更新信息，包含：
     *                    <ul>
     *                      <li>username - 新用户名</li>
     *                      <li>userType - 用户类型</li>
     *                      <li>nickname - 昵称</li>
     *                      <li>phone - 电话</li>
     *                      <li>gender - 性别</li>
     *                    </ul>
     * @return 操作结果响应实体
     */
    @PutMapping("/user/{id}")
    public ResponseEntity<AjaxResult> updateUser(@PathVariable("id") Long id, @RequestBody UserInfoDTO userInfoDTO) {
        if (userInfoDTO == null || userInfoDTO.getUsername() == null || userInfoDTO.getUsername().isEmpty() || userInfoDTO.getUserType() == null
                || userInfoDTO.getUserType().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400, "信息不全"));
        }
        User user = userService.findById(id);
        if (user == null) {
            //没有该用户
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400, "用户名不存在"));
        } else {
            User user1 = userService.findByUname(userInfoDTO.getUsername());
            if (user1 != null && !Objects.equals(user1.getUsername(), user.getUsername())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400, "用户名重复"));
            }

            Boolean bool = userService.updateUserInfoById(id, userInfoDTO.getUsername(), userInfoDTO.getUserType(),
                    userInfoDTO.getNickname(), userInfoDTO.getPhone(), userInfoDTO.getGender());
            if (bool) {
                return ResponseEntity.ok().body(AjaxResult.success());
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(AjaxResult.error(500, "修改失败"));
            }
        }
    }



    /**
     * 添加用户（管理员）
     *
     * <p>管理员专用接口，添加新用户。
     *
     * <p>可能的错误情况：
     * <ul>
     *   <li>信息不全(400)</li>
     *   <li>用户名已存在(400)</li>
     *   <li>添加失败(500)</li>
     * </ul>
     *
     * @param userAddDto 用户添加信息，包含：
     *                   <ul>
     *                     <li>username - 用户名</li>
     *                     <li>password - 密码</li>
     *                     <li>userType - 用户类型</li>
     *                     <li>nickname - 昵称</li>
     *                     <li>phone - 电话</li>
     *                     <li>gender - 性别</li>
     *                   </ul>
     * @return 操作结果响应实体
     */
    @PostMapping("/user")
    @PreAuthorize("hasRole('管理员')")
    public ResponseEntity<AjaxResult> addUser(@RequestBody UserAddDTO userAddDto) {
        System.out.println(userAddDto);
        if (userAddDto == null || userAddDto.getUsername() == null || userAddDto.getUserType() == null || userAddDto.getPassword() == null
                || userAddDto.getUsername().isEmpty() || userAddDto.getPassword().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400, "信息不全"));
        }
        User user = userService.findByUname(userAddDto.getUsername());
        if (user != null) {
            //用户名重了
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400, "用户名已存在"));
        } else {
            String encrypted = MD5Utils.encrypt(userAddDto.getPassword());
            User newUser = new User(null, "user_" + userAddDto.getUsername(), userAddDto.getUsername(), userAddDto.getUserType(),
                    encrypted, userAddDto.getNickname(), userAddDto.getPhone(), userAddDto.getGender());
            Boolean b = userService.addOneUser(newUser);
            if (b) {
                return ResponseEntity.ok().body(AjaxResult.success());
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(AjaxResult.error(500, "添加失败"));
            }
        }
    }

    @Autowired
    private SendMailUtil sendMailUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // TODO 找回密码
    /**
     * 找回/重置密码
     *
     * <p>通过邮箱验证码找回并重置密码。
     *
     * <p>流程：
     * <ol>
     *   <li>验证输入信息完整性</li>
     *   <li>检查用户是否存在</li>
     *   <li>验证邮箱验证码</li>
     *   <li>更新密码</li>
     * </ol>
     *
     * <p>可能的错误情况：
     * <ul>
     *   <li>信息不全(400)</li>
     *   <li>用户不存在(404)</li>
     *   <li>验证码无效(400)</li>
     *   <li>密码未改变(400)</li>
     *   <li>更新失败(400)</li>
     * </ul>
     *
     * @param verificationRequest 密码找回请求，包含：
     *                           <ul>
     *                             <li>userName - 用户名</li>
     *                             <li>email - 邮箱</li>
     *                             <li>code - 验证码</li>
     *                             <li>newPassword - 新密码</li>
     *                           </ul>
     * @return 操作结果响应实体
     */
    @PostMapping("/user/find-password")
    public ResponseEntity<AjaxResult> findPassword(@RequestBody UserFindPasswordDTO verificationRequest) {
        if (verificationRequest.getEmail() == null || verificationRequest.getEmail().isEmpty() ||
                verificationRequest.getCode() == null || verificationRequest.getCode().isEmpty() ||
                verificationRequest.getNewPassword() == null || verificationRequest.getNewPassword().isEmpty() ||
                verificationRequest.getUserName() == null || verificationRequest.getUserName().isEmpty()) {
            return ResponseEntity.badRequest().body(AjaxResult.error(400, "信息不完全。"));
        }
        User byUname = userService.findByUname(verificationRequest.getUserName());
        if (byUname == null) {
            return ResponseEntity.badRequest().body(AjaxResult.error(404, "该用户不存在"));
        }
        boolean isValid = sendMailUtil.verifyCodeFromRedis(
                verificationRequest.getEmail(),
                verificationRequest.getCode()
        );
        if (isValid) {
            logger.info("邮箱 {} 的验证码验证成功。", verificationRequest.getEmail());
            // 从数据库获取已加密的旧密码
            String storedEncodedPassword = byUname.getPassword();
            // 从请求中获取用户想要设置的新密码（明文）
            String rawNewPassword = verificationRequest.getNewPassword();
            // 在密码重置流程中，验证成功后，前端通常会引导用户进入设置新密码的页面
            if (passwordEncoder.matches(rawNewPassword, storedEncodedPassword)) {
                return ResponseEntity.badRequest().body(AjaxResult.error(400, "密码与之前一样"));
            } else {
                Boolean b = userService.updatePasswordByUsername(verificationRequest.getUserName(),
                        verificationRequest.getNewPassword());
                if (b) {
                    return ResponseEntity.ok(AjaxResult.success("修改成功"));
                } else {
                    return ResponseEntity.badRequest().body(AjaxResult.error(400, "修改失败"));
                }
            }
        } else {
            logger.warn("邮箱 {} 的验证码验证失败。验证码无效或已过期。", verificationRequest.getEmail());
            return ResponseEntity.badRequest().body(AjaxResult.error(400, "验证码无效或已过期。"));
        }
    }

    /**
     * 删除用户
     *
     * @return
     */
    @DeleteMapping("/user/{id}")
    @PreAuthorize("hasRole('管理员')")
    public ResponseEntity<AjaxResult> removeUser(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(AjaxResult.error(404, "没有该用户"));
        } else {
            //有，删除
            Boolean b = userService.removeByUname(user.getUsername());
            if (b) {
                return ResponseEntity.ok().body(AjaxResult.success("删除成功"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(AjaxResult.error(500, "删除失败"));
            }
        }
    }


    /**
     * 获取用户详细信息
     *
     * <p>根据用户ID获取用户完整信息。
     *
     * <p>可能的错误情况：
     * <ul>
     *   <li>用户不存在(404)</li>
     * </ul>
     *
     * @param id 用户ID
     * @return 用户信息响应实体(UserInfoVo)
     */
    @GetMapping("/user/{id}")
    public ResponseEntity<AjaxResult> getUserInfo(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user == null) {
            //没有该用户
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(AjaxResult.error(404, "没有该用户"));
        } else {
            UserInfoVo userInfoVo = new UserInfoVo(user.getId(), user.getUsername(), user.getUserType(),
                    user.getNickname(), user.getPhone(), user.getGender());
            return ResponseEntity.ok().body(AjaxResult.success(userInfoVo));
        }
    }

    /**
     * 获取当前登录用户信息
     *
     * <p>通过JWT令牌获取当前登录用户的详细信息。
     *
     * <p>认证流程：
     * <ol>
     *   <li>从请求头获取Authorization</li>
     *   <li>验证Token有效性</li>
     *   <li>解析用户名</li>
     *   <li>查询用户信息</li>
     * </ol>
     *
     * <p>可能的错误情况：
     * <ul>
     *   <li>未提供Token(401)</li>
     *   <li>Token无效(401)</li>
     *   <li>Token解析失败(400)</li>
     *   <li>用户不存在(404)</li>
     * </ul>
     *
     * @param request HTTP请求对象
     * @return 用户信息响应实体(UserInfoVo)
     */
    @GetMapping("/own")
    public ResponseEntity<AjaxResult> getOwnInfo(HttpServletRequest request) {
        // 1. 从请求头获取 Authorization
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AjaxResult.error(401, "请先登录"));
        }

        String token = authHeader.substring(7); // 去掉 Bearer 前缀

        // 2. 验证 token
        if (!JWTUtil.verify(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AjaxResult.error(401, "Token 无效或已过期"));
        }

        // 3. 获取 token 中的用户名
        String username;
        try {
            username = JWTUtil.getTokenInfo(token).getClaim("username").asString();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AjaxResult.error(400, "Token 解析失败"));
        }

        // 4. 查数据库获取用户信息
        User user = userService.findByUname(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(AjaxResult.error(404, "用户不存在"));
        }

        UserInfoVo userInfoVo = new UserInfoVo(
                user.getId(),
                user.getUsername(),
                user.getUserType(),
                user.getNickname(),
                user.getPhone(),
                user.getGender()
        );

        return ResponseEntity.ok(AjaxResult.success(userInfoVo));
    }

    @Autowired
    JavaMailSenderImpl javaMailSender;
    /**
     * 前端点击是否同意该用户注册为操作员或管理员
     *
     * @param request
     * @return
     */
    @PostMapping("/user/accept-user")
    @PreAuthorize("hasRole('管理员')")
    public ResponseEntity<AjaxResult> acceptUser(@RequestBody UserAccpetDTO request) {
        System.out.println(request);
        User user = userService.findByUname(request.getUserName());
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(Constants.SENDER_EMAIL);
        message.setTo(user.getPhone()); // 确保发送到用户数据库中记录的邮箱
        message.setSubject("[YunCang权限] Permission");
        if (request.isAgree()) {
            boolean b = userService.authUserLogin(request.getUserName());
            if (b) {
                message.setText("【YunCang权限】您的账户已被管理员通过\n" +
                "【YunCang Permission】 Your account has been approved by the administrator");
                try {
                    javaMailSender.send(message);
                    logger.info("已将通过信息发送至用户 {} 的邮箱: {}", request.getUserName(), user.getPhone());
                } catch (MailSendException e) {
                    logger.error("发送通知至 {} 失败: 邮件服务器错误或邮箱地址问题。", user.getPhone(), e);
                }
                return ResponseEntity.ok(AjaxResult.success("已修改"));
            } else {
                return ResponseEntity.badRequest().body(AjaxResult.error("发生错误，请检查用户是否已经被同意"));
            }
        }
        message.setText("【YunCang权限】您的账户已被管理员拒绝通过，如有问题请联系管理员\n" +
                "【YunCang Permission】 Your account has been rejected by the administrator. If you have any questions, please contact the administrator");
        try {
            javaMailSender.send(message);
            logger.info("已将拒绝信息发送至用户 {} 的邮箱: {}", request.getUserName(), user.getPhone());
        } catch (MailSendException e) {
            logger.error("发送通知至 {} 失败: 邮件服务器错误或邮箱地址问题。", user.getPhone(), e);
        }
        Boolean b = userService.deleteByUname(request.getUserName());
        return ResponseEntity.ok(AjaxResult.success("未修改"));
    }

    /**
     * 更新当前登录用户自己的信息
     *
     * <p>用户通过此接口更新自己的个人资料，如用户名、昵称、性别、邮箱等。
     *
     * <p>流程：
     * <ol>
     *   <li>从SecurityContext中获取当前登录用户的ID。</li>
     *   <li>调用UserService处理更新逻辑，包括用户名冲突检查。</li>
     * </ol>
     *
     * <p>可能的错误情况：
     * <ul>
     *   <li>用户未登录 (401)</li>
     *   <li>请求体信息不全 (400)</li>
     *   <li>新用户名已被占用 (400)</li>
     *   <li>更新失败 (500)</li>
     * </ul>
     *
     * @param updateMineDTO 包含要更新字段的数据传输对象
     * @return 更新后的用户信息或错误信息
     */
    @PostMapping("/mine")
    // 任何已认证的用户都可以访问
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AjaxResult> updateMineInfo(@RequestBody UpdateMineDTO updateMineDTO) {
        // 1. 获取当前登录的用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(AjaxResult.error(401, "用户未登录或认证信息无效"));
        }
        String username = userDetails.getUsername();

        // 2. 基本的参数校验
        if (updateMineDTO.getUsername() == null || updateMineDTO.getUsername().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(AjaxResult.error(400, "用户名不能为空"));
        }
        // ...可以添加更多校验，比如email格式等

        try {
            // 3. 调用 Service 层进行更新
            boolean success = userService.updateSelfInfo(username, updateMineDTO);

            if (success) {
                // 4. 更新成功，返回最新的用户信息
                // 注意：如果用户名被修改，JWT Token 理论上也应该重新生成并返回，
                // 因为旧 Token 中的用户名信息已经过时。
                // 这是一个高级话题，暂时可以先不处理，让用户用新用户名重新登录。

                // 返回一个成功消息
                return ResponseEntity.ok(AjaxResult.success("用户信息更新成功！"));
            } else {
                // 通常 Service 层会通过抛出异常来中断，这里作为防御性代码
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(AjaxResult.error(500, "更新失败，请稍后重试。"));
            }
        } catch (RuntimeException e) {
            // 捕获 Service 层抛出的异常（如用户名冲突）
            return ResponseEntity.badRequest().body(AjaxResult.error(400, e.getMessage()));
        }
    }
}
