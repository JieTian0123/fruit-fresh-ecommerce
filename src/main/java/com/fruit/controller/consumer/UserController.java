package com.fruit.controller.consumer;

import com.fruit.common.result.Result;
import com.fruit.dto.LoginDTO;
import com.fruit.dto.PasswordDTO;
import com.fruit.dto.RegisterDTO;
import com.fruit.dto.UserUpdateDTO;
import com.fruit.service.UserService;
import com.fruit.utils.UserContext;
import com.fruit.vo.LoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 消费者用户控制器
 */
@Api(tags = "消费者-用户")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @ApiOperation("用户注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.success();
    }

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        LoginVO vo = userService.login(dto);
        return Result.success(vo);
    }

    @ApiOperation("退出登录")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String token) {
        userService.logout(token);
        return Result.success();
    }

    @ApiOperation("获取当前用户信息")
    @GetMapping("/info")
    public Result<LoginVO> info() {
        Long userId = UserContext.getUserId();
        LoginVO vo = userService.getUserInfo(userId);
        return Result.success(vo);
    }

    @ApiOperation("更新用户信息")
    @PutMapping("/update")
    public Result<Void> updateProfile(@RequestBody UserUpdateDTO dto) {
        Long userId = UserContext.getUserId();
        userService.updateProfile(userId, dto);
        return Result.success();
    }

    @ApiOperation("修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody PasswordDTO dto) {
        Long userId = UserContext.getUserId();
        userService.changePassword(userId, dto);
        return Result.success();
    }
}
