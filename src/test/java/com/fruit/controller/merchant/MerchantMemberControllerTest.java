package com.fruit.controller.merchant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fruit.common.exception.GlobalExceptionHandler;
import com.fruit.entity.MemberLevel;
import com.fruit.entity.User;
import com.fruit.mapper.MemberLevelMapper;
import com.fruit.mapper.OrderMapper;
import com.fruit.mapper.UserMapper;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MerchantMemberControllerTest {

    @InjectMocks
    private MerchantMemberController controller;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private MemberLevelMapper memberLevelMapper;

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
    void listMemberCustomers_Success() throws Exception {
        when(orderMapper.countDistinctCustomers(1L)).thenReturn(1L);

        Map<String, Object> stat = new HashMap<>();
        stat.put("user_id", 2L);
        stat.put("order_count", 5);
        stat.put("total_spend", new BigDecimal("100.00"));
        stat.put("last_order_time", LocalDateTime.now());
        when(orderMapper.selectMemberCustomers(anyLong(), anyInt(), anyInt())).thenReturn(Arrays.asList(stat));

        User customer = new User();
        customer.setId(2L);
        customer.setUsername("customer1");
        customer.setNickname("Customer 1");
        customer.setMemberLevelId(1L);
        when(userMapper.selectBatchIds(any())).thenReturn(Arrays.asList(customer));

        MemberLevel level = new MemberLevel();
        level.setId(1L);
        level.setLevelName("Gold");
        when(memberLevelMapper.selectList(null)).thenReturn(Arrays.asList(level));

        mockMvc.perform(get("/merchant/member-customers/list")
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].userId").value(2))
                .andExpect(jsonPath("$.data.list[0].username").value("customer1"))
                .andExpect(jsonPath("$.data.list[0].memberLevelName").value("Gold"));
    }

    @Test
    void listMemberCustomers_Empty() throws Exception {
        when(orderMapper.countDistinctCustomers(1L)).thenReturn(0L);

        mockMvc.perform(get("/merchant/member-customers/list")
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.list").isEmpty());
    }
}