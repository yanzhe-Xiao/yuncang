package com.xhz.yuncang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xhz.yuncang.dto.user.UpdateMineDTO;
import com.xhz.yuncang.entity.User;
import com.xhz.yuncang.mapper.UserMapper;
import com.xhz.yuncang.service.UserService;
import com.xhz.yuncang.utils.MD5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private PasswordEncoder passwordEncoder;
//    @Override
//    public User findByUnameAndPwd(String username, String password) {
//        //pwd用MD5加密
//        String encryptedPwd = MD5Utils.encrypt(password);
//        System.out.println("encryptedPwd: "+encryptedPwd);
//        return lambdaQuery()
//                .eq(User::getUsername,username)
//                .eq(User::getPassword,encryptedPwd)
//                .one();
//    }

    /**
     * 根据用户名查找用户，并验证密码。
     * 注意：此方法通常在自定义 UserDetailsService 实现的 loadUserByUsername 方法内部被间接调用，
     * 或者在需要手动验证密码的特定场景下使用。
     * Spring Security 的标准流程会自动处理密码验证。
     *
     * @param username 用户名
     * @param rawPassword 用户输入的原始密码 (未加密)
     * @return 如果用户名存在且密码匹配，则返回 User 对象；否则返回 null 或抛出异常。
     */
    @Override
    public User findByUnameAndPwd(String username, String rawPassword) {
        // 1. 首先根据用户名从数据库中查找用户
        User user = lambdaQuery()
                .eq(User::getUsername, username)
                .one();

        // 2. 如果用户存在，则使用 PasswordEncoder 验证密码
        if (user != null) {
            // user.getPassword() 应该返回数据库中存储的 BCrypt (或其他PasswordEncoder) 哈希过的密码
            String encodedPasswordFromDB = user.getPassword();
            // 使用 passwordEncoder.matches() 来比较原始密码和数据库中存储的哈希密码
            if (passwordEncoder.matches(rawPassword, encodedPasswordFromDB)) {
                System.out.println("Password matches for user: " + username);
                return user; // 密码匹配，返回用户对象
            } else {
                System.out.println("Password does not match for user: " + username);
                return null; // 密码不匹配
            }
        }
        System.out.println("User not found: " + username);
        return null; // 用户不存在
    }


    @Override
    public User findByUnameAndPwdAndType(String username, String password, String userType) {
        //pwd用MD5加密
        String encryptedPwd = MD5Utils.encrypt(password);
        System.out.println("encryptedPwd: "+encryptedPwd);
        return lambdaQuery()
                .eq(User::getUsername,username)
                .eq(User::getPassword,encryptedPwd)
                .eq(User::getUserType,userType)
                .one();
    }

    public User findByUname(String username){
        return lambdaQuery()
                .eq(User::getUsername,username)
                .one();
    }

    public User findById(Long id){
        return lambdaQuery()
                .eq(User::getId,id)
                .one();
    }

    public User findByUid(String userId){
        return lambdaQuery()
                .eq(User::getUserId,userId)
                .one();
    }


    public Boolean deleteByUname(String username){
        return lambdaUpdate()
                .eq(User::getUsername,username)
                .remove();
    }

    public List<User> findAll(){
        return list();
    }

    public Boolean deleteAll(){
        return remove(null);
    }

    public Boolean addOneUser(User user){
        return save(user);
    }

    public Boolean updatePasswordByUsername(String username, String newPassword) {
        // 对新密码进行加密（建议使用 MD5）
        String encryptedPwd = passwordEncoder.encode(newPassword);

        return lambdaUpdate()
                .eq(User::getUsername, username)
                .set(User::getPassword, encryptedPwd)
                .update();
    }

    @Override
    public Boolean update(User user) {
        return update(user);
    }

    @Override
    public Boolean updateUserInfoByUname(String username, String userType, String nickname, String phone, String gender) {
        return lambdaUpdate()
                .eq(User::getUsername,username)
                .set(User::getUserType,userType)
                .set(User::getNickname,nickname)
                .set(User::getPhone,phone)
                .set(User::getGender,gender)
                .update();
    }

    @Override
    public Boolean updateUserInfoById(Long id, String username, String userType, String nickname, String phone, String gender) {
        return lambdaUpdate()
                .eq(User::getId, id)
                .set(User::getUsername,username)
                .set(User::getUserType,userType)
                .set(User::getNickname,nickname)
                .set(User::getPhone,phone)
                .set(User::getGender,gender)
                .update();
    }

    public Boolean removeByUname(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return remove(wrapper);
    }

    /**
     * 使用Spring Security加密方式注册新用户
     * @param username
     * @param userType
     * @param nickname
     * @param password
     * @param phone
     * @param gender
     * @return 如果注册成功返回 true，如果用户名已存在或保存失败则返回 false。
     */
    @Transactional // 建议将注册操作放在一个事务中
    @Override
    public boolean registerUserWithSpringSecurity(String username,String userType,String nickname,String password,
                                                  String phone,String gender) {
        // 1. 检查用户名是否已存在
        // 您原来的Controller中是通过 userService.findByUname() 实现的
        if (this.findByUname(username) != null) {
            // 用户名已存在
            // logger.warn("Attempt to register with existing username: {}", userRegisterDTO.getUsername());
            return false;
        }

        // 2. 使用 PasswordEncoder 加密密码
        String encodedPassword = passwordEncoder.encode(password);

        // 3. 创建 User 实体对象
        User newUser = new User();
        // 根据您的 User 实体类结构进行设置。
        // 以下是基于您之前Controller中 User 构造函数的推断，并改用setter（更常见）：
        // User(Long id, String someGeneratedName, String username, String userType, String password, String salt, String phone, String someOtherField)

        // newUser.setId(null); // ID通常由数据库自动生成
        newUser.setUsername(username);
        newUser.setUserType(userType);
        newUser.setPassword(encodedPassword); // 存储加密后的密码
        newUser.setPhone(phone);
        newUser.setGender(gender);
        newUser.setNickname(nickname);
        if(userType.equals("客户")){
            newUser.setUserId("user_"+username);
        }else{
            newUser.setUserId("#_user_"+username); //表示未认证
        }

        // 4. 将新用户保存到数据库
        try {
            //  UserServiceImpl 继承了 MyBatis-Plus 的 ServiceImpl<UserMapper, User>
            boolean saveResult = addOneUser(newUser);
            if (!saveResult) {
                // logger.error("Failed to save user (save method returned false): {}", userRegisterDTO.getUsername());
            }
            return saveResult;


        } catch (Exception e) {
            // 捕获可能的数据库异常或其他运行时异常
//             logger.error("Exception during user registration for username: {}", userRegisterDTO.getUsername(), e);
            System.err.println(e);
            return false; // 注册失败
        }
    }

    @Override
    public boolean authUserLogin(String username) {
        User user = findByUname(username);

        if (user == null) {
            return false;
        }
        String originalUserId = user.getUserId();
        // 校验 userId 是否有效，以及是否以 "#_" 开头
        if (originalUserId == null || !originalUserId.startsWith("#_")) {
            return false; // 无需更新
        }

        // 3. 修改 (Modify): 在内存中修改对象
        String cleanedUserId = originalUserId.substring(2); // 去除前两个字符 "#_"
        // 或者使用更现代的 stripPrefix 方法
        // String cleanedUserId = originalUserId.stripPrefix("#_");

        user.setUserId(cleanedUserId); // 更新 user 对象的 userId 字段

        // 4. 保存 (Save): 将修改后的对象持久化到数据库
        // this.updateById(user) 会根据 user 对象的主键 (id) 来生成 UPDATE 语句。
        // UPDATE user SET user_id = ? ... WHERE id = ?
        return this.updateById(user);
    }

    @Override
    @Transactional // 确保数据库操作的原子性
    public boolean updateSelfInfo(String username, UpdateMineDTO updateMineDTO) {
        // 1. 获取要更新的用户实体
        User currentUser = this.findByUname(username);
        if (currentUser == null) {
            // 理论上，能走到这一步的用户ID肯定是存在的，但做个防御性检查
            return false;
        }

        // 2. 检查新用户名是否被其他用户占用
        String newUsername = updateMineDTO.getUsername();
        if (StringUtils.hasText(newUsername) && !newUsername.equals(currentUser.getUsername())) {
            // 用户名发生了改变，需要检查是否冲突
            User existingUser = this.getOne(
                    new QueryWrapper<User>().eq("username", newUsername)
            );
            if (existingUser != null) {
                // 新用户名已被占用
                // 可以在这里抛出自定义异常，让 Controller 层捕获并返回更具体的错误信息
                throw new RuntimeException("用户名 '" + newUsername + "' 已被占用！");
            }
            currentUser.setUsername(newUsername);
            currentUser.setUserId("user_" + newUsername); // 如果 userId 也是基于 username 生成的，需要同步更新
        }

        // 3. 更新其他字段
        // 使用 Objects.nonNull 来判断前端是否真的传了这个字段，如果没传则不更新
        if (Objects.nonNull(updateMineDTO.getNickname())) {
            currentUser.setNickname(updateMineDTO.getNickname());
        }
        if (Objects.nonNull(updateMineDTO.getGender())) {
            currentUser.setGender(updateMineDTO.getGender());
        }
        // 注意：DTO 中的 email 对应数据库中的 phone
        if (Objects.nonNull(updateMineDTO.getEmail())) {
            currentUser.setPhone(updateMineDTO.getEmail());
        }

        // 4. 执行更新
        return this.updateById(currentUser);
    }
}
