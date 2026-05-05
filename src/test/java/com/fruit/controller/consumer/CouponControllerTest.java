package com.fruit.controller.consumer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fruit.common.exception.GlobalExceptionHandler;
import com.fruit.entity.Coupon;
import com.fruit.entity.User;
import com.fruit.service.CouponService;
import com.fruit.utils.UserContext;
import com.fruit.vo.UserCouponVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CouponControllerTest {

    @InjectMocks
    private CouponController couponController;

    @Mock
    private CouponService couponService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(couponController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        User user = new User();
        user.setId(1L);
        user.setUserType(0);
        UserContext.setUser(user);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void getAvailableCoupons() throws Exception {
        Page<Coupon> page = new Page<>();
        page.setRecords(Arrays.asList(new Coupon()));
        page.setTotal(1);

        when(couponService.getAvailableCoupons(1, 10)).thenReturn(page);
        when(couponService.getUserReceivedCouponIds(1L)).thenReturn(Arrays.asList(1L, 2L));

        mockMvc.perform(get("/coupon/available")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.receivedCouponIds").isArray());

        verify(couponService).getAvailableCoupons(1, 10);
        verify(couponService).getUserReceivedCouponIds(1L);
    }

    @Test
    void receiveCoupon() throws Exception {
        mockMvc.perform(post("/coupon/receive/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(couponService).receiveCoupon(1L, 1L);
    }

    @Test
    void getMyCoupons() throws Exception {
        Page page = new Page<>();
        when(couponService.getUserCoupons(eq(1L), eq(1), eq(10), any())).thenReturn(page);

        mockMvc.perform(get("/coupon/my")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(couponService).getUserCoupons(1L, 1, 10, 0);
    }

    @Test
    void getUsableCoupons() throws Exception {
        List<UserCouponVO> coupons = Arrays.asList(new UserCouponVO());
        when(couponService.getUsableCoupons(eq(1L), any(BigDecimal.class))).thenReturn(coupons);

        mockMvc.perform(get("/coupon/usable")
                        .param("amount", "100.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());

        verify(couponService).getUsableCoupons(eq(1L), any(BigDecimal.class));
    }

    @Test
    void getExchangeableCoupons() throws Exception {
        Page<Coupon> page = new Page<>();
        when(couponService.getExchangeableCoupons(1, 10)).thenReturn(page);

        mockMvc.perform(get("/coupon/exchangeable")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(couponService).getExchangeableCoupons(1, 10);
    }

    @Test
    void exchangeCoupon() throws Exception {
        mockMvc.perform(post("/coupon/exchange/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(couponService).exchangeCoupon(1L, 1L);
    }
}