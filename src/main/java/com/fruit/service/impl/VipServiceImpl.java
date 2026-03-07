package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.common.exception.BusinessException;
import com.fruit.common.result.ResultCode;
import com.fruit.entity.*;
import com.fruit.mapper.*;
import com.fruit.service.VipService;
import com.fruit.utils.OrderNoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * VIP会员服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VipServiceImpl implements VipService {

    private final UserMapper userMapper;
    private final VipPlanMapper vipPlanMapper;
    private final VipOrderMapper vipOrderMapper;
    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;

    /**
     * 订单达标自动升级VIP的阈值
     */
    private static final int VIP_ORDER_THRESHOLD = 28;

    /**
     * 订单达标后赠送的VIP天数
     */
    private static final int VIP_ORDER_UPGRADE_DAYS = 365;

    @Override
    public Map<String, Object> getVipStatus(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        Map<String, Object> result = new HashMap<>();
        boolean isVipActive = user.getIsVip() != null && user.getIsVip() == 1
                && user.getVipExpireTime() != null && user.getVipExpireTime().isAfter(LocalDateTime.now());

        result.put("isVip", isVipActive);
        result.put("expireTime", user.getVipExpireTime());

        // 已完成订单数（实时查询）
        int completedOrders = vipOrderMapper.countCompletedOrders(userId);
        result.put("completedOrders", completedOrders);
        result.put("upgradeThreshold", VIP_ORDER_THRESHOLD);

        return result;
    }

    @Override
    public boolean isVip(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        return user.getIsVip() != null && user.getIsVip() == 1
                && user.getVipExpireTime() != null && user.getVipExpireTime().isAfter(LocalDateTime.now());
    }

    @Override
    public List<VipPlan> getAvailablePlans() {
        LambdaQueryWrapper<VipPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VipPlan::getStatus, 1);
        wrapper.orderByAsc(VipPlan::getSort);
        return vipPlanMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VipOrder purchaseVip(Long userId, Long planId) {
        VipPlan plan = vipPlanMapper.selectById(planId);
        if (plan == null || plan.getStatus() != 1) {
            throw new BusinessException("VIP套餐不存在或已下架");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 计算VIP开始和结束时间
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now;
        LocalDateTime endTime;

        // 如果用户当前是VIP且未过期，则续期
        if (user.getIsVip() != null && user.getIsVip() == 1
                && user.getVipExpireTime() != null && user.getVipExpireTime().isAfter(now)) {
            endTime = user.getVipExpireTime().plusDays(plan.getDurationDays());
        } else {
            endTime = now.plusDays(plan.getDurationDays());
        }

        // 创建VIP订单（模拟支付，直接成功）
        VipOrder vipOrder = new VipOrder();
        vipOrder.setUserId(userId);
        vipOrder.setPlanId(planId);
        vipOrder.setOrderNo("VIP" + OrderNoUtils.generateOrderNo());
        vipOrder.setAmount(plan.getPrice());
        vipOrder.setSource(1); // 购买
        vipOrder.setStatus(1); // 已付款
        vipOrder.setPayTime(now);
        vipOrder.setVipStartTime(startTime);
        vipOrder.setVipEndTime(endTime);
        vipOrderMapper.insert(vipOrder);

        // 激活用户VIP
        user.setIsVip(1);
        user.setVipExpireTime(endTime);
        userMapper.updateById(user);

        return vipOrder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkAndUpgradeByOrders(Long userId) {
        int completedOrders = vipOrderMapper.countCompletedOrders(userId);

        // 更新缓存的订单数
        User user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }
        user.setVipOrderCount(completedOrders);
        userMapper.updateById(user);

        // 检查是否达到阈值
        if (completedOrders < VIP_ORDER_THRESHOLD) {
            return;
        }

        // 如果已经是VIP且未过期，不重复升级
        if (user.getIsVip() != null && user.getIsVip() == 1
                && user.getVipExpireTime() != null && user.getVipExpireTime().isAfter(LocalDateTime.now())) {
            return;
        }

        // 自动升级VIP
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.plusDays(VIP_ORDER_UPGRADE_DAYS);

        VipOrder vipOrder = new VipOrder();
        vipOrder.setUserId(userId);
        vipOrder.setOrderNo("VIPAUTO" + OrderNoUtils.generateOrderNo());
        vipOrder.setAmount(BigDecimal.ZERO);
        vipOrder.setSource(2); // 订单达标自动升级
        vipOrder.setStatus(1);
        vipOrder.setPayTime(now);
        vipOrder.setVipStartTime(now);
        vipOrder.setVipEndTime(endTime);
        vipOrderMapper.insert(vipOrder);

        user.setIsVip(1);
        user.setVipExpireTime(endTime);
        userMapper.updateById(user);

        log.info("用户 {} 完成 {} 笔订单，自动升级VIP，有效期至 {}", userId, completedOrders, endTime);
    }

    @Override
    @Scheduled(cron = "0 0 0 ? * MON") // 每周一凌晨执行
    @Transactional(rollbackFor = Exception.class)
    public void distributeWeeklyVipCoupons() {
        log.info("开始发放VIP每周优惠券...");

        // 查询所有有效VIP用户
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getIsVip, 1);
        userWrapper.gt(User::getVipExpireTime, LocalDateTime.now());
        userWrapper.eq(User::getDeleted, 0);
        List<User> vipUsers = userMapper.selectList(userWrapper);

        if (vipUsers.isEmpty()) {
            log.info("没有有效VIP用户，跳过发放");
            return;
        }

        // 查找标记为VIP专属的优惠券（memberLevels字段包含"VIP"的）
        // 如果没有专门的VIP优惠券，则创建一张默认的
        LambdaQueryWrapper<Coupon> couponWrapper = new LambdaQueryWrapper<>();
        couponWrapper.eq(Coupon::getStatus, 1);
        couponWrapper.like(Coupon::getMemberLevels, "VIP");
        couponWrapper.le(Coupon::getValidFrom, LocalDateTime.now());
        couponWrapper.ge(Coupon::getValidUntil, LocalDateTime.now());
        List<Coupon> vipCoupons = couponMapper.selectList(couponWrapper);

        if (vipCoupons.isEmpty()) {
            log.info("没有可用的VIP专属优惠券，跳过发放");
            return;
        }

        int distributed = 0;
        for (User vipUser : vipUsers) {
            for (Coupon coupon : vipCoupons) {
                // 检查本周是否已发放过
                LocalDateTime weekStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
                LambdaQueryWrapper<UserCoupon> checkWrapper = new LambdaQueryWrapper<>();
                checkWrapper.eq(UserCoupon::getUserId, vipUser.getId());
                checkWrapper.eq(UserCoupon::getCouponId, coupon.getId());
                checkWrapper.ge(UserCoupon::getReceiveTime, weekStart);
                long existCount = userCouponMapper.selectCount(checkWrapper);

                if (existCount > 0) {
                    continue;
                }

                // 发放优惠券
                UserCoupon userCoupon = new UserCoupon();
                userCoupon.setUserId(vipUser.getId());
                userCoupon.setCouponId(coupon.getId());
                userCoupon.setStatus(0); // 未使用
                userCoupon.setReceiveTime(LocalDateTime.now());

                // 设置有效期
                if (coupon.getValidDays() != null) {
                    userCoupon.setValidFrom(LocalDateTime.now());
                    userCoupon.setValidUntil(LocalDateTime.now().plusDays(coupon.getValidDays()));
                } else {
                    userCoupon.setValidFrom(coupon.getValidFrom());
                    userCoupon.setValidUntil(coupon.getValidUntil());
                }

                userCouponMapper.insert(userCoupon);
                distributed++;
            }
        }

        log.info("VIP每周优惠券发放完成，共发放 {} 张", distributed);
    }

    // ============ 管理员接口 ============

    @Override
    public Page<VipPlan> listAllPlans(Integer pageNum, Integer pageSize) {
        Page<VipPlan> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<VipPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(VipPlan::getSort);
        return vipPlanMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPlan(VipPlan plan) {
        plan.setStatus(plan.getStatus() != null ? plan.getStatus() : 1);
        vipPlanMapper.insert(plan);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePlan(VipPlan plan) {
        VipPlan existing = vipPlanMapper.selectById(plan.getId());
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        vipPlanMapper.updateById(plan);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePlan(Long id) {
        VipPlan existing = vipPlanMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        vipPlanMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePlanStatus(Long id, Integer status) {
        VipPlan plan = vipPlanMapper.selectById(id);
        if (plan == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        plan.setStatus(status);
        vipPlanMapper.updateById(plan);
    }

    @Override
    public Page<Map<String, Object>> listVipUsers(Integer pageNum, Integer pageSize) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getIsVip, 1);
        wrapper.eq(User::getDeleted, 0);
        wrapper.orderByDesc(User::getVipExpireTime);
        Page<User> userPage = userMapper.selectPage(page, wrapper);

        // 转换为Map格式，隐藏敏感信息
        Page<Map<String, Object>> result = new Page<>(pageNum, pageSize, userPage.getTotal());
        List<Map<String, Object>> records = new ArrayList<>();
        for (User user : userPage.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("username", user.getUsername());
            map.put("nickname", user.getNickname());
            map.put("phone", user.getPhone());
            map.put("avatar", user.getAvatar());
            map.put("isVip", user.getIsVip());
            map.put("vipExpireTime", user.getVipExpireTime());
            map.put("vipOrderCount", user.getVipOrderCount());
            map.put("points", user.getPoints());
            records.add(map);
        }
        result.setRecords(records);
        return result;
    }

    @Override
    public Page<VipOrder> listVipOrders(Integer pageNum, Integer pageSize) {
        Page<VipOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<VipOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(VipOrder::getCreateTime);
        return vipOrderMapper.selectPage(page, wrapper);
    }
}
