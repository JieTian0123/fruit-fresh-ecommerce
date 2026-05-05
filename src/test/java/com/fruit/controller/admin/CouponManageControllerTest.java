package com.fruit.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fruit.common.exception.GlobalExceptionHandler;
import com.fruit.entity.Coupon;
import com.fruit.service.CouponService;
import com.fruit.utils.UserContext;
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

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CouponManageControllerTest {

    @InjectMocks
    private CouponManageController controller;

    @Mock
    private CouponService couponService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void list_Success() throws Exception {
        Page<Coupon> page = new Page<>(1, 10);
        Coupon coupon = new Coupon();
        coupon.setId(1L);
        coupon.setTitle("Test Coupon");
        page.setRecords(Collections.singletonList(coupon));
        page.setTotal(1);

        when(couponService.listCoupons(1, 10, 1, "Test")).thenReturn(page);

        mockMvc.perform(get("/admin/coupon/list")
                .param("pageNum", "1")
                .param("pageSize", "10")
                .param("status", "1")
                .param("keyword", "Test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(1))
                .andExpect(jsonPath("$.data.records[0].title").value("Test Coupon"));

        verify(couponService).listCoupons(1, 10, 1, "Test");
    }

    @Test
    void detail_Success() throws Exception {
        Coupon coupon = new Coupon();
        coupon.setId(1L);
        coupon.setTitle("Test Coupon");

        when(couponService.getById(1L)).thenReturn(coupon);

        mockMvc.perform(get("/admin/coupon/detail/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Test Coupon"));

        verify(couponService).getById(1L);
    }

    @Test
    void add_Success() throws Exception {
        Coupon coupon = new Coupon();
        coupon.setTitle("New Coupon");

        mockMvc.perform(post("/admin/coupon/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(coupon)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(couponService).addCoupon(any(Coupon.class));
    }

    @Test
    void update_Success() throws Exception {
        Coupon coupon = new Coupon();
        coupon.setId(1L);
        coupon.setTitle("Updated Coupon");

        mockMvc.perform(put("/admin/coupon/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(coupon)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(couponService).updateCoupon(any(Coupon.class));
    }

    @Test
    void delete_Success() throws Exception {
        mockMvc.perform(delete("/admin/coupon/delete/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(couponService).deleteCoupon(1L);
    }

    @Test
    void updateStatus_Success() throws Exception {
        mockMvc.perform(put("/admin/coupon/status/1")
                .param("status", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(couponService).updateStatus(1L, 1);
    }
}
