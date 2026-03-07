package com.fruit.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.common.result.Result;
import com.fruit.entity.Coupon;
import com.fruit.service.CouponService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 管理员优惠券管理控制器
 */
@Api(tags = "管理员-优惠券管理")
@RestController
@RequestMapping("/admin/coupon")
@RequiredArgsConstructor
public class CouponManageController {

    private final CouponService couponService;

    @ApiOperation("优惠券列表")
    @GetMapping("/list")
    public Result<Page<Coupon>> list(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("状态") @RequestParam(required = false) Integer status,
            @ApiParam("关键词") @RequestParam(required = false) String keyword) {

        Page<Coupon> page = couponService.listCoupons(pageNum, pageSize, status, keyword);
        return Result.success(page);
    }

    @ApiOperation("优惠券详情")
    @GetMapping("/detail/{id}")
    public Result<Coupon> detail(@PathVariable Long id) {
        Coupon coupon = couponService.getById(id);
        return Result.success(coupon);
    }

    @ApiOperation("添加优惠券")
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody Coupon coupon) {
        couponService.addCoupon(coupon);
        return Result.success();
    }

    @ApiOperation("修改优惠券")
    @PutMapping("/update")
    public Result<Void> update(@Valid @RequestBody Coupon coupon) {
        couponService.updateCoupon(coupon);
        return Result.success();
    }

    @ApiOperation("删除优惠券")
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        couponService.deleteCoupon(id);
        return Result.success();
    }

    @ApiOperation("启用/禁用优惠券")
    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        couponService.updateStatus(id, status);
        return Result.success();
    }
}
