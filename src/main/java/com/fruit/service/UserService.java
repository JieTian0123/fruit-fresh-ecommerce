package com.fruit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruit.entity.User;
import com.fruit.dto.LoginDTO;
import com.fruit.dto.PasswordDTO;
import com.fruit.dto.RegisterDTO;
import com.fruit.dto.UserUpdateDTO;
import com.fruit.vo.LoginVO;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 消费者注册
     */
    void register(RegisterDTO dto);

    /**
     * 消费者登录
     */
    LoginVO login(LoginDTO dto);

    /**
     * 商家注册
     */
    void merchantRegister(RegisterDTO dto);

    /**
     * 商家登录
     */
    LoginVO merchantLogin(LoginDTO dto);

    /**
     * 管理员登录
     */
    LoginVO adminLogin(LoginDTO dto);

    /**
     * 退出登录
     */
    void logout(String token);

    /**
     * 获取用户信息
     */
    LoginVO getUserInfo(Long userId);

    /**
     * 更新用户信息
     */
    void updateProfile(Long userId, UserUpdateDTO dto);

    /**
     * 修改密码
     */
    void changePassword(Long userId, PasswordDTO dto);
}
