package com.fruit.controller.admin;

import com.fruit.annotation.RateLimit;
import com.fruit.common.result.Result;
import com.fruit.dto.LoginDTO;
import com.fruit.service.UserService;
import com.fruit.vo.LoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 管理员控制器
 */
@Api(tags = "管理员-登录")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @ApiOperation("管理员登录")
    @RateLimit(count = 5, time = 60)
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        LoginVO vo = userService.adminLogin(dto);
        return Result.success(vo);
    }

    @ApiOperation("退出登录")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String token) {
        userService.logout(token);
        return Result.success();
    }
}
