package com.fruit.controller.merchant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fruit.common.exception.GlobalExceptionHandler;
import com.fruit.entity.User;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MerchantStatsControllerTest {

    @InjectMocks
    private MerchantStatsController controller;

    @Mock
    private StatsService statsService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        User user = new User();
        user.setId(1L);
        user.setUserType(1);
        UserContext.setUser(user);

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
        data.put("totalSales", 1000);
        when(statsService.getMerchantOverview(1L)).thenReturn(data);

        mockMvc.perform(get("/merchant/stats/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalSales").value(1000));
    }

    @Test
    void periodStats_Success() throws Exception {
        PeriodStatsVO vo = new PeriodStatsVO();
        when(statsService.getMerchantPeriodStats(eq(1L), anyString())).thenReturn(vo);

        mockMvc.perform(get("/merchant/stats/period")
                .param("period", "week"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}