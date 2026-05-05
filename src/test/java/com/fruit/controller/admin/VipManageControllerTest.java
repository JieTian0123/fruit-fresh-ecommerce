package com.fruit.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fruit.common.exception.GlobalExceptionHandler;
import com.fruit.entity.VipOrder;
import com.fruit.entity.VipPlan;
import com.fruit.service.VipService;
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
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class VipManageControllerTest {

    @InjectMocks
    private VipManageController controller;

    @Mock
    private VipService vipService;

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
    void listPlans_Success() throws Exception {
        Page<VipPlan> page = new Page<>(1, 10);
        VipPlan plan = new VipPlan();
        plan.setId(1L);
        plan.setName("Test Plan");
        page.setRecords(Collections.singletonList(plan));
        page.setTotal(1L);

        when(vipService.listAllPlans(1, 10)).thenReturn(page);

        mockMvc.perform(get("/admin/vip/plans")
                .param("pageNum", "1")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(1))
                .andExpect(jsonPath("$.data.records[0].name").value("Test Plan"));

        verify(vipService).listAllPlans(1, 10);
    }

    @Test
    void addPlan_Success() throws Exception {
        VipPlan plan = new VipPlan();
        plan.setName("New Plan");

        mockMvc.perform(post("/admin/vip/plan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(plan)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(vipService).addPlan(any(VipPlan.class));
    }

    @Test
    void updatePlan_Success() throws Exception {
        VipPlan plan = new VipPlan();
        plan.setId(1L);
        plan.setName("Updated Plan");

        mockMvc.perform(put("/admin/vip/plan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(plan)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(vipService).updatePlan(any(VipPlan.class));
    }

    @Test
    void deletePlan_Success() throws Exception {
        mockMvc.perform(delete("/admin/vip/plan/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(vipService).deletePlan(1L);
    }

    @Test
    void updatePlanStatus_Success() throws Exception {
        mockMvc.perform(put("/admin/vip/plan/1/status")
                .param("status", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(vipService).updatePlanStatus(1L, 1);
    }

    @Test
    void listVipUsers_Success() throws Exception {
        Page<Map<String, Object>> page = new Page<>(1, 10);
        Map<String, Object> user = new HashMap<>();
        user.put("id", 1L);
        user.put("username", "testuser");
        page.setRecords(Collections.singletonList(user));
        page.setTotal(1L);

        when(vipService.listVipUsers(1, 10)).thenReturn(page);

        mockMvc.perform(get("/admin/vip/users")
                .param("pageNum", "1")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(1))
                .andExpect(jsonPath("$.data.records[0].username").value("testuser"));

        verify(vipService).listVipUsers(1, 10);
    }

    @Test
    void listVipOrders_Success() throws Exception {
        Page<VipOrder> page = new Page<>(1, 10);
        VipOrder order = new VipOrder();
        order.setId(1L);
        order.setOrderNo("VIP123");
        page.setRecords(Collections.singletonList(order));
        page.setTotal(1L);

        when(vipService.listVipOrders(1, 10)).thenReturn(page);

        mockMvc.perform(get("/admin/vip/orders")
                .param("pageNum", "1")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(1))
                .andExpect(jsonPath("$.data.records[0].orderNo").value("VIP123"));

        verify(vipService).listVipOrders(1, 10);
    }
}
