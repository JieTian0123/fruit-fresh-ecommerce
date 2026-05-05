package com.fruit.controller.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fruit.common.exception.GlobalExceptionHandler;
import com.fruit.dto.CartDTO;
import com.fruit.entity.User;
import com.fruit.service.CartService;
import com.fruit.utils.UserContext;
import com.fruit.vo.CartVO;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @InjectMocks
    private CartController cartController;

    @Mock
    private CartService cartService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cartController)
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
        CartVO cartVO = new CartVO();
        cartVO.setId(1L);
        cartVO.setProductId(100L);
        cartVO.setQuantity(2);
        cartVO.setPrice(new BigDecimal("10.00"));
        cartVO.setCreateTime(LocalDateTime.now());
        List<CartVO> cartList = Arrays.asList(cartVO);

        when(cartService.getCartList(1L)).thenReturn(cartList);

        mockMvc.perform(get("/consumer/cart/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].productId").value(100));

        verify(cartService).getCartList(1L);
    }

    @Test
    void add() throws Exception {
        CartDTO dto = new CartDTO();
        dto.setProductId(100L);
        dto.setQuantity(2);

        mockMvc.perform(post("/consumer/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(cartService).addCart(1L, 100L, 2);
    }

    @Test
    void update() throws Exception {
        mockMvc.perform(put("/consumer/cart/update/1")
                .param("quantity", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(cartService).updateQuantity(1L, 1L, 3);
    }

    @Test
    void deleteCart() throws Exception {
        mockMvc.perform(delete("/consumer/cart/delete/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(cartService).deleteCart(1L, 1L);
    }

    @Test
    void clear() throws Exception {
        mockMvc.perform(delete("/consumer/cart/clear"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(cartService).clearCart(1L);
    }

    @Test
    void select() throws Exception {
        mockMvc.perform(put("/consumer/cart/select/1")
                .param("selected", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(cartService).updateSelected(1L, 1L, 1);
    }

    @Test
    void selectAll() throws Exception {
        mockMvc.perform(put("/consumer/cart/selectAll")
                .param("selected", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(cartService).selectAll(1L, 1);
    }
}
