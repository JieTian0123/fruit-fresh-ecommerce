package com.fruit.controller.consumer;

import com.fruit.common.result.Result;
import com.fruit.entity.VipOrder;
import com.fruit.entity.VipPlan;
import com.fruit.service.VipService;
import com.fruit.utils.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 消费者VIP会员控制器
 */
@Api(tags = "消费者-VIP会员")
@RestController
@RequestMapping("/consumer/vip")
@RequiredArgsConstructor
public class VipController {

    private final VipService vipService;

    @ApiOperation("获取VIP状态")
    @GetMapping("/status")
    public Result<Map<String, Object>> getVipStatus() {
        Long userId = UserContext.getUserId();
        Map<String, Object> status = vipService.getVipStatus(userId);
        return Result.success(status);
    }

    @ApiOperation("获取可用VIP套餐列表")
    @GetMapping("/plans")
    public Result<List<VipPlan>> getAvailablePlans() {
        List<VipPlan> plans = vipService.getAvailablePlans();
        return Result.success(plans);
    }

    @ApiOperation("购买VIP")
    @PostMapping("/purchase/{planId}")
    public Result<VipOrder> purchaseVip(@PathVariable Long planId) {
        Long userId = UserContext.getUserId();
        VipOrder order = vipService.purchaseVip(userId, planId);
        return Result.success(order);
    }
}
