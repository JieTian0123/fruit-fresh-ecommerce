package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fruit.common.exception.BusinessException;
import com.fruit.common.result.ResultCode;
import com.fruit.dto.LoginDTO;
import com.fruit.dto.PasswordDTO;
import com.fruit.dto.RegisterDTO;
import com.fruit.dto.UserUpdateDTO;
import com.fruit.entity.User;
import com.fruit.enums.StatusEnum;
import com.fruit.enums.UserTypeEnum;
import com.fruit.mapper.UserMapper;
import com.fruit.utils.JwtUtils;
import com.fruit.utils.PasswordUtils;
import com.fruit.vo.LoginVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务测试")
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "baseMapper", userMapper);
    }

    @Test
    @DisplayName("消费者注册 - 成功")
    void register_Success() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("testuser");
        dto.setPassword("Test1234");
        dto.setPhone("13800138000");

        when(userMapper.selectCount(any())).thenReturn(0L);
        when(userMapper.insert(any(User.class))).thenReturn(1);

        assertDoesNotThrow(() -> userService.register(dto));

        verify(userMapper, times(2)).selectCount(any());
        verify(userMapper, times(1)).insert(any(User.class));
    }

    @Test
    @DisplayName("消费者注册 - 用户名已存在")
    void register_UsernameExist() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("testuser");

        when(userMapper.selectCount(any())).thenReturn(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> userService.register(dto));
        assertEquals(ResultCode.USERNAME_EXIST.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("消费者注册 - 手机号已存在")
    void register_PhoneExist() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("testuser");
        dto.setPhone("13800138000");

        // 第一次调用是检查用户名，返回0；第二次调用是检查手机号，返回1
        when(userMapper.selectCount(any()))
                .thenReturn(0L)
                .thenReturn(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> userService.register(dto));
        assertEquals(ResultCode.PHONE_EXIST.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("消费者登录 - 成功")
    void login_Success() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser");
        dto.setPassword("Test1234");
        dto.setCaptchaUuid("uuid123");
        dto.setCaptchaCode("1234");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword(PasswordUtils.encrypt("Test1234"));
        user.setUserType(UserTypeEnum.CONSUMER.getCode());
        user.setStatus(StatusEnum.ENABLED.getCode());

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("captcha:uuid123")).thenReturn("1234");
        when(userMapper.selectOne(any(), anyBoolean())).thenReturn(user);
        when(jwtUtils.generateToken(1L, UserTypeEnum.CONSUMER.getCode())).thenReturn("token123");

        LoginVO vo = userService.login(dto);

        assertNotNull(vo);
        assertEquals(1L, vo.getUserId());
        assertEquals("testuser", vo.getUsername());
        assertEquals("token123", vo.getToken());
        assertEquals("CONSUMER", vo.getRole());

        verify(redisTemplate).delete("captcha:uuid123");
        verify(valueOperations).set(eq("user:token:1"), eq("token123"), eq(7L), eq(TimeUnit.DAYS));
    }

    @Test
    @DisplayName("消费者登录 - 验证码错误")
    void login_CaptchaError() {
        LoginDTO dto = new LoginDTO();
        dto.setCaptchaUuid("uuid123");
        dto.setCaptchaCode("1234");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("captcha:uuid123")).thenReturn("5678");

        BusinessException exception = assertThrows(BusinessException.class, () -> userService.login(dto));
        assertEquals(ResultCode.CAPTCHA_ERROR.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("消费者登录 - 用户不存在")
    void login_UserNotExist() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser");
        dto.setCaptchaUuid("uuid123");
        dto.setCaptchaCode("1234");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("captcha:uuid123")).thenReturn("1234");
        when(userMapper.selectOne(any(), anyBoolean())).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> userService.login(dto));
        assertEquals(ResultCode.USER_NOT_EXIST.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("消费者登录 - 密码错误")
    void login_PasswordError() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser");
        dto.setPassword("WrongPassword");
        dto.setCaptchaUuid("uuid123");
        dto.setCaptchaCode("1234");

        User user = new User();
        user.setUsername("testuser");
        user.setPassword(PasswordUtils.encrypt("CorrectPassword123"));

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("captcha:uuid123")).thenReturn("1234");
        when(userMapper.selectOne(any(), anyBoolean())).thenReturn(user);

        BusinessException exception = assertThrows(BusinessException.class, () -> userService.login(dto));
        assertEquals(ResultCode.USER_PASSWORD_ERROR.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("消费者登录 - 用户被禁用")
    void login_UserDisabled() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser");
        dto.setPassword("Test1234");
        dto.setCaptchaUuid("uuid123");
        dto.setCaptchaCode("1234");

        User user = new User();
        user.setUsername("testuser");
        user.setPassword(PasswordUtils.encrypt("Test1234"));
        user.setStatus(StatusEnum.DISABLED.getCode());

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("captcha:uuid123")).thenReturn("1234");
        when(userMapper.selectOne(any(), anyBoolean())).thenReturn(user);

        BusinessException exception = assertThrows(BusinessException.class, () -> userService.login(dto));
        assertEquals(ResultCode.USER_DISABLED.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("消费者登录 - 用户待审核")
    void login_UserPending() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser");
        dto.setPassword("Test1234");
        dto.setCaptchaUuid("uuid123");
        dto.setCaptchaCode("1234");

        User user = new User();
        user.setUsername("testuser");
        user.setPassword(PasswordUtils.encrypt("Test1234"));
        user.setStatus(StatusEnum.PENDING.getCode());

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("captcha:uuid123")).thenReturn("1234");
        when(userMapper.selectOne(any(), anyBoolean())).thenReturn(user);

        BusinessException exception = assertThrows(BusinessException.class, () -> userService.login(dto));
        assertEquals(ResultCode.USER_PENDING.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("商家注册 - 成功")
    void merchantRegister_Success() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("merchant");
        dto.setPassword("Test1234");

        when(userMapper.selectCount(any())).thenReturn(0L);
        when(userMapper.insert(any(User.class))).thenReturn(1);

        assertDoesNotThrow(() -> userService.merchantRegister(dto));

        verify(userMapper, times(1)).selectCount(any());
        verify(userMapper, times(1)).insert(argThat(user ->
            user.getUserType().equals(UserTypeEnum.MERCHANT.getCode()) &&
            user.getStatus().equals(StatusEnum.PENDING.getCode())
        ));
    }

    @Test
    @DisplayName("商家登录 - 成功")
    void merchantLogin_Success() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("merchant");
        dto.setPassword("Test1234");
        dto.setCaptchaUuid("uuid123");
        dto.setCaptchaCode("1234");

        User user = new User();
        user.setId(2L);
        user.setUsername("merchant");
        user.setPassword(PasswordUtils.encrypt("Test1234"));
        user.setUserType(UserTypeEnum.MERCHANT.getCode());
        user.setStatus(StatusEnum.ENABLED.getCode());

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("captcha:uuid123")).thenReturn("1234");
        when(userMapper.selectOne(any(), anyBoolean())).thenReturn(user);
        when(jwtUtils.generateToken(2L, UserTypeEnum.MERCHANT.getCode())).thenReturn("token123");

        LoginVO vo = userService.merchantLogin(dto);

        assertNotNull(vo);
        assertEquals(2L, vo.getUserId());
        assertEquals("MERCHANT", vo.getRole());
    }

    @Test
    @DisplayName("管理员登录 - 成功")
    void adminLogin_Success() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("Test1234");
        dto.setCaptchaUuid("uuid123");
        dto.setCaptchaCode("1234");

        User user = new User();
        user.setId(3L);
        user.setUsername("admin");
        user.setPassword(PasswordUtils.encrypt("Test1234"));
        user.setUserType(UserTypeEnum.ADMIN.getCode());
        user.setStatus(StatusEnum.ENABLED.getCode());

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("captcha:uuid123")).thenReturn("1234");
        when(userMapper.selectOne(any(), anyBoolean())).thenReturn(user);
        when(jwtUtils.generateToken(3L, UserTypeEnum.ADMIN.getCode())).thenReturn("token123");

        LoginVO vo = userService.adminLogin(dto);

        assertNotNull(vo);
        assertEquals(3L, vo.getUserId());
        assertEquals("ADMIN", vo.getRole());
    }

    @Test
    @DisplayName("退出登录 - 成功")
    void logout_Success() {
        String token = "token123";
        when(jwtUtils.getUserId(token)).thenReturn(1L);

        assertDoesNotThrow(() -> userService.logout(token));

        verify(redisTemplate).delete("user:token:1");
    }

    @Test
    @DisplayName("退出登录 - Token无效")
    void logout_InvalidToken() {
        String token = "invalidToken";
        when(jwtUtils.getUserId(token)).thenThrow(new RuntimeException("Invalid token"));

        assertDoesNotThrow(() -> userService.logout(token));

        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("获取用户信息 - 成功")
    void getUserInfo_Success() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setUserType(UserTypeEnum.CONSUMER.getCode());

        when(userMapper.selectById(1L)).thenReturn(user);

        LoginVO vo = userService.getUserInfo(1L);

        assertNotNull(vo);
        assertEquals(1L, vo.getUserId());
        assertEquals("testuser", vo.getUsername());
        assertEquals("CONSUMER", vo.getRole());
    }

    @Test
    @DisplayName("获取用户信息 - 用户不存在")
    void getUserInfo_UserNotExist() {
        when(userMapper.selectById(1L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> userService.getUserInfo(1L));
        assertEquals(ResultCode.USER_NOT_EXIST.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("更新用户信息 - 成功")
    void updateProfile_Success() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setNickname("newNickname");
        dto.setPhone("13900139000");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        when(userMapper.selectById(1L)).thenReturn(user);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        assertDoesNotThrow(() -> userService.updateProfile(1L, dto));

        verify(userMapper).updateById(argThat(u ->
            "newNickname".equals(u.getNickname()) &&
            "13900139000".equals(u.getPhone())
        ));
    }

    @Test
    @DisplayName("更新用户信息 - 用户不存在")
    void updateProfile_UserNotExist() {
        UserUpdateDTO dto = new UserUpdateDTO();
        when(userMapper.selectById(1L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> userService.updateProfile(1L, dto));
        assertEquals(ResultCode.USER_NOT_EXIST.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("修改密码 - 成功")
    void changePassword_Success() {
        PasswordDTO dto = new PasswordDTO();
        dto.setOldPassword("Old12345");
        dto.setNewPassword("New12345");

        User user = new User();
        user.setId(1L);
        user.setPassword(PasswordUtils.encrypt("Old12345"));

        when(userMapper.selectById(1L)).thenReturn(user);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        assertDoesNotThrow(() -> userService.changePassword(1L, dto));

        verify(userMapper).updateById(argThat(u -> PasswordUtils.verify("New12345", u.getPassword())));
        verify(redisTemplate).delete("user:token:1");
    }

    @Test
    @DisplayName("修改密码 - 原密码错误")
    void changePassword_OldPasswordError() {
        PasswordDTO dto = new PasswordDTO();
        dto.setOldPassword("WrongOld");
        dto.setNewPassword("New12345");

        User user = new User();
        user.setId(1L);
        user.setPassword(PasswordUtils.encrypt("Old12345"));

        when(userMapper.selectById(1L)).thenReturn(user);

        BusinessException exception = assertThrows(BusinessException.class, () -> userService.changePassword(1L, dto));
        assertEquals(ResultCode.OLD_PASSWORD_ERROR.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("修改密码 - 用户不存在")
    void changePassword_UserNotExist() {
        PasswordDTO dto = new PasswordDTO();
        when(userMapper.selectById(1L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> userService.changePassword(1L, dto));
        assertEquals(ResultCode.USER_NOT_EXIST.getCode(), exception.getCode());
    }
}
