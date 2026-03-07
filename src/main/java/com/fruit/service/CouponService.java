package com.fruit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fruit.entity.Coupon;
import com.fruit.entity.UserCoupon;

import java.math.BigDecimal;
import java.util.List;
/**
 * 优惠券服务接口
 */
public interface CouponService extends IService<Coupon> {

    /**
     * 分页查询优惠券列表
     */
    Page<Coupon> listCoupons(Integer pageNum, Integer pageSize, Integer status, String keyword);

    /**
     * 添加优惠券
     */
    void addCoupon(Coupon coupon);

    /**
     * 更新优惠券
     */
    void updateCoupon(Coupon coupon);

    /**
     * 删除优惠券
     */
    void deleteCoupon(Long id);

    /**
     * 启用/禁用优惠券
     */
    void updateStatus(Long id, Integer status);

    /**
     * 获取可领取的优惠券列表（消费者端）
     */
    Page<Coupon> getAvailableCoupons(Integer pageNum, Integer pageSize);

    /**
     * 用户领取优惠券
     */
    void receiveCoupon(Long userId, Long couponId);

    /**
     * 获取用户优惠券列表
     */
    Page<?> getUserCoupons(Long userId, Integer pageNum, Integer pageSize, Integer status);

    /**
     * 获取用户可用于指定金额的优惠券列表
     */
    List<UserCoupon> getUsableCoupons(Long userId, BigDecimal orderAmount);

    /**
     * 获取可积分兑换的优惠券列表
     */
    Page<Coupon> getExchangeableCoupons(Integer pageNum, Integer pageSize);

    /**
     * 积分兑换优惠券
     */
    void exchangeCoupon(Long userId, Long couponId);
}
