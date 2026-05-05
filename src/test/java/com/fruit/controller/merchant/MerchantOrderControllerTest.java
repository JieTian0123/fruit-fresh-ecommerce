package com.fruit.controller.merchant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fruit.common.exception.GlobalExceptionHandler;
import com.fruit.common.result.PageResult;
import com.fruit.dto.DeliverDTO;
import com.fruit.entity.Order;
import com.fruit.entity.User;
import com.fruit.service.OrderService;
import com.fruit.utils.UserContext;
import com.fruit.vo.OrderDetailVO;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MerchantOrderControllerTest {

    @InjectMocks
    private MerchantOrderController controller;

    @Mock
    private OrderService orderService;

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
    void list_Success() throws Exception {
        PageResult<Order> pageResult = PageResult.of(1L, 10L, 1L, Collections.singletonList(new Order()));
        when(orderService.listForMerchant(anyLong(), any(), any(), anyInt(), anyInt())).thenReturn(pageResult);

        mockMvc.perform(get("/merchant/order/list")
                .param("pageNum", "1")
                .param("orderNo", "ORDER")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1));

        verify(orderService).listForMerchant(eq(1L), any(), eq("ORDER"), eq(1), eq(10));
    }

    @Test
    void detail_Success() throws Exception {
        OrderDetailVO vo = new OrderDetailVO();
        vo.setOrderNo("ORDER123");
        when(orderService.getOrderDetail(eq("ORDER123"), any())).thenReturn(vo);

        mockMvc.perform(get("/merchant/order/detail/ORDER123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orderNo").value("ORDER123"));
    }

    @Test
    void deliver_Success() throws Exception {
        DeliverDTO dto = new DeliverDTO();
        dto.setOrderNo("ORDER123");
        dto.setDeliveryCompany("SF");
        dto.setDeliveryNo("SF123456");

        mockMvc.perform(post("/merchant/order/deliver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(orderService).deliverOrder(eq(1L), any(DeliverDTO.class));
    }
}
