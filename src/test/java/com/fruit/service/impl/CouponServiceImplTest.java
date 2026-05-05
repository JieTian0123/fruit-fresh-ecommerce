package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.common.exception.BusinessException;
import com.fruit.common.result.ResultCode;
import com.fruit.entity.Coupon;
import com.fruit.entity.UserCoupon;
import com.fruit.entity.UserPointsLog;
import com.fruit.mapper.CouponMapper;
import com.fruit.mapper.UserCouponMapper;
import com.fruit.mapper.UserPointsLogMapper;
import com.fruit.service.PointService;
import com.fruit.service.VipService;
import com.fruit.vo.UserCouponVO;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.Serializable;
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
class CouponServiceImplTest {

    @InjectMocks
    private CouponServiceImpl couponService;

    @Mock
    private CouponMapper couponMapper;

    @Mock
    private UserCouponMapper userCouponMapper;

    @Mock
    private UserPointsLogMapper userPointsLogMapper;

    @Mock
    private PointService pointService;

    @Mock
    private VipService vipService;

    @BeforeAll
    static void beforeAll() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new Configuration(), ""), Coupon.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new Configuration(), ""), UserCoupon.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new Configuration(), ""), UserPointsLog.class);
    }

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(couponService, "baseMapper", couponMapper);
    }

    @Test
    @DisplayName("测试获取优惠券列表 - 成功")
    void listCoupons_Success() {
        Page<Coupon> mockPage = new Page<>();
        mockPage.setRecords(Collections.singletonList(new Coupon()));
        when(couponMapper.selectPage(any(Page.class), any())).thenReturn(mockPage);

        Page<Coupon> result = couponService.listCoupons(1, 10, 1, "测试");

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        verify(couponMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测试添加优惠券 - 成功")
    void addCoupon_Success() {
        Coupon coupon = new Coupon();
        when(couponMapper.insert(any(Coupon.class))).thenReturn(1);

        couponService.addCoupon(coupon);

        assertEquals(0, coupon.getReceivedQuantity());
        assertEquals(0, coupon.getUsedQuantity());
        assertEquals(1, coupon.getStatus());
        verify(couponMapper).insert(coupon);
    }

    @Test
    @DisplayName("测试添加优惠券 - 领取后N天自动补齐领取窗口")
    void addCoupon_RelativeValidDays_FillsClaimWindow() {
        Coupon coupon = new Coupon();
        coupon.setValidDays(7);
        when(couponMapper.insert(any(Coupon.class))).thenReturn(1);

        couponService.addCoupon(coupon);

        assertNotNull(coupon.getValidFrom());
        assertNotNull(coupon.getValidUntil());
        assertTrue(coupon.getValidUntil().isAfter(coupon.getValidFrom()));
        assertEquals(7, coupon.getValidDays());
        verify(couponMapper).insert(coupon);
    }

    @Test
    @DisplayName("测试更新优惠券 - 成功")
    void updateCoupon_Success() {
        Coupon existingCoupon = new Coupon();
        existingCoupon.setId(1L);
        when(couponMapper.selectById(1L)).thenReturn(existingCoupon);
        when(couponMapper.updateById(any(Coupon.class))).thenReturn(1);

        Coupon updateCoupon = new Coupon();
        updateCoupon.setId(1L);
        updateCoupon.setReceivedQuantity(10);
        updateCoupon.setUsedQuantity(5);

        couponService.updateCoupon(updateCoupon);

        assertNull(updateCoupon.getReceivedQuantity());
        assertNull(updateCoupon.getUsedQuantity());
        verify(couponMapper).updateById(updateCoupon);
    }

    @Test
    @DisplayName("测试更新优惠券 - 优惠券不存在")
    void updateCoupon_NotFound() {
        when(couponMapper.selectById(1L)).thenReturn(null);

        Coupon updateCoupon = new Coupon();
        updateCoupon.setId(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> couponService.updateCoupon(updateCoupon));
        assertEquals(ResultCode.DATA_NOT_EXIST.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("测试删除优惠券 - 成功")
    void deleteCoupon_Success() {
        Coupon coupon = new Coupon();
        coupon.setId(1L);
        when(couponMapper.selectById(1L)).thenReturn(coupon);
        when(userCouponMapper.selectCount(any())).thenReturn(0L);
        when(couponMapper.deleteById(any(Coupon.class))).thenReturn(1);

        couponService.deleteCoupon(1L);

        verify(couponMapper).deleteById(any(Coupon.class));
    }

    @Test
    @DisplayName("测试删除优惠券 - 优惠券不存在")
    void deleteCoupon_NotFound() {
        when(couponMapper.selectById(1L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> couponService.deleteCoupon(1L));
        assertEquals(ResultCode.DATA_NOT_EXIST.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("测试删除优惠券 - 已有用户领取")
    void deleteCoupon_AlreadyReceived() {
        Coupon coupon = new Coupon();
        coupon.setId(1L);
        when(couponMapper.selectById(1L)).thenReturn(coupon);
        when(userCouponMapper.selectCount(any())).thenReturn(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> couponService.deleteCoupon(1L));
        assertEquals("该优惠券已有用户领取，无法删除", exception.getMessage());
    }

    @Test
    @DisplayName("测试更新优惠券状态 - 成功")
    void updateStatus_Success() {
        Coupon coupon = new Coupon();
        coupon.setId(1L);
        when(couponMapper.selectById(1L)).thenReturn(coupon);
        when(couponMapper.updateById(any(Coupon.class))).thenReturn(1);

        couponService.updateStatus(1L, 0);

        assertEquals(0, coupon.getStatus());
        verify(couponMapper).updateById(coupon);
    }

    @Test
    @DisplayName("测试更新优惠券状态 - 优惠券不存在")
    void updateStatus_NotFound() {
        when(couponMapper.selectById(1L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> couponService.updateStatus(1L, 0));
        assertEquals(ResultCode.DATA_NOT_EXIST.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("测试获取可用优惠券 - 成功")
    void getAvailableCoupons_Success() {
        Page<Coupon> mockPage = new Page<>();
        mockPage.setRecords(Collections.singletonList(new Coupon()));
        when(couponMapper.selectPage(any(Page.class), any())).thenReturn(mockPage);

        Page<Coupon> result = couponService.getAvailableCoupons(1, 10);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        verify(couponMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测试领取优惠券 - 优惠券不存在")
    void receiveCoupon_NotFound() {
        when(couponMapper.selectById(1L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> couponService.receiveCoupon(1L, 1L));
        assertEquals(ResultCode.DATA_NOT_EXIST.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("测试领取优惠券 - 优惠券已禁用")
    void receiveCoupon_Disabled() {
        Coupon coupon = new Coupon();
        coupon.setStatus(0);
        when(couponMapper.selectById(1L)).thenReturn(coupon);

        BusinessException exception = assertThrows(BusinessException.class, () -> couponService.receiveCoupon(1L, 1L));
        assertEquals("该优惠券已禁用", exception.getMessage());
    }

    @Test
    @DisplayName("测试领取优惠券 - 不在有效期内")
    void receiveCoupon_Expired() {
        Coupon coupon = new Coupon();
        coupon.setStatus(1);
        coupon.setValidFrom(LocalDateTime.now().plusDays(1));
        coupon.setValidUntil(LocalDateTime.now().plusDays(2));
        when(couponMapper.selectById(1L)).thenReturn(coupon);

        BusinessException exception = assertThrows(BusinessException.class, () -> couponService.receiveCoupon(1L, 1L));
        assertEquals("该优惠券不在有效期内", exception.getMessage());
    }

    @Test
    @DisplayName("测试领取优惠券 - 配置异常(totalQuantity为null)")
    void receiveCoupon_TotalQuantityNull() {
        Coupon coupon = new Coupon();
        coupon.setStatus(1);
        coupon.setValidFrom(LocalDateTime.now().minusDays(1));
        coupon.setValidUntil(LocalDateTime.now().plusDays(1));
        coupon.setTotalQuantity(null);
        when(couponMapper.selectById(1L)).thenReturn(coupon);

        BusinessException exception = assertThrows(BusinessException.class, () -> couponService.receiveCoupon(1L, 1L));
        assertEquals("该优惠券配置异常，请联系管理员", exception.getMessage());
    }

    @Test
    @DisplayName("测试领取优惠券 - 已被领完")
    void receiveCoupon_OutOfStock() {
        Coupon coupon = new Coupon();
        coupon.setStatus(1);
        coupon.setValidFrom(LocalDateTime.now().minusDays(1));
        coupon.setValidUntil(LocalDateTime.now().plusDays(1));
        coupon.setTotalQuantity(10);
        coupon.setReceivedQuantity(10);
        when(couponMapper.selectById(1L)).thenReturn(coupon);

        BusinessException exception = assertThrows(BusinessException.class, () -> couponService.receiveCoupon(1L, 1L));
        assertEquals("该优惠券已被领完", exception.getMessage());
    }

    @Test
    @DisplayName("测试领取优惠券 - VIP免费领取但已领取过")
    void receiveCoupon_VipAlreadyReceived() {
        Coupon coupon = new Coupon();
        coupon.setStatus(1);
        coupon.setValidFrom(LocalDateTime.now().minusDays(1));
        coupon.setValidUntil(LocalDateTime.now().plusDays(1));
        coupon.setTotalQuantity(10);
        coupon.setReceivedQuantity(0);
        coupon.setVipFreeReceive(1);
        when(couponMapper.selectById(1L)).thenReturn(coupon);
        when(userCouponMapper.selectCount(any())).thenReturn(1L);
        when(vipService.isVip(1L)).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> couponService.receiveCoupon(1L, 1L));
        assertEquals("VIP会员已免费领取过该优惠券", exception.getMessage());
    }

    @Test
    @DisplayName("测试领取优惠券 - 非VIP用户尝试免费领取VIP券")
    void receiveCoupon_NotVip() {
        Coupon coupon = new Coupon();
        coupon.setStatus(1);
        coupon.setValidFrom(LocalDateTime.now().minusDays(1));
        coupon.setValidUntil(LocalDateTime.now().plusDays(1));
        coupon.setTotalQuantity(10);
        coupon.setReceivedQuantity(0);
        coupon.setVipFreeReceive(1);
        when(couponMapper.selectById(1L)).thenReturn(coupon);
        when(userCouponMapper.selectCount(any())).thenReturn(0L);
        when(vipService.isVip(1L)).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> couponService.receiveCoupon(1L, 1L));
        assertEquals("该优惠券仅VIP会员可免费领取，非VIP用户请使用积分兑换", exception.getMessage());
    }

    @Test
    @DisplayName("测试领取优惠券 - 未开启VIP免费领取的积分券不能免费领取")
    void receiveCoupon_PointsCouponRequiresExchangeWhenVipFreeDisabled() {
        Coupon coupon = new Coupon();
        coupon.setStatus(1);
        coupon.setValidFrom(LocalDateTime.now().minusDays(1));
        coupon.setValidUntil(LocalDateTime.now().plusDays(1));
        coupon.setTotalQuantity(10);
        coupon.setReceivedQuantity(0);
        coupon.setVipFreeReceive(0);
        coupon.setPointsPrice(100);
        coupon.setPerUserLimit(2);
        when(couponMapper.selectById(1L)).thenReturn(coupon);
        when(userCouponMapper.selectCount(any())).thenReturn(0L);
        when(vipService.isVip(1L)).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> couponService.receiveCoupon(1L, 1L));
        assertEquals("该优惠券需使用积分兑换", exception.getMessage());
        verify(userCouponMapper, never()).insert(any(UserCoupon.class));
        verify(pointService, never()).deductPoints(anyLong(), anyInt(), anyInt(), anyString(), anyString());
    }

    @Test
    @DisplayName("测试领取优惠券 - 普通领取达到上限")
    void receiveCoupon_LimitReached() {
        Coupon coupon = new Coupon();
        coupon.setStatus(1);
        coupon.setValidFrom(LocalDateTime.now().minusDays(1));
        coupon.setValidUntil(LocalDateTime.now().plusDays(1));
        coupon.setTotalQuantity(10);
        coupon.setReceivedQuantity(0);
        coupon.setVipFreeReceive(0);
        coupon.setPerUserLimit(2);
        when(couponMapper.selectById(1L)).thenReturn(coupon);
        when(userCouponMapper.selectCount(any())).thenReturn(2L);
        when(vipService.isVip(1L)).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> couponService.receiveCoupon(1L, 1L));
        assertEquals("您已达到该优惠券的领取上限", exception.getMessage());
    }

    @Test
    @DisplayName("测试领取优惠券 - 成功(相对有效期)")
    void receiveCoupon_Success_RelativeValidDays() {
        Coupon coupon = new Coupon();
        coupon.setId(1L);
        coupon.setStatus(1);
        coupon.setValidFrom(LocalDateTime.now().minusDays(1));
        coupon.setValidUntil(LocalDateTime.now().plusDays(1));
        coupon.setTotalQuantity(10);
        coupon.setReceivedQuantity(0);
        coupon.setVipFreeReceive(0);
        coupon.setPerUserLimit(2);
        coupon.setValidDays(7);
        when(couponMapper.selectById(1L)).thenReturn(coupon);
        when(userCouponMapper.selectCount(any())).thenReturn(0L);
        when(vipService.isVip(1L)).thenReturn(false);
        when(userCouponMapper.insert(any(UserCoupon.class))).thenReturn(1);
        when(couponMapper.updateById(any(Coupon.class))).thenReturn(1);

        couponService.receiveCoupon(1L, 1L);

        ArgumentCaptor<UserCoupon> captor = ArgumentCaptor.forClass(UserCoupon.class);
        verify(userCouponMapper).insert(captor.capture());
        UserCoupon savedUserCoupon = captor.getValue();
        assertEquals(1L, savedUserCoupon.getUserId());
        assertEquals(1L, savedUserCoupon.getCouponId());
        assertEquals(0, savedUserCoupon.getStatus());
        assertNotNull(savedUserCoupon.getValidFrom());
        assertNotNull(savedUserCoupon.getValidUntil());
        assertEquals(1, coupon.getReceivedQuantity());
    }

    @Test
    @DisplayName("测试领取优惠券 - 相对有效期无需固定有效期")
    void receiveCoupon_Success_RelativeValidDaysWithoutFixedDates() {
        Coupon coupon = new Coupon();
        coupon.setId(1L);
        coupon.setStatus(1);
        coupon.setTotalQuantity(10);
        coupon.setReceivedQuantity(0);
        coupon.setVipFreeReceive(0);
        coupon.setPerUserLimit(2);
        coupon.setValidDays(7);
        when(couponMapper.selectById(1L)).thenReturn(coupon);
        when(userCouponMapper.selectCount(any())).thenReturn(0L);
        when(vipService.isVip(1L)).thenReturn(false);
        when(userCouponMapper.insert(any(UserCoupon.class))).thenReturn(1);
        when(couponMapper.updateById(any(Coupon.class))).thenReturn(1);

        couponService.receiveCoupon(1L, 1L);

        ArgumentCaptor<UserCoupon> captor = ArgumentCaptor.forClass(UserCoupon.class);
        verify(userCouponMapper).insert(captor.capture());
        UserCoupon savedUserCoupon = captor.getValue();
        assertNotNull(savedUserCoupon.getValidFrom());
        assertNotNull(savedUserCoupon.getValidUntil());
        assertEquals(1, coupon.getReceivedQuantity());
    }

    @Test
    @DisplayName("测试领取优惠券 - 成功(固定有效期)")
    void receiveCoupon_Success_FixedValidDates() {
        Coupon coupon = new Coupon();
        coupon.setId(1L);
        coupon.setStatus(1);
        LocalDateTime validFrom = LocalDateTime.now().minusDays(1);
        LocalDateTime validUntil = LocalDateTime.now().plusDays(1);
        coupon.setValidFrom(validFrom);
        coupon.setValidUntil(validUntil);
        coupon.setTotalQuantity(10);
        coupon.setReceivedQuantity(0);
        coupon.setVipFreeReceive(0);
        coupon.setPerUserLimit(2);
        coupon.setValidDays(null);
        when(couponMapper.selectById(1L)).thenReturn(coupon);
        when(userCouponMapper.selectCount(any())).thenReturn(0L);
        when(vipService.isVip(1L)).thenReturn(false);
        when(userCouponMapper.insert(any(UserCoupon.class))).thenReturn(1);
        when(couponMapper.updateById(any(Coupon.class))).thenReturn(1);

        couponService.receiveCoupon(1L, 1L);

        ArgumentCaptor<UserCoupon> captor = ArgumentCaptor.forClass(UserCoupon.class);
        verify(userCouponMapper).insert(captor.capture());
        UserCoupon savedUserCoupon = captor.getValue();
        assertEquals(validFrom, savedUserCoupon.getValidFrom());
        assertEquals(validUntil, savedUserCoupon.getValidUntil());
    }

    @Test
    @DisplayName("测试获取用户优惠券 - 成功")
    void getUserCoupons_Success() {
        UserCoupon uc1 = new UserCoupon();
        uc1.setId(1L);
        uc1.setCouponId(1L);
        uc1.setStatus(0);
        uc1.setValidUntil(LocalDateTime.now().minusDays(1)); // Expired

        UserCoupon uc2 = new UserCoupon();
        uc2.setId(2L);
        uc2.setCouponId(2L);
        uc2.setStatus(0);
        uc2.setValidUntil(LocalDateTime.now().plusDays(1)); // Valid

        when(userCouponMapper.selectList(any())).thenReturn(Arrays.asList(uc1, uc2));
        when(userCouponMapper.updateById(any(UserCoupon.class))).thenReturn(1);

        Page<UserCoupon> mockPage = new Page<>();
        mockPage.setRecords(Arrays.asList(uc1, uc2));
        mockPage.setTotal(2);
        when(userCouponMapper.selectPage(any(Page.class), any())).thenReturn(mockPage);

        Coupon c1 = new Coupon();
        c1.setId(1L);
        c1.setTitle("Coupon 1");
        Coupon c2 = new Coupon();
        c2.setId(2L);
        c2.setTitle("Coupon 2");
        when(couponMapper.selectBatchIds(any())).thenReturn(Arrays.asList(c1, c2));

        Page<UserCouponVO> result = couponService.getUserCoupons(1L, 1, 10, null);

        assertNotNull(result);
        assertEquals(2, result.getRecords().size());
        assertEquals(2, uc1.getStatus()); // Should be updated to expired
        verify(userCouponMapper).updateById(uc1);
    }

    @Test
    @DisplayName("测试获取用户优惠券 - 空结果")
    void getUserCoupons_Empty() {
        when(userCouponMapper.selectList(any())).thenReturn(Collections.emptyList());

        Page<UserCoupon> mockPage = new Page<>();
        mockPage.setRecords(Collections.emptyList());
        mockPage.setTotal(0);
        when(userCouponMapper.selectPage(any(Page.class), any())).thenReturn(mockPage);

        Page<UserCouponVO> result = couponService.getUserCoupons(1L, 1, 10, null);

        assertNotNull(result);
        assertTrue(result.getRecords().isEmpty());
    }

    @Test
    @DisplayName("测试获取可用优惠券(下单时) - 成功")
    void getUsableCoupons_Success() {
        UserCoupon uc1 = new UserCoupon();
        uc1.setId(1L);
        uc1.setCouponId(1L);
        uc1.setStatus(0);

        UserCoupon uc2 = new UserCoupon();
        uc2.setId(2L);
        uc2.setCouponId(2L);
        uc2.setStatus(0);

        when(userCouponMapper.selectList(any())).thenReturn(Arrays.asList(uc1, uc2));

        Coupon c1 = new Coupon();
        c1.setId(1L);
        c1.setStatus(1);
        c1.setMinimumAmount(new BigDecimal("100"));

        Coupon c2 = new Coupon();
        c2.setId(2L);
        c2.setStatus(1);
        c2.setMinimumAmount(new BigDecimal("200"));

        when(couponMapper.selectList(any())).thenReturn(Arrays.asList(c1, c2));

        List<UserCouponVO> result = couponService.getUsableCoupons(1L, new BigDecimal("150"));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getCouponId()); // Only c1 is usable
    }

    @Test
    @DisplayName("测试获取可用优惠券(下单时) - 空结果")
    void getUsableCoupons_Empty() {
        when(userCouponMapper.selectList(any())).thenReturn(Collections.emptyList());

        List<UserCouponVO> result = couponService.getUsableCoupons(1L, new BigDecimal("150"));

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试获取可兑换优惠券 - 成功")
    void getExchangeableCoupons_Success() {
        Page<Coupon> mockPage = new Page<>();
        mockPage.setRecords(Collections.singletonList(new Coupon()));
        when(couponMapper.selectPage(any(Page.class), any())).thenReturn(mockPage);

        Page<Coupon> result = couponService.getExchangeableCoupons(1, 10);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        verify(couponMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测试兑换优惠券 - 优惠券不存在")
    void exchangeCoupon_NotFound() {
        when(couponMapper.selectById(1L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> couponService.exchangeCoupon(1L, 1L));
        assertEquals(ResultCode.DATA_NOT_EXIST.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("测试兑换优惠券 - 不支持积分兑换")
    void exchangeCoupon_NotExchangeable() {
        Coupon coupon = new Coupon();
        coupon.setPointsPrice(null);
        when(couponMapper.selectById(1L)).thenReturn(coupon);

        BusinessException exception = assertThrows(BusinessException.class, () -> couponService.exchangeCoupon(1L, 1L));
        assertEquals("该优惠券不支持积分兑换", exception.getMessage());
    }

    @Test
    @DisplayName("测试兑换优惠券 - 优惠券已禁用")
    void exchangeCoupon_Disabled() {
        Coupon coupon = new Coupon();
        coupon.setPointsPrice(100);
        coupon.setStatus(0);
        when(couponMapper.selectById(1L)).thenReturn(coupon);

        BusinessException exception = assertThrows(BusinessException.class, () -> couponService.exchangeCoupon(1L, 1L));
        assertEquals("该优惠券已禁用", exception.getMessage());
    }

    @Test
    @DisplayName("测试兑换优惠券 - 不在有效期内")
    void exchangeCoupon_Expired() {
        Coupon coupon = new Coupon();
        coupon.setPointsPrice(100);
        coupon.setStatus(1);
        coupon.setValidFrom(LocalDateTime.now().plusDays(1));
        coupon.setValidUntil(LocalDateTime.now().plusDays(2));
        when(couponMapper.selectById(1L)).thenReturn(coupon);

        BusinessException exception = assertThrows(BusinessException.class, () -> couponService.exchangeCoupon(1L, 1L));
        assertEquals("该优惠券不在有效期内", exception.getMessage());
    }

    @Test
    @DisplayName("测试兑换优惠券 - 配置异常(totalQuantity为null)")
    void exchangeCoupon_TotalQuantityNull() {
        Coupon coupon = new Coupon();
        coupon.setPointsPrice(100);
        coupon.setStatus(1);
        coupon.setValidFrom(LocalDateTime.now().minusDays(1));
        coupon.setValidUntil(LocalDateTime.now().plusDays(1));
        coupon.setTotalQuantity(null);
        when(couponMapper.selectById(1L)).thenReturn(coupon);

        BusinessException exception = assertThrows(BusinessException.class, () -> couponService.exchangeCoupon(1L, 1L));
        assertEquals("该优惠券配置异常，请联系管理员", exception.getMessage());
    }

    @Test
    @DisplayName("测试兑换优惠券 - 已兑完")
    void exchangeCoupon_OutOfStock() {
        Coupon coupon = new Coupon();
        coupon.setPointsPrice(100);
        coupon.setStatus(1);
        coupon.setValidFrom(LocalDateTime.now().minusDays(1));
        coupon.setValidUntil(LocalDateTime.now().plusDays(1));
        coupon.setTotalQuantity(10);
        coupon.setReceivedQuantity(10);
        when(couponMapper.selectById(1L)).thenReturn(coupon);

        BusinessException exception = assertThrows(BusinessException.class, () -> couponService.exchangeCoupon(1L, 1L));
        assertEquals("该优惠券已兑完", exception.getMessage());
    }

    @Test
    @DisplayName("测试兑换优惠券 - 达到兑换上限")
    void exchangeCoupon_LimitReached() {
        Coupon coupon = new Coupon();
        coupon.setPointsPrice(100);
        coupon.setStatus(1);
        coupon.setValidFrom(LocalDateTime.now().minusDays(1));
        coupon.setValidUntil(LocalDateTime.now().plusDays(1));
        coupon.setTotalQuantity(10);
        coupon.setReceivedQuantity(0);
        coupon.setPerUserLimit(2);
        when(couponMapper.selectById(1L)).thenReturn(coupon);
        when(userCouponMapper.selectCount(any())).thenReturn(2L);

        BusinessException exception = assertThrows(BusinessException.class, () -> couponService.exchangeCoupon(1L, 1L));
        assertEquals("您已达到该优惠券的兑换上限", exception.getMessage());
    }

    @Test
    @DisplayName("测试兑换优惠券 - 成功")
    void exchangeCoupon_Success() {
        Coupon coupon = new Coupon();
        coupon.setId(1L);
        coupon.setTitle("Test Coupon");
        coupon.setPointsPrice(100);
        coupon.setStatus(1);
        coupon.setValidFrom(LocalDateTime.now().minusDays(1));
        coupon.setValidUntil(LocalDateTime.now().plusDays(1));
        coupon.setTotalQuantity(10);
        coupon.setReceivedQuantity(0);
        coupon.setPerUserLimit(2);
        coupon.setValidDays(7);
        when(couponMapper.selectById(1L)).thenReturn(coupon);
        when(userCouponMapper.selectCount(any())).thenReturn(0L);
        doNothing().when(pointService).deductPoints(eq(1L), eq(100), eq(5), eq("1"), anyString());
        when(userCouponMapper.insert(any(UserCoupon.class))).thenReturn(1);
        when(couponMapper.updateById(any(Coupon.class))).thenReturn(1);

        couponService.exchangeCoupon(1L, 1L);

        verify(pointService).deductPoints(eq(1L), eq(100), eq(5), eq("1"), anyString());
        verify(userCouponMapper).insert(any(UserCoupon.class));
        assertEquals(1, coupon.getReceivedQuantity());
        verify(couponMapper).updateById(coupon);
    }

    @Test
    @DisplayName("测试获取用户已领取的优惠券ID列表 - 成功")
    void getUserReceivedCouponIds_Success() {
        UserCoupon uc1 = new UserCoupon();
        uc1.setCouponId(1L);
        UserCoupon uc2 = new UserCoupon();
        uc2.setCouponId(2L);
        UserCoupon uc3 = new UserCoupon();
        uc3.setCouponId(1L); // Duplicate

        when(userCouponMapper.selectList(any())).thenReturn(Arrays.asList(uc1, uc2, uc3));

        List<Long> result = couponService.getUserReceivedCouponIds(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(1L));
        assertTrue(result.contains(2L));
    }

    @Test
    @DisplayName("测试获取用户优惠券获得方式 - 兑换和领取分别标记")
    void getUserCouponAcquireTypeMap_DistinguishesReceiveAndExchange() {
        UserCoupon received = new UserCoupon();
        received.setCouponId(1L);
        UserCoupon exchanged = new UserCoupon();
        exchanged.setCouponId(2L);
        when(userCouponMapper.selectList(any())).thenReturn(Arrays.asList(received, exchanged));

        UserPointsLog exchangeLog = new UserPointsLog();
        exchangeLog.setSourceId("2");
        when(userPointsLogMapper.selectList(any())).thenReturn(Collections.singletonList(exchangeLog));

        Map<Long, String> result = couponService.getUserCouponAcquireTypeMap(1L);

        assertEquals("receive", result.get(1L));
        assertEquals("exchange", result.get(2L));
    }
}
