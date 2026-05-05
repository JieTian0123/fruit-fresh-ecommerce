package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruit.common.exception.BusinessException;
import com.fruit.common.result.ResultCode;
import com.fruit.dto.LoginDTO;
import com.fruit.dto.RegisterDTO;
import com.fruit.entity.User;
import com.fruit.enums.StatusEnum;
import com.fruit.enums.UserTypeEnum;
import com.fruit.mapper.UserMapper;
import com.fruit.service.UserService;
import com.fruit.utils.JwtUtils;
import com.fruit.utils.PasswordUtils;
import com.fruit.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final String CAPTCHA_PREFIX = "captcha:";
    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String TOKEN_PREFIX = "user:token:";
    private static final long TOKEN_EXPIRE_DAYS = 7;

    @Override
    public void register(RegisterDTO dto) {
        // 检查用户名是否存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        if (count(wrapper) > 0) {
            throw new BusinessException(ResultCode.USERNAME_EXIST);
        }

        // 检查手机号是否存在
        if (dto.getPhone() != null) {
            LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(User::getPhone, dto.getPhone());
            if (count(phoneWrapper) > 0) {
                throw new BusinessException(ResultCode.PHONE_EXIST);
            }
        }

        // 创建用户
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(PasswordUtils.encrypt(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setPhone(dto.getPhone());
        user.setUserType(UserTypeEnum.CONSUMER.getCode());
        user.setStatus(StatusEnum.ENABLED.getCode());

        save(user);
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        return doLogin(dto, UserTypeEnum.CONSUMER.getCode());
    }

    @Override
    public void merchantRegister(RegisterDTO dto) {
        // 检查用户名是否存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        if (count(wrapper) > 0) {
            throw new BusinessException(ResultCode.USERNAME_EXIST);
        }

        // 创建商家账户（需要审核）
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(PasswordUtils.encrypt(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setPhone(dto.getPhone());
        user.setUserType(UserTypeEnum.MERCHANT.getCode());
        user.setStatus(StatusEnum.PENDING.getCode()); // 待审核

        save(user);
    }

    @Override
    public LoginVO merchantLogin(LoginDTO dto) {
        return doLogin(dto, UserTypeEnum.MERCHANT.getCode());
    }

    @Override
    public LoginVO adminLogin(LoginDTO dto) {
        return doLogin(dto, UserTypeEnum.ADMIN.getCode());
    }

    private LoginVO doLogin(LoginDTO dto, Integer userType) {
        String captchaKey = CAPTCHA_PREFIX + dto.getCaptchaUuid();
        Object captchaValue = redisTemplate.opsForValue().get(captchaKey);
        redisTemplate.delete(captchaKey);
        if (captchaValue == null || !Objects.equals(String.valueOf(captchaValue), dto.getCaptchaCode().trim())) {
            throw new BusinessException(ResultCode.CAPTCHA_ERROR);
        }

        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        wrapper.eq(User::getUserType, userType);
        User user = getOne(wrapper);

        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 验证密码
        if (!PasswordUtils.verify(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }

        // 检查状态
        if (StatusEnum.DISABLED.getCode().equals(user.getStatus())) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
        if (StatusEnum.PENDING.getCode().equals(user.getStatus())) {
            throw new BusinessException(ResultCode.USER_PENDING);
        }

        // 生成Token
        String token = jwtUtils.generateToken(user.getId(), user.getUserType());

        // 存入Redis
        redisTemplate.opsForValue().set(TOKEN_PREFIX + user.getId(), token, TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);

        // 返回登录信息
        LoginVO vo = new LoginVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setUserType(user.getUserType());
        vo.setRole(getUserRole(user.getUserType()));
        vo.setToken(token);

        return vo;
    }

    @Override
    public void logout(String token) {
        try {
            Long userId = jwtUtils.getUserId(token);
            redisTemplate.delete(TOKEN_PREFIX + userId);
        } catch (Exception e) {
            // Token 无效，忽略
        }
    }

    @Override
    public LoginVO getUserInfo(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        LoginVO vo = new LoginVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setUserType(user.getUserType());
        vo.setRole(getUserRole(user.getUserType()));
        vo.setPoints(user.getPoints());
        vo.setTotalConsumption(user.getTotalConsumption());

        return vo;
    }

    /**
     * 根据用户类型获取角色字符串
     */
    private String getUserRole(Integer userType) {
        if (userType == null) {
            return "CONSUMER";
        }
        switch (userType) {
            case 1:
                return "MERCHANT";
            case 2:
                return "ADMIN";
            default:
                return "CONSUMER";
        }
    }

    @Override
    public void updateProfile(Long userId, com.fruit.dto.UserUpdateDTO dto) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 更新用户信息（仅更新非空字段）
        if (dto.getNickname() != null && !dto.getNickname().isEmpty()) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getPhone() != null && !dto.getPhone().isEmpty()) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getAvatar() != null && !dto.getAvatar().isEmpty()) {
            user.setAvatar(dto.getAvatar());
        }

        updateById(user);
    }

    @Override
    public void changePassword(Long userId, com.fruit.dto.PasswordDTO dto) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 验证原密码
        if (!PasswordUtils.verify(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.OLD_PASSWORD_ERROR);
        }

        // 更新密码
        user.setPassword(PasswordUtils.encrypt(dto.getNewPassword()));
        updateById(user);

        // 使 token 失效，强制重新登录
        redisTemplate.delete(TOKEN_PREFIX + userId);
    }
}
