package com.fruit.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fruit.common.exception.GlobalExceptionHandler;
import com.fruit.service.StatsService;
import com.fruit.utils.UserContext;
import com.fruit.vo.PeriodStatsVO;
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
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminStatsControllerTest {

    @InjectMocks
    private AdminStatsController controller;

    @Mock
    private StatsService statsService;

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
    void overview_Success() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("totalUsers", 100);
        data.put("totalSales", new BigDecimal("1000.00"));

        when(statsService.getAdminOverview()).thenReturn(data);

        mockMvc.perform(get("/admin/stats/overview")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalUsers").value(100))
                .andExpect(jsonPath("$.data.totalSales").value(1000.00));

        verify(statsService).getAdminOverview();
    }

    @Test
    void growth_Success() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("userGrowth", 10.5);
        data.put("salesGrowth", 5.2);

        when(statsService.getAdminGrowth()).thenReturn(data);

        mockMvc.perform(get("/admin/stats/growth")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userGrowth").value(10.5))
                .andExpect(jsonPath("$.data.salesGrowth").value(5.2));

        verify(statsService).getAdminGrowth();
    }

    @Test
    void periodStats_Success() throws Exception {
        PeriodStatsVO vo = new PeriodStatsVO();
        vo.setPeriodSales(new BigDecimal("500.00"));
        vo.setPeriodOrders(50L);
        vo.setTrendLabels(Arrays.asList("Mon", "Tue"));
        vo.setTrendSales(Arrays.asList(new BigDecimal("200.00"), new BigDecimal("300.00")));
        vo.setTrendOrders(Arrays.asList(20L, 30L));
        vo.setGrowthRate(15.0);

        when(statsService.getAdminPeriodStats("week")).thenReturn(vo);

        mockMvc.perform(get("/admin/stats/period")
                .param("period", "week")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.periodSales").value(500.00))
                .andExpect(jsonPath("$.data.periodOrders").value(50))
                .andExpect(jsonPath("$.data.growthRate").value(15.0));

        verify(statsService).getAdminPeriodStats("week");
    }
}
