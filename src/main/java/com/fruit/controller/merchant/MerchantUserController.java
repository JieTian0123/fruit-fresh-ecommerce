package com.fruit.controller.merchant;

import com.fruit.common.result.Result;
import com.fruit.dto.LoginDTO;
import com.fruit.dto.RegisterDTO;
import com.fruit.service.UserService;
import com.fruit.vo.LoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 商家用户控制器
 */
@Api(tags = "商家-用户")
@RestController
@RequestMapping("/merchant")
@RequiredArgsConstructor
public class MerchantUserController {

    private final UserService userService;

    @ApiOperation("商家注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        userService.merchantRegister(dto);
        return Result.success();
    }

    @ApiOperation("商家登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        LoginVO vo = userService.merchantLogin(dto);
        return Result.success(vo);
    }

    @ApiOperation("退出登录")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String token) {
        userService.logout(token);
        return Result.success();
    }
}
