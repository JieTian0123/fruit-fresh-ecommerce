package com.fruit.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fruit.common.exception.GlobalExceptionHandler;
import com.fruit.common.result.PageResult;
import com.fruit.entity.Order;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderManageControllerTest {

    @InjectMocks
    private OrderManageController controller;

    @Mock
    private OrderService orderService;

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
        PageResult<Order> pageResult = new PageResult<>();
        Order order = new Order();
        order.setId(1L);
        order.setOrderNo("ORDER123");
        pageResult.setList(Collections.singletonList(order));
        pageResult.setTotal(1L);

        when(orderService.listForAdmin(1, "ORDER123", 1, 10)).thenReturn(pageResult);

        mockMvc.perform(get("/admin/order/list")
                .param("status", "1")
                .param("orderNo", "ORDER123")
                .param("pageNum", "1")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].id").value(1))
                .andExpect(jsonPath("$.data.list[0].orderNo").value("ORDER123"));

        verify(orderService).listForAdmin(1, "ORDER123", 1, 10);
    }

    @Test
    void detail_Success() throws Exception {
        OrderDetailVO vo = new OrderDetailVO();
        vo.setId(1L);
        vo.setOrderNo("ORDER123");

        when(orderService.getOrderDetail("ORDER123", null)).thenReturn(vo);

        mockMvc.perform(get("/admin/order/detail/ORDER123")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.orderNo").value("ORDER123"));

        verify(orderService).getOrderDetail("ORDER123", null);
    }
}
