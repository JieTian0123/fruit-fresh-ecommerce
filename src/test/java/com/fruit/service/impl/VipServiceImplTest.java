package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.common.exception.BusinessException;
import com.fruit.common.result.ResultCode;
import com.fruit.entity.*;
import com.fruit.mapper.*;
import com.fruit.utils.OrderNoUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VipServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private VipPlanMapper vipPlanMapper;

    @Mock
    private VipOrderMapper vipOrderMapper;

    @Mock
    private CouponMapper couponMapper;

    @Mock
    private UserCouponMapper userCouponMapper;

    @InjectMocks
    private VipServiceImpl vipService;

    @Test
    @DisplayName("获取VIP状态 - 用户不存在")
    void getVipStatus_UserNotFound() {
        when(userMapper.selectById(1L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> vipService.getVipStatus(1L));
        assertEquals(ResultCode.USER_NOT_EXIST.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("获取VIP状态 - 活跃VIP")
    void getVipStatus_ActiveVip() {
        User user = new User();
        user.setId(1L);
        user.setIsVip(1);
        user.setVipExpireTime(LocalDateTime.now().plusDays(10));

        when(userMapper.selectById(1L)).thenReturn(user);
        when(vipOrderMapper.countCompletedOrders(1L)).thenReturn(5);

        Map<String, Object> result = vipService.getVipStatus(1L);

        assertTrue((Boolean) result.get("isVip"));
        assertEquals(user.getVipExpireTime(), result.get("expireTime"));
        assertEquals(5, result.get("completedOrders"));
        assertEquals(28, result.get("upgradeThreshold"));
    }

    @Test
    @DisplayName("获取VIP状态 - 过期VIP")
    void getVipStatus_ExpiredVip() {
        User user = new User();
        user.setId(1L);
        user.setIsVip(1);
        user.setVipExpireTime(LocalDateTime.now().minusDays(10));

        when(userMapper.selectById(1L)).thenReturn(user);
        when(vipOrderMapper.countCompletedOrders(1L)).thenReturn(5);

        Map<String, Object> result = vipService.getVipStatus(1L);

        assertFalse((Boolean) result.get("isVip"));
    }

    @Test
    @DisplayName("获取VIP状态 - 非VIP")
    void getVipStatus_NonVip() {
        User user = new User();
        user.setId(1L);
        user.setIsVip(0);

        when(userMapper.selectById(1L)).thenReturn(user);
        when(vipOrderMapper.countCompletedOrders(1L)).thenReturn(5);

        Map<String, Object> result = vipService.getVipStatus(1L);

        assertFalse((Boolean) result.get("isVip"));
    }

    @Test
    @DisplayName("判断是否VIP - 活跃VIP")
    void isVip_ActiveVip() {
        User user = new User();
        user.setId(1L);
        user.setIsVip(1);
        user.setVipExpireTime(LocalDateTime.now().plusDays(10));

        when(userMapper.selectById(1L)).thenReturn(user);

        assertTrue(vipService.isVip(1L));
    }

    @Test
    @DisplayName("判断是否VIP - 过期VIP")
    void isVip_ExpiredVip() {
        User user = new User();
        user.setId(1L);
        user.setIsVip(1);
        user.setVipExpireTime(LocalDateTime.now().minusDays(10));

        when(userMapper.selectById(1L)).thenReturn(user);

        assertFalse(vipService.isVip(1L));
    }

    @Test
    @DisplayName("判断是否VIP - 非VIP")
    void isVip_NonVip() {
        User user = new User();
        user.setId(1L);
        user.setIsVip(0);

        when(userMapper.selectById(1L)).thenReturn(user);

        assertFalse(vipService.isVip(1L));
    }

    @Test
    @DisplayName("判断是否VIP - 用户不存在")
    void isVip_UserNotFound() {
        when(userMapper.selectById(1L)).thenReturn(null);

        assertFalse(vipService.isVip(1L));
    }

    @Test
    @DisplayName("获取可用VIP套餐")
    void getAvailablePlans() {
        VipPlan plan1 = new VipPlan();
        plan1.setId(1L);
        VipPlan plan2 = new VipPlan();
        plan2.setId(2L);

        when(vipPlanMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(plan1, plan2));

        List<VipPlan> plans = vipService.getAvailablePlans();

        assertEquals(2, plans.size());
        verify(vipPlanMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("购买VIP - 套餐不存在")
    void purchaseVip_PlanNotFound() {
        when(vipPlanMapper.selectById(1L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> vipService.purchaseVip(1L, 1L));
        assertEquals("VIP套餐不存在或已下架", exception.getMessage());
    }

    @Test
    @DisplayName("购买VIP - 套餐已下架")
    void purchaseVip_PlanDisabled() {
        VipPlan plan = new VipPlan();
        plan.setId(1L);
        plan.setStatus(0);

        when(vipPlanMapper.selectById(1L)).thenReturn(plan);

        BusinessException exception = assertThrows(BusinessException.class, () -> vipService.purchaseVip(1L, 1L));
        assertEquals("VIP套餐不存在或已下架", exception.getMessage());
    }

    @Test
    @DisplayName("购买VIP - 用户不存在")
    void purchaseVip_UserNotFound() {
        VipPlan plan = new VipPlan();
        plan.setId(1L);
        plan.setStatus(1);

        when(vipPlanMapper.selectById(1L)).thenReturn(plan);
        when(userMapper.selectById(1L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> vipService.purchaseVip(1L, 1L));
        assertEquals(ResultCode.USER_NOT_EXIST.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("购买VIP - 成功(新VIP)")
    void purchaseVip_SuccessNewVip() {
        VipPlan plan = new VipPlan();
        plan.setId(1L);
        plan.setStatus(1);
        plan.setDurationDays(30);
        plan.setPrice(new BigDecimal("9.9"));

        User user = new User();
        user.setId(1L);
        user.setIsVip(0);

        when(vipPlanMapper.selectById(1L)).thenReturn(plan);
        when(userMapper.selectById(1L)).thenReturn(user);

        VipOrder order = vipService.purchaseVip(1L, 1L);

        assertNotNull(order);
        assertEquals(1L, order.getUserId());
        assertEquals(1L, order.getPlanId());
        assertTrue(order.getOrderNo().startsWith("VIP"));
        assertEquals(new BigDecimal("9.9"), order.getAmount());
        assertEquals(1, order.getSource());
        assertEquals(1, order.getStatus());

        verify(vipOrderMapper, times(1)).insert(any(VipOrder.class));
        verify(userMapper, times(1)).updateById(any(User.class));
        assertEquals(1, user.getIsVip());
        assertNotNull(user.getVipExpireTime());
    }

    @Test
    @DisplayName("购买VIP - 成功(续期)")
    void purchaseVip_SuccessExtendVip() {
        VipPlan plan = new VipPlan();
        plan.setId(1L);
        plan.setStatus(1);
        plan.setDurationDays(30);
        plan.setPrice(new BigDecimal("9.9"));

        User user = new User();
        user.setId(1L);
        user.setIsVip(1);
        LocalDateTime expireTime = LocalDateTime.now().plusDays(10);
        user.setVipExpireTime(expireTime);

        when(vipPlanMapper.selectById(1L)).thenReturn(plan);
        when(userMapper.selectById(1L)).thenReturn(user);

        VipOrder order = vipService.purchaseVip(1L, 1L);

        assertNotNull(order);
        verify(vipOrderMapper, times(1)).insert(any(VipOrder.class));
        verify(userMapper, times(1)).updateById(any(User.class));
        assertEquals(1, user.getIsVip());
        assertEquals(expireTime.plusDays(30), user.getVipExpireTime());
    }

    @Test
    @DisplayName("检查并升级VIP - 用户不存在")
    void checkAndUpgradeByOrders_UserNotFound() {
        when(vipOrderMapper.countCompletedOrders(1L)).thenReturn(30);
        when(userMapper.selectById(1L)).thenReturn(null);

        vipService.checkAndUpgradeByOrders(1L);

        verify(userMapper, never()).updateById(any(User.class));
        verify(vipOrderMapper, never()).insert(any(VipOrder.class));
    }

    @Test
    @DisplayName("检查并升级VIP - 未达阈值")
    void checkAndUpgradeByOrders_BelowThreshold() {
        User user = new User();
        user.setId(1L);

        when(vipOrderMapper.countCompletedOrders(1L)).thenReturn(20);
        when(userMapper.selectById(1L)).thenReturn(user);

        vipService.checkAndUpgradeByOrders(1L);

        verify(userMapper, times(1)).updateById(user);
        assertEquals(20, user.getVipOrderCount());
        verify(vipOrderMapper, never()).insert(any(VipOrder.class));
    }

    @Test
    @DisplayName("检查并升级VIP - 已是活跃VIP")
    void checkAndUpgradeByOrders_AlreadyActiveVip() {
        User user = new User();
        user.setId(1L);
        user.setIsVip(1);
        user.setVipExpireTime(LocalDateTime.now().plusDays(10));

        when(vipOrderMapper.countCompletedOrders(1L)).thenReturn(30);
        when(userMapper.selectById(1L)).thenReturn(user);

        vipService.checkAndUpgradeByOrders(1L);

        verify(userMapper, times(1)).updateById(user);
        assertEquals(30, user.getVipOrderCount());
        verify(vipOrderMapper, never()).insert(any(VipOrder.class));
    }

    @Test
    @DisplayName("检查并升级VIP - 达到阈值触发升级")
    void checkAndUpgradeByOrders_TriggerUpgrade() {
        User user = new User();
        user.setId(1L);
        user.setIsVip(0);

        when(vipOrderMapper.countCompletedOrders(1L)).thenReturn(30);
        when(userMapper.selectById(1L)).thenReturn(user);

        vipService.checkAndUpgradeByOrders(1L);

        verify(userMapper, times(2)).updateById(user);
        assertEquals(30, user.getVipOrderCount());
        assertEquals(1, user.getIsVip());
        assertNotNull(user.getVipExpireTime());

        verify(vipOrderMapper, times(1)).insert(any(VipOrder.class));
    }

    @Test
    @DisplayName("发放每周VIP优惠券 - 无VIP用户")
    void distributeWeeklyVipCoupons_NoVipUsers() {
        when(userMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        vipService.distributeWeeklyVipCoupons();

        verify(couponMapper, never()).selectList(any(LambdaQueryWrapper.class));
        verify(userCouponMapper, never()).insert(any(UserCoupon.class));
    }

    @Test
    @DisplayName("发放每周VIP优惠券 - 无VIP优惠券")
    void distributeWeeklyVipCoupons_NoVipCoupons() {
        User user = new User();
        user.setId(1L);

        when(userMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(user));
        when(couponMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        vipService.distributeWeeklyVipCoupons();

        verify(userCouponMapper, never()).insert(any(UserCoupon.class));
    }

    @Test
    @DisplayName("发放每周VIP优惠券 - 成功发放")
    void distributeWeeklyVipCoupons_Success() {
        User user = new User();
        user.setId(1L);

        Coupon coupon = new Coupon();
        coupon.setId(1L);
        coupon.setValidDays(7);

        when(userMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(user));
        when(couponMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(coupon));
        when(userCouponMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        vipService.distributeWeeklyVipCoupons();

        verify(userCouponMapper, times(1)).insert(any(UserCoupon.class));
    }

    @Test
    @DisplayName("发放每周VIP优惠券 - 本周已发放")
    void distributeWeeklyVipCoupons_AlreadyDistributed() {
        User user = new User();
        user.setId(1L);

        Coupon coupon = new Coupon();
        coupon.setId(1L);

        when(userMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(user));
        when(couponMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(coupon));
        when(userCouponMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        vipService.distributeWeeklyVipCoupons();

        verify(userCouponMapper, never()).insert(any(UserCoupon.class));
    }

    @Test
    @DisplayName("获取所有VIP套餐列表")
    void listAllPlans() {
        Page<VipPlan> page = new Page<>(1, 10);
        when(vipPlanMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        Page<VipPlan> result = vipService.listAllPlans(1, 10);

        assertNotNull(result);
        verify(vipPlanMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("添加VIP套餐")
    void addPlan() {
        VipPlan plan = new VipPlan();
        plan.setName("Test Plan");

        vipService.addPlan(plan);

        assertEquals(1, plan.getStatus());
        verify(vipPlanMapper, times(1)).insert(plan);
    }

    @Test
    @DisplayName("更新VIP套餐 - 套餐不存在")
    void updatePlan_NotFound() {
        VipPlan plan = new VipPlan();
        plan.setId(1L);

        when(vipPlanMapper.selectById(1L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> vipService.updatePlan(plan));
        assertEquals(ResultCode.DATA_NOT_EXIST.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("更新VIP套餐 - 成功")
    void updatePlan_Success() {
        VipPlan plan = new VipPlan();
        plan.setId(1L);

        when(vipPlanMapper.selectById(1L)).thenReturn(new VipPlan());

        vipService.updatePlan(plan);

        verify(vipPlanMapper, times(1)).updateById(plan);
    }

    @Test
    @DisplayName("删除VIP套餐 - 套餐不存在")
    void deletePlan_NotFound() {
        when(vipPlanMapper.selectById(1L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> vipService.deletePlan(1L));
        assertEquals(ResultCode.DATA_NOT_EXIST.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("删除VIP套餐 - 成功")
    void deletePlan_Success() {
        when(vipPlanMapper.selectById(1L)).thenReturn(new VipPlan());

        vipService.deletePlan(1L);

        verify(vipPlanMapper, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("更新VIP套餐状态 - 套餐不存在")
    void updatePlanStatus_NotFound() {
        when(vipPlanMapper.selectById(1L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> vipService.updatePlanStatus(1L, 1));
        assertEquals(ResultCode.DATA_NOT_EXIST.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("更新VIP套餐状态 - 成功")
    void updatePlanStatus_Success() {
        VipPlan plan = new VipPlan();
        plan.setId(1L);

        when(vipPlanMapper.selectById(1L)).thenReturn(plan);

        vipService.updatePlanStatus(1L, 1);

        assertEquals(1, plan.getStatus());
        verify(vipPlanMapper, times(1)).updateById(plan);
    }

    @Test
    @DisplayName("获取VIP用户列表")
    void listVipUsers() {
        User user = new User();
        user.setId(1L);
        user.setUsername("test");
        user.setNickname("Test User");
        user.setPhone("13800138000");
        user.setAvatar("avatar.jpg");
        user.setIsVip(1);
        user.setVipExpireTime(LocalDateTime.now());
        user.setVipOrderCount(10);
        user.setPoints(100);

        Page<User> page = new Page<>(1, 10);
        page.setRecords(Collections.singletonList(user));
        page.setTotal(1);

        when(userMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        Page<Map<String, Object>> result = vipService.listVipUsers(1, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());

        Map<String, Object> map = result.getRecords().get(0);
        assertEquals(1L, map.get("id"));
        assertEquals("test", map.get("username"));
        assertEquals("Test User", map.get("nickname"));
        assertEquals("13800138000", map.get("phone"));
        assertEquals("avatar.jpg", map.get("avatar"));
        assertEquals(1, map.get("isVip"));
        assertEquals(user.getVipExpireTime(), map.get("vipExpireTime"));
        assertEquals(10, map.get("vipOrderCount"));
        assertEquals(100, map.get("points"));
    }

    @Test
    @DisplayName("获取VIP订单列表")
    void listVipOrders() {
        Page<VipOrder> page = new Page<>(1, 10);
        when(vipOrderMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        Page<VipOrder> result = vipService.listVipOrders(1, 10);

        assertNotNull(result);
        verify(vipOrderMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }
}
