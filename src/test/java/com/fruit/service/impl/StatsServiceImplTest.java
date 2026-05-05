package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fruit.entity.MerchantShop;
import com.fruit.entity.Order;
import com.fruit.entity.Product;
import com.fruit.entity.User;
import com.fruit.mapper.MerchantShopMapper;
import com.fruit.mapper.OrderMapper;
import com.fruit.mapper.ProductMapper;
import com.fruit.mapper.UserMapper;
import com.fruit.vo.PeriodStatsVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private MerchantShopMapper merchantShopMapper;

    @InjectMocks
    private StatsServiceImpl statsService;

    @Test
    void getAdminOverview_Success() {
        // Mock user counts
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(100L, 20L, 5L);

        // Mock product count
        when(productMapper.selectCount(null)).thenReturn(500L);

        // Mock order count
        when(orderMapper.selectCount(any())).thenReturn(1000L, 50L);

        // Mock completed orders
        Order order1 = new Order();
        order1.setPayAmount(new BigDecimal("100.00"));
        Order order2 = new Order();
        order2.setPayAmount(new BigDecimal("200.00"));
        when(orderMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(order1, order2), Collections.singletonList(order1));

        Map<String, Object> result = statsService.getAdminOverview();

        assertNotNull(result);
        assertEquals(100L, result.get("totalUsers"));
        assertEquals(20L, result.get("totalMerchants"));
        assertEquals(500L, result.get("totalProducts"));
        assertEquals(1000L, result.get("totalOrders"));
        assertEquals(new BigDecimal("300.00"), result.get("totalSales"));
        assertEquals(50L, result.get("todayOrders"));
        assertEquals(new BigDecimal("100.00"), result.get("todaySales"));
        assertEquals(5L, result.get("todayNewUsers"));
    }

    @Test
    void getAdminGrowth_Success() {
        // Mock order counts
        when(orderMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(100L, 50L);

        // Mock sales
        Order order1 = new Order();
        order1.setPayAmount(new BigDecimal("1000.00"));
        Order order2 = new Order();
        order2.setPayAmount(new BigDecimal("500.00"));
        when(orderMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(order1), Collections.singletonList(order2));

        Map<String, Object> result = statsService.getAdminGrowth();

        assertNotNull(result);
        assertEquals(100.0, result.get("ordersGrowth")); // (100-50)/50 * 100 = 100.0
        assertEquals(100.0, result.get("salesGrowth")); // (1000-500)/500 * 100 = 100.0
    }

    @Test
    void getAdminGrowth_ZeroPrevious_Success() {
        // Mock order counts
        when(orderMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(100L, 0L);

        // Mock sales
        Order order1 = new Order();
        order1.setPayAmount(new BigDecimal("1000.00"));
        when(orderMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(order1), Collections.emptyList());

        Map<String, Object> result = statsService.getAdminGrowth();

        assertNotNull(result);
        assertEquals(100.0, result.get("ordersGrowth"));
        assertEquals(100.0, result.get("salesGrowth"));
    }

    @Test
    void getMerchantOverview_Success() {
        Long merchantId = 1L;

        // Mock shop
        MerchantShop shop = new MerchantShop();
        shop.setMerchantId(merchantId);
        when(merchantShopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);

        // Mock product counts
        when(productMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(50L, 100L);

        // Mock order counts
        when(orderMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(10L, 200L, 5L);

        // Mock sales
        Order order1 = new Order();
        order1.setPayAmount(new BigDecimal("500.00"));
        Order order2 = new Order();
        order2.setPayAmount(new BigDecimal("100.00"));
        when(orderMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(order1, order2), Collections.singletonList(order2));

        Map<String, Object> result = statsService.getMerchantOverview(merchantId);

        assertNotNull(result);
        assertEquals(50L, result.get("onSaleProducts"));
        assertEquals(100L, result.get("totalProducts"));
        assertEquals(10L, result.get("pendingShipment"));
        assertEquals(200L, result.get("totalOrders"));
        assertEquals(new BigDecimal("600.00"), result.get("totalSales"));
        assertEquals(5L, result.get("todayOrders"));
        assertEquals(new BigDecimal("100.00"), result.get("todaySales"));
    }

    @Test
    void getAdminPeriodStats_Today_Success() {
        Order order = new Order();
        order.setPayAmount(new BigDecimal("100.00"));
        when(orderMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(order));

        PeriodStatsVO result = statsService.getAdminPeriodStats("today");

        assertNotNull(result);
        assertEquals(new BigDecimal("100.00"), result.getPeriodSales());
        assertEquals(1L, result.getPeriodOrders());
        assertEquals(0.0, result.getGrowthRate()); // 100 - 100 / 100 = 0
        assertEquals(12, result.getTrendLabels().size());
        assertEquals(12, result.getTrendSales().size());
        assertEquals(12, result.getTrendOrders().size());
    }

    @Test
    void getMerchantPeriodStats_Today_Success() {
        Long merchantId = 1L;
        Order order = new Order();
        order.setPayAmount(new BigDecimal("100.00"));
        when(orderMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(order));

        PeriodStatsVO result = statsService.getMerchantPeriodStats(merchantId, "today");

        assertNotNull(result);
        assertEquals(new BigDecimal("100.00"), result.getPeriodSales());
        assertEquals(1L, result.getPeriodOrders());
        assertEquals(0.0, result.getGrowthRate());
        assertEquals(12, result.getTrendLabels().size());
        assertEquals(12, result.getTrendSales().size());
        assertEquals(12, result.getTrendOrders().size());
    }
}
