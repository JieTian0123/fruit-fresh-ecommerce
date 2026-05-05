package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruit.common.exception.BusinessException;
import com.fruit.common.result.ResultCode;
import com.fruit.entity.Coupon;
import com.fruit.entity.UserCoupon;
import com.fruit.entity.UserPointsLog;
import com.fruit.mapper.CouponMapper;
import com.fruit.mapper.UserCouponMapper;
import com.fruit.mapper.UserPointsLogMapper;
import com.fruit.service.CouponService;
import com.fruit.service.PointService;
import com.fruit.service.VipService;
import com.fruit.vo.UserCouponVO;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 优惠券服务实现
 */
@Service
@RequiredArgsConstructor
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements CouponService {

    private final UserCouponMapper userCouponMapper;
    private final UserPointsLogMapper userPointsLogMapper;

    @Lazy
    private final PointService pointService;

    @Lazy
    private final VipService vipService;

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
        normalizeCoupon(coupon);

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

        normalizeCoupon(coupon);

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
                .apply("((valid_days IS NOT NULL AND (valid_from IS NULL OR valid_from <= {0}) AND (valid_until IS NULL OR valid_until >= {0})) " +
                        "OR (valid_days IS NULL AND valid_from <= {0} AND valid_until >= {0}))", now)
                .apply("COALESCE(received_quantity, 0) < total_quantity")
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
        if (!isClaimWindowActive(coupon, now)) {
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

        // 检查用户领取次数
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getCouponId, couponId);
        long userReceivedCount = userCouponMapper.selectCount(wrapper);

        // 检查是否是VIP用户
        boolean isVipUser = vipService.isVip(userId);
        // 检查该优惠券是否支持VIP免费领取
        boolean isVipFreeReceive = coupon.getVipFreeReceive() != null && coupon.getVipFreeReceive() == 1;

        boolean requiresPointsExchange = coupon.getPointsPrice() != null && coupon.getPointsPrice() > 0;

        if (isVipFreeReceive) {
            if (isVipUser) {
                // VIP用户免费领取，每种优惠券只能免费领取一次
                if (userReceivedCount >= 1) {
                    throw new BusinessException("VIP会员已免费领取过该优惠券");
                }
            } else {
                // 非VIP用户不能免费领取VIP优惠券，需走积分兑换
                throw new BusinessException("该优惠券仅VIP会员可免费领取，非VIP用户请使用积分兑换");
            }
        } else {
            if (requiresPointsExchange) {
                throw new BusinessException("该优惠券需使用积分兑换");
            }

            // 非VIP免费券，普通领取，检查每人限领数量
            if (coupon.getPerUserLimit() != null && userReceivedCount >= coupon.getPerUserLimit()) {
                throw new BusinessException("您已达到该优惠券的领取上限");
            }
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
    public Page<UserCouponVO> getUserCoupons(Long userId, Integer pageNum, Integer pageSize, Integer status) {
        LocalDateTime now = LocalDateTime.now();

        // 先查询所有该用户的优惠券记录（无分页），用于动态计算过期状态
        LambdaQueryWrapper<UserCoupon> allWrapper = new LambdaQueryWrapper<>();
        allWrapper.eq(UserCoupon::getUserId, userId);
        List<UserCoupon> allUserCoupons = userCouponMapper.selectList(allWrapper);

        // 动态更新过期状态（status=0 且 validUntil < now 的记录应标记为已过期）
        for (UserCoupon uc : allUserCoupons) {
            if (uc.getStatus() == 0 && uc.getValidUntil() != null && uc.getValidUntil().isBefore(now)) {
                uc.setStatus(2); // 标记为已过期
                userCouponMapper.updateById(uc);
            }
        }

        // 按状态筛选
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getUserId, userId);
        if (status != null) {
            wrapper.eq(UserCoupon::getStatus, status);
        }
        wrapper.orderByDesc(UserCoupon::getReceiveTime);

        // 分页查询
        Page<UserCoupon> ucPage = new Page<>(pageNum, pageSize);
        ucPage = userCouponMapper.selectPage(ucPage, wrapper);

        List<UserCoupon> userCoupons = ucPage.getRecords();

        if (userCoupons.isEmpty()) {
            Page<UserCouponVO> result = new Page<>(pageNum, pageSize);
            result.setTotal(ucPage.getTotal());
            result.setRecords(new ArrayList<>());
            return result;
        }

        // 批量查询关联的优惠券信息
        Set<Long> couponIds = userCoupons.stream()
                .map(UserCoupon::getCouponId)
                .collect(Collectors.toSet());
        List<Coupon> coupons = listByIds(couponIds);
        Map<Long, Coupon> couponMap = coupons.stream()
                .collect(Collectors.toMap(Coupon::getId, c -> c));

        // 组装 VO
        List<UserCouponVO> voList = userCoupons.stream().map(uc -> {
            UserCouponVO vo = new UserCouponVO();
            vo.setId(uc.getId());
            vo.setUserId(uc.getUserId());
            vo.setCouponId(uc.getCouponId());
            vo.setStatus(uc.getStatus());
            vo.setReceiveTime(uc.getReceiveTime());
            vo.setUseTime(uc.getUseTime());
            vo.setOrderNo(uc.getOrderNo());
            vo.setValidFrom(uc.getValidFrom());
            vo.setValidUntil(uc.getValidUntil());

            Coupon coupon = couponMap.get(uc.getCouponId());
            if (coupon != null) {
                vo.setTitle(coupon.getTitle());
                vo.setDescription(coupon.getDescription());
                vo.setCouponType(coupon.getCouponType());
                vo.setDiscountAmount(coupon.getDiscountAmount());
                vo.setDiscountRate(coupon.getDiscountRate());
                vo.setMinimumAmount(coupon.getMinimumAmount());
                vo.setMaximumDiscount(coupon.getMaximumDiscount());
            }

            return vo;
        }).collect(Collectors.toList());

        // 返回分页结果
        Page<UserCouponVO> result = new Page<>(pageNum, pageSize);
        result.setTotal(ucPage.getTotal());
        result.setRecords(voList);
        return result;
    }

    @Override
    public List<UserCouponVO> getUsableCoupons(Long userId, BigDecimal orderAmount) {
        LocalDateTime now = LocalDateTime.now();

        // 查询用户未使用且在有效期内的优惠券
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getStatus, 0)
                .le(UserCoupon::getValidFrom, now)
                .ge(UserCoupon::getValidUntil, now);
        List<UserCoupon> userCoupons = userCouponMapper.selectList(wrapper);

        if (userCoupons.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量查询关联的优惠券信息
        Set<Long> couponIds = userCoupons.stream()
                .map(UserCoupon::getCouponId)
                .collect(Collectors.toSet());
        LambdaQueryWrapper<Coupon> couponWrapper = new LambdaQueryWrapper<>();
        couponWrapper.in(Coupon::getId, couponIds)
                .eq(Coupon::getStatus, 1);
        List<Coupon> coupons = list(couponWrapper);
        Map<Long, Coupon> couponMap = coupons.stream()
                .collect(Collectors.toMap(Coupon::getId, c -> c));

        // 组装 VO 并判断可用性
        return userCoupons.stream()
                .map(uc -> {
                    Coupon coupon = couponMap.get(uc.getCouponId());
                    if (coupon == null) {
                        return null;
                    }

                    // 检查最低消费金额
                    boolean isUsable = coupon.getMinimumAmount() == null ||
                                       orderAmount.compareTo(coupon.getMinimumAmount()) >= 0;
                    if (!isUsable) {
                        return null;
                    }

                    UserCouponVO vo = new UserCouponVO();
                    vo.setId(uc.getId());
                    vo.setUserId(uc.getUserId());
                    vo.setCouponId(uc.getCouponId());
                    vo.setStatus(uc.getStatus());
                    vo.setReceiveTime(uc.getReceiveTime());
                    vo.setUseTime(uc.getUseTime());
                    vo.setOrderNo(uc.getOrderNo());
                    vo.setValidFrom(uc.getValidFrom());
                    vo.setValidUntil(uc.getValidUntil());

                    // 填充优惠券详情
                    vo.setTitle(coupon.getTitle());
                    vo.setDescription(coupon.getDescription());
                    vo.setCouponType(coupon.getCouponType());
                    vo.setDiscountAmount(coupon.getDiscountAmount());
                    vo.setDiscountRate(coupon.getDiscountRate());
                    vo.setMinimumAmount(coupon.getMinimumAmount());
                    vo.setMaximumDiscount(coupon.getMaximumDiscount());

                    return vo;
                })
                .filter(vo -> vo != null)
                .collect(Collectors.toList());
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
                .apply("((valid_days IS NOT NULL AND (valid_from IS NULL OR valid_from <= {0}) AND (valid_until IS NULL OR valid_until >= {0})) " +
                        "OR (valid_days IS NULL AND valid_from <= {0} AND valid_until >= {0}))", now)
                .apply("COALESCE(received_quantity, 0) < total_quantity")
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
        if (!isClaimWindowActive(coupon, now)) {
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
        // sourceType=5 表示兑换
        pointService.deductPoints(userId, coupon.getPointsPrice(), 5,
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

    @Override
    public List<Long> getUserReceivedCouponIds(Long userId) {
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getUserId, userId)
               .select(UserCoupon::getCouponId);
        List<UserCoupon> userCoupons = userCouponMapper.selectList(wrapper);
        return userCoupons.stream()
                .map(UserCoupon::getCouponId)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public Map<Long, String> getUserCouponAcquireTypeMap(Long userId) {
        LambdaQueryWrapper<UserCoupon> couponWrapper = new LambdaQueryWrapper<>();
        couponWrapper.eq(UserCoupon::getUserId, userId)
                .select(UserCoupon::getCouponId);
        List<UserCoupon> userCoupons = userCouponMapper.selectList(couponWrapper);

        if (userCoupons.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, String> acquireTypeMap = userCoupons.stream()
                .map(UserCoupon::getCouponId)
                .distinct()
                .collect(Collectors.toMap(id -> id, id -> "receive"));

        Set<String> couponIdStrings = acquireTypeMap.keySet().stream()
                .map(String::valueOf)
                .collect(Collectors.toSet());

        LambdaQueryWrapper<UserPointsLog> logWrapper = new LambdaQueryWrapper<>();
        logWrapper.eq(UserPointsLog::getUserId, userId)
                .eq(UserPointsLog::getSourceType, 5)
                .in(UserPointsLog::getSourceId, couponIdStrings)
                .select(UserPointsLog::getSourceId);
        List<UserPointsLog> exchangeLogs = userPointsLogMapper.selectList(logWrapper);

        for (UserPointsLog log : exchangeLogs) {
            try {
                Long couponId = Long.valueOf(log.getSourceId());
                if (acquireTypeMap.containsKey(couponId)) {
                    acquireTypeMap.put(couponId, "exchange");
                }
            } catch (NumberFormatException ignored) {
                // 旧数据可能不是优惠券ID，直接忽略即可。
            }
        }

        return acquireTypeMap;
    }

    private void normalizeCoupon(Coupon coupon) {
        if (coupon == null) {
            return;
        }

        if (coupon.getValidDays() != null && coupon.getValidDays() > 0) {
            LocalDateTime now = LocalDateTime.now();
            if (coupon.getValidFrom() == null) {
                coupon.setValidFrom(now);
            }
            if (coupon.getValidUntil() == null || !coupon.getValidUntil().isAfter(coupon.getValidFrom())) {
                coupon.setValidUntil(coupon.getValidFrom().plusYears(1));
            }
        } else {
            coupon.setValidDays(null);
        }
    }

    private boolean isClaimWindowActive(Coupon coupon, LocalDateTime now) {
        if (coupon.getValidFrom() != null && now.isBefore(coupon.getValidFrom())) {
            return false;
        }
        if (coupon.getValidUntil() != null && now.isAfter(coupon.getValidUntil())) {
            return false;
        }
        return coupon.getValidDays() != null || (coupon.getValidFrom() != null && coupon.getValidUntil() != null);
    }
}
