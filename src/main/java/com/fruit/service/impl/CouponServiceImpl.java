package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruit.common.exception.BusinessException;
import com.fruit.common.result.ResultCode;
import com.fruit.entity.Coupon;
import com.fruit.entity.UserCoupon;
import com.fruit.mapper.CouponMapper;
import com.fruit.mapper.UserCouponMapper;
import com.fruit.service.CouponService;
import com.fruit.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 优惠券服务实现
 */
@Service
@RequiredArgsConstructor
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements CouponService {

    private final UserCouponMapper userCouponMapper;

    @Lazy
    private final PointService pointService;

    @Override
    public Page<Coupon> listCoupons(Integer pageNum, Integer pageSize, Integer status, String keyword) {
        Page<Coupon> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null) {
            wrapper.eq(Coupon::getStatus, status);
        }
        
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Coupon::getTitle, keyword);
        }
        
        wrapper.orderByDesc(Coupon::getCreateTime);
        
        return page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCoupon(Coupon coupon) {
        // 设置初始数量
        coupon.setReceivedQuantity(0);
        coupon.setUsedQuantity(0);
        
        // 设置默认状态为启用
        if (coupon.getStatus() == null) {
            coupon.setStatus(1);
        }
        
        save(coupon);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCoupon(Coupon coupon) {
        Coupon existingCoupon = getById(coupon.getId());
        if (existingCoupon == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        
        // 不允许修改已领取和已使用数量
        coupon.setReceivedQuantity(null);
        coupon.setUsedQuantity(null);
        
        updateById(coupon);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCoupon(Long id) {
        Coupon coupon = getById(id);
        if (coupon == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        
        // 检查是否有用户已领取
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getCouponId, id);
        long count = userCouponMapper.selectCount(wrapper);
        
        if (count > 0) {
            throw new BusinessException("该优惠券已有用户领取，无法删除");
        }
        
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        Coupon coupon = getById(id);
        if (coupon == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        
        coupon.setStatus(status);
        updateById(coupon);
    }

    @Override
    public Page<Coupon> getAvailableCoupons(Integer pageNum, Integer pageSize) {
        Page<Coupon> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        
        LocalDateTime now = LocalDateTime.now();
        
        // 只查询启用且在有效期内的优惠券
        wrapper.eq(Coupon::getStatus, 1)
                .le(Coupon::getValidFrom, now)
                .ge(Coupon::getValidUntil, now)
                .apply("received_quantity < total_quantity")
                .orderByDesc(Coupon::getCreateTime);
        
        return page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receiveCoupon(Long userId, Long couponId) {
        Coupon coupon = getById(couponId);
        if (coupon == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        
        // 检查优惠券状态
        if (coupon.getStatus() != 1) {
            throw new BusinessException("该优惠券已禁用");
        }
        
        // 检查有效期
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getValidFrom()) || now.isAfter(coupon.getValidUntil())) {
            throw new BusinessException("该优惠券不在有效期内");
        }
        
        // 检查库存（防止 totalQuantity 为 null 导致 NPE）
        if (coupon.getTotalQuantity() == null) {
            throw new BusinessException("该优惠券配置异常，请联系管理员");
        }
        int receivedQty = coupon.getReceivedQuantity() == null ? 0 : coupon.getReceivedQuantity();
        if (receivedQty >= coupon.getTotalQuantity()) {
            throw new BusinessException("该优惠券已被领完");
        }

        // 检查用户领取次数（防止 perUserLimit 为 null 导致 NPE）
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getCouponId, couponId);
        long userReceivedCount = userCouponMapper.selectCount(wrapper);

        if (coupon.getPerUserLimit() != null && userReceivedCount >= coupon.getPerUserLimit()) {
            throw new BusinessException("您已达到该优惠券的领取上限");
        }
        
        // 创建用户优惠券记录
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(couponId);
        userCoupon.setStatus(0); // 未使用
        userCoupon.setReceiveTime(now);
        
        // 计算有效期
        if (coupon.getValidDays() != null) {
            // 使用相对有效期
            userCoupon.setValidFrom(now);
            userCoupon.setValidUntil(now.plusDays(coupon.getValidDays()));
        } else {
            // 使用固定有效期
            userCoupon.setValidFrom(coupon.getValidFrom());
            userCoupon.setValidUntil(coupon.getValidUntil());
        }
        
        userCouponMapper.insert(userCoupon);
        
        // 更新优惠券已领取数量
        coupon.setReceivedQuantity(receivedQty + 1);
        updateById(coupon);
    }

    @Override
    public Page<?> getUserCoupons(Long userId, Integer pageNum, Integer pageSize, Integer status) {
        Page<UserCoupon> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.eq(UserCoupon::getUserId, userId);
        
        if (status != null) {
            wrapper.eq(UserCoupon::getStatus, status);
        }
        
        wrapper.orderByDesc(UserCoupon::getReceiveTime);
        
        return userCouponMapper.selectPage(page, wrapper);
    }

    @Override
    public List<UserCoupon> getUsableCoupons(Long userId, BigDecimal orderAmount) {
        LocalDateTime now = LocalDateTime.now();

        // 查询用户未使用且在有效期内的优惠券
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getStatus, 0)
                .le(UserCoupon::getValidFrom, now)
                .ge(UserCoupon::getValidUntil, now);
        List<UserCoupon> userCoupons = userCouponMapper.selectList(wrapper);

        if (userCoupons.isEmpty()) {
            return userCoupons;
        }

        // 按关联的优惠券规则过滤：最低消费金额
        List<Long> couponIds = userCoupons.stream()
                .map(UserCoupon::getCouponId)
                .collect(java.util.stream.Collectors.toList());
        LambdaQueryWrapper<Coupon> couponWrapper = new LambdaQueryWrapper<>();
        couponWrapper.in(Coupon::getId, couponIds)
                .eq(Coupon::getStatus, 1);
        List<Coupon> coupons = list(couponWrapper);

        java.util.Set<Long> validCouponIds = coupons.stream()
                .filter(c -> c.getMinimumAmount() == null || orderAmount.compareTo(c.getMinimumAmount()) >= 0)
                .map(Coupon::getId)
                .collect(java.util.stream.Collectors.toSet());

        return userCoupons.stream()
                .filter(uc -> validCouponIds.contains(uc.getCouponId()))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Page<Coupon> getExchangeableCoupons(Integer pageNum, Integer pageSize) {
        Page<Coupon> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();

        LocalDateTime now = LocalDateTime.now();

        // 只查询启用、在有效期内、设置了积分兑换价格、且有库存的优惠券
        wrapper.eq(Coupon::getStatus, 1)
                .isNotNull(Coupon::getPointsPrice)
                .gt(Coupon::getPointsPrice, 0)
                .le(Coupon::getValidFrom, now)
                .ge(Coupon::getValidUntil, now)
                .apply("received_quantity < total_quantity")
                .orderByAsc(Coupon::getPointsPrice);

        return page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void exchangeCoupon(Long userId, Long couponId) {
        Coupon coupon = getById(couponId);
        if (coupon == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        // 检查是否为可兑换优惠券
        if (coupon.getPointsPrice() == null || coupon.getPointsPrice() <= 0) {
            throw new BusinessException("该优惠券不支持积分兑换");
        }

        // 检查优惠券状态
        if (coupon.getStatus() != 1) {
            throw new BusinessException("该优惠券已禁用");
        }

        // 检查有效期
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getValidFrom()) || now.isAfter(coupon.getValidUntil())) {
            throw new BusinessException("该优惠券不在有效期内");
        }

        // 检查库存（防止 totalQuantity 为 null 导致 NPE）
        if (coupon.getTotalQuantity() == null) {
            throw new BusinessException("该优惠券配置异常，请联系管理员");
        }
        int receivedQtyEx = coupon.getReceivedQuantity() == null ? 0 : coupon.getReceivedQuantity();
        if (receivedQtyEx >= coupon.getTotalQuantity()) {
            throw new BusinessException("该优惠券已兑完");
        }

        // 检查用户兑换次数（防止 perUserLimit 为 null 导致 NPE）
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getCouponId, couponId);
        long userReceivedCount = userCouponMapper.selectCount(wrapper);

        if (coupon.getPerUserLimit() != null && userReceivedCount >= coupon.getPerUserLimit()) {
            throw new BusinessException("您已达到该优惠券的兑换上限");
        }

        // 扣减积分（如果积分不足会抛异常）
        pointService.deductPoints(userId, coupon.getPointsPrice(), 4,
                String.valueOf(couponId), "积分兑换优惠券：" + coupon.getTitle());

        // 创建用户优惠券记录
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(couponId);
        userCoupon.setStatus(0); // 未使用
        userCoupon.setReceiveTime(now);

        // 计算有效期
        if (coupon.getValidDays() != null) {
            userCoupon.setValidFrom(now);
            userCoupon.setValidUntil(now.plusDays(coupon.getValidDays()));
        } else {
            userCoupon.setValidFrom(coupon.getValidFrom());
            userCoupon.setValidUntil(coupon.getValidUntil());
        }

        userCouponMapper.insert(userCoupon);

        // 更新优惠券已领取数量
        coupon.setReceivedQuantity(receivedQtyEx + 1);
        updateById(coupon);
    }
}
