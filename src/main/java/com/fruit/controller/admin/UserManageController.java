package com.fruit.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.common.result.PageResult;
import com.fruit.common.result.Result;
import com.fruit.entity.User;
import com.fruit.enums.StatusEnum;
import com.fruit.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员用户管理控制器
 */
@Api(tags = "管理员-用户管理")
@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
public class UserManageController {

    private final UserService userService;

    @ApiOperation("用户列表")
    @GetMapping("/list")
    public Result<PageResult<User>> list(
            @ApiParam("用户类型") @RequestParam(required = false) Integer userType,
            @ApiParam("用户状态") @RequestParam(required = false) Integer status,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (userType != null) {
            wrapper.eq(User::getUserType, userType);
        }

        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }

        wrapper.orderByDesc(User::getCreateTime);

        Page<User> page = new Page<>(pageNum, pageSize);
        Page<User> result = userService.page(page, wrapper);

        PageResult<User> pageResult = PageResult.of(
                result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());

        return Result.success(pageResult);
    }

    @ApiOperation("用户详情")
    @GetMapping("/detail/{id}")
    public Result<User> detail(@PathVariable Long id) {
        User user = userService.getById(id);
        return Result.success(user);
    }

    @ApiOperation("审核用户")
    @PutMapping("/approve/{id}")
    public Result<Void> approve(@PathVariable Long id, @RequestParam Integer status) {
        User user = userService.getById(id);
        user.setStatus(status);
        userService.updateById(user);
        return Result.success();
    }

    @ApiOperation("禁用用户")
    @PutMapping("/disable/{id}")
    public Result<Void> disable(@PathVariable Long id) {
        User user = userService.getById(id);
        user.setStatus(StatusEnum.DISABLED.getCode());
        userService.updateById(user);
        return Result.success();
    }

    @ApiOperation("启用用户")
    @PutMapping("/enable/{id}")
    public Result<Void> enable(@PathVariable Long id) {
        User user = userService.getById(id);
        user.setStatus(StatusEnum.ENABLED.getCode());
        userService.updateById(user);
        return Result.success();
    }
}
