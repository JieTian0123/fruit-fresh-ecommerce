package com.fruit.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.common.result.Result;
import com.fruit.entity.VipOrder;
import com.fruit.entity.VipPlan;
import com.fruit.service.VipService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * 管理员VIP管理控制器
 */
@Api(tags = "管理员-VIP管理")
@RestController
@RequestMapping("/admin/vip")
@RequiredArgsConstructor
public class VipManageController {

    private final VipService vipService;

    @ApiOperation("VIP套餐列表")
    @GetMapping("/plans")
    public Result<Page<VipPlan>> listPlans(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {

        Page<VipPlan> page = vipService.listAllPlans(pageNum, pageSize);
        return Result.success(page);
    }

    @ApiOperation("添加VIP套餐")
    @PostMapping("/plan")
    public Result<Void> addPlan(@Valid @RequestBody VipPlan plan) {
        vipService.addPlan(plan);
        return Result.success();
    }

    @ApiOperation("修改VIP套餐")
    @PutMapping("/plan")
    public Result<Void> updatePlan(@Valid @RequestBody VipPlan plan) {
        vipService.updatePlan(plan);
        return Result.success();
    }

    @ApiOperation("删除VIP套餐")
    @DeleteMapping("/plan/{id}")
    public Result<Void> deletePlan(@PathVariable Long id) {
        vipService.deletePlan(id);
        return Result.success();
    }

    @ApiOperation("更新套餐状态")
    @PutMapping("/plan/{id}/status")
    public Result<Void> updatePlanStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        vipService.updatePlanStatus(id, status);
        return Result.success();
    }

    @ApiOperation("VIP用户列表")
    @GetMapping("/users")
    public Result<Page<Map<String, Object>>> listVipUsers(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {

        Page<Map<String, Object>> page = vipService.listVipUsers(pageNum, pageSize);
        return Result.success(page);
    }

    @ApiOperation("VIP订单列表")
    @GetMapping("/orders")
    public Result<Page<VipOrder>> listVipOrders(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {

        Page<VipOrder> page = vipService.listVipOrders(pageNum, pageSize);
        return Result.success(page);
    }
}
