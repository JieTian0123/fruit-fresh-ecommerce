package com.fruit.controller.consumer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.common.result.Result;
import com.fruit.entity.Coupon;
import com.fruit.entity.UserCoupon;
import com.fruit.service.CouponService;
import com.fruit.utils.UserContext;
import com.fruit.vo.UserCouponVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 消费者优惠券控制器
 */
@Api(tags = "消费者-优惠券")
@RestController
@RequestMapping("/coupon")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @ApiOperation("获取可领取的优惠券列表")
    @GetMapping("/available")
    public Result<Map<String, Object>> getAvailableCoupons(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {

        Long userId = UserContext.getUserId();
        Page<Coupon> page = couponService.getAvailableCoupons(pageNum, pageSize);

        // 获取用户已领取的优惠券ID列表
        List<Long> receivedCouponIds = couponService.getUserReceivedCouponIds(userId);
        Map<Long, String> acquireTypeMap = couponService.getUserCouponAcquireTypeMap(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("records", page.getRecords());
        result.put("total", page.getTotal());
        result.put("receivedCouponIds", receivedCouponIds);
        result.put("acquireTypeMap", acquireTypeMap);

        return Result.success(result);
    }

    @ApiOperation("领取优惠券")
    @PostMapping("/receive/{couponId}")
    public Result<Void> receiveCoupon(@PathVariable Long couponId) {
        Long userId = UserContext.getUserId();
        couponService.receiveCoupon(userId, couponId);
        return Result.success();
    }

    @ApiOperation("我的优惠券列表")
    @GetMapping("/my")
    public Result<Page<?>> getMyCoupons(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("状态 0-未使用 1-已使用 2-已过期") @RequestParam(required = false) Integer status) {

        Long userId = UserContext.getUserId();
        Page<?> page = couponService.getUserCoupons(userId, pageNum, pageSize, status);
        return Result.success(page);
    }

    @ApiOperation("获取可用优惠券（结算页）")
    @GetMapping("/usable")
    public Result<List<UserCouponVO>> getUsableCoupons(
            @ApiParam("订单金额") @RequestParam BigDecimal amount) {

        Long userId = UserContext.getUserId();
        List<UserCouponVO> coupons = couponService.getUsableCoupons(userId, amount);
        return Result.success(coupons);
    }

    @ApiOperation("获取可积分兑换的优惠券列表")
    @GetMapping("/exchangeable")
    public Result<Page<Coupon>> getExchangeableCoupons(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {

        Page<Coupon> page = couponService.getExchangeableCoupons(pageNum, pageSize);
        return Result.success(page);
    }

    @ApiOperation("获取当前用户优惠券获得方式")
    @GetMapping("/acquire-types")
    public Result<Map<Long, String>> getCouponAcquireTypes() {
        Long userId = UserContext.getUserId();
        Map<Long, String> acquireTypeMap = couponService.getUserCouponAcquireTypeMap(userId);
        return Result.success(acquireTypeMap);
    }

    @ApiOperation("积分兑换优惠券")
    @PostMapping("/exchange/{couponId}")
    public Result<Void> exchangeCoupon(@PathVariable Long couponId) {
        Long userId = UserContext.getUserId();
        couponService.exchangeCoupon(userId, couponId);
        return Result.success();
    }
}
