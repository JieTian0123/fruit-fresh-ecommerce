package com.fruit.controller.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fruit.common.exception.GlobalExceptionHandler;
import com.fruit.entity.User;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class VipControllerTest {

    @InjectMocks
    private VipController vipController;

    @Mock
    private VipService vipService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(vipController)
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
    void getVipStatus() throws Exception {
        Map<String, Object> statusMap = new HashMap<>();
        statusMap.put("isVip", true);
        when(vipService.getVipStatus(1L)).thenReturn(statusMap);

        mockMvc.perform(get("/consumer/vip/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.isVip").value(true));

        verify(vipService).getVipStatus(1L);
    }

    @Test
    void getAvailablePlans() throws Exception {
        when(vipService.getAvailablePlans()).thenReturn(Arrays.asList(new VipPlan()));

        mockMvc.perform(get("/consumer/vip/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());

        verify(vipService).getAvailablePlans();
    }

    @Test
    void purchaseVip() throws Exception {
        VipOrder order = new VipOrder();
        order.setId(1L);
        when(vipService.purchaseVip(1L, 1L)).thenReturn(order);

        mockMvc.perform(post("/consumer/vip/purchase/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(vipService).purchaseVip(1L, 1L);
    }
}