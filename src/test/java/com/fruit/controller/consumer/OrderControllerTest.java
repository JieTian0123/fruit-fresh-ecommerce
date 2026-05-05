package com.fruit.controller.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fruit.common.exception.GlobalExceptionHandler;
import com.fruit.common.result.PageResult;
import com.fruit.dto.OrderCreateDTO;
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

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @InjectMocks
    private OrderController orderController;

    @Mock
    private OrderService orderService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
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
    void list() throws Exception {
        Order order = new Order();
        order.setId(1L);
        order.setOrderNo("ORD123");
        PageResult<Order> pageResult = PageResult.of(1L, 10L, 1L, java.util.Collections.singletonList(order));

        when(orderService.listForConsumer(1L, 1, 1, 10)).thenReturn(pageResult);

        mockMvc.perform(get("/consumer/order/list")
                .param("status", "1")
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].orderNo").value("ORD123"));

        verify(orderService).listForConsumer(1L, 1, 1, 10);
    }

    @Test
    void detail() throws Exception {
        OrderDetailVO vo = new OrderDetailVO();
        vo.setId(1L);
        vo.setOrderNo("ORD123");

        when(orderService.getOrderDetail("ORD123", 1L)).thenReturn(vo);

        mockMvc.perform(get("/consumer/order/detail/ORD123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orderNo").value("ORD123"));

        verify(orderService).getOrderDetail("ORD123", 1L);
    }

    @Test
    void create() throws Exception {
        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setAddressId(10L);

        when(orderService.createOrder(eq(1L), any(OrderCreateDTO.class))).thenReturn("ORD123");

        mockMvc.perform(post("/consumer/order/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("ORD123"));

        verify(orderService).createOrder(eq(1L), any(OrderCreateDTO.class));
    }

    @Test
    void cancel() throws Exception {
        mockMvc.perform(put("/consumer/order/cancel/ORD123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(orderService).cancelOrder(1L, "ORD123");
    }

    @Test
    void pay() throws Exception {
        mockMvc.perform(put("/consumer/order/pay/ORD123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(orderService).payOrder(1L, "ORD123");
    }

    @Test
    void confirm() throws Exception {
        mockMvc.perform(put("/consumer/order/confirm/ORD123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(orderService).confirmReceive(1L, "ORD123");
    }

    @Test
    void refund() throws Exception {
        mockMvc.perform(put("/consumer/order/refund/ORD123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(orderService).refundApply(1L, "ORD123");
    }
}
