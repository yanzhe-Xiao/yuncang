package com.xhz.yuncang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xhz.yuncang.dto.user.UpdateMineDTO;
import com.xhz.yuncang.entity.User;

import java.util.List;

public interface UserService extends IService<User> {

    User findByUnameAndPwd(String username,String password);

    User findByUnameAndPwdAndType(String username,String password,String userType);

    User findByUname(String username);

    Boolean addOneUser(User user);

    Boolean deleteByUname(String username);

    List<User> findAll();

    User findById(Long id);

    Boolean deleteAll();

    Boolean updatePasswordByUsername(String username, String newPassword);

    Boolean update(User user);

    Boolean updateUserInfoByUname(String username,String userType,String nickname,
                                  String phone,String gender);

    Boolean updateUserInfoById(Long id, String username, String userType, String nickname, String phone, String gender);

    Boolean removeByUname(String username);

    User findByUid(String userId);

    boolean registerUserWithSpringSecurity(String username,String userType,String nickname,String password,
                                           String phone,String gender);

    boolean authUserLogin(String username);

    /**
     * 更新当前登录用户自己的信息
     * @param username  当前登录用户的ID
     * @param updateMineDTO  包含新信息的数据对象
     * @return 是否更新成功
     */
    boolean updateSelfInfo(String username, UpdateMineDTO updateMineDTO);

}
