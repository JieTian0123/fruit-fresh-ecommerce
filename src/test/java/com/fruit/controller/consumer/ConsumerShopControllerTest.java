package com.fruit.controller.consumer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fruit.common.exception.GlobalExceptionHandler;
import com.fruit.entity.MerchantShop;
import com.fruit.entity.Product;
import com.fruit.entity.User;
import com.fruit.mapper.MerchantShopMapper;
import com.fruit.mapper.ProductMapper;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ConsumerShopControllerTest {

    @InjectMocks
    private ConsumerShopController consumerShopController;

    @Mock
    private MerchantShopMapper merchantShopMapper;

    @Mock
    private ProductMapper productMapper;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(consumerShopController)
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
    void getShopInfo() throws Exception {
        MerchantShop shop = new MerchantShop();
        shop.setId(1L);
        when(merchantShopMapper.selectById(1L)).thenReturn(shop);

        mockMvc.perform(get("/consumer/shop/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(merchantShopMapper).selectById(1L);
    }

    @Test
    void getShopProducts() throws Exception {
        MerchantShop shop = new MerchantShop();
        shop.setId(1L);
        shop.setMerchantId(1L);
        shop.setStatus(1);
        when(merchantShopMapper.selectById(1L)).thenReturn(shop);

        Page<Product> page = new Page<>();
        page.setRecords(Arrays.asList(new Product()));
        page.setTotal(1L);
        page.setPages(1L);
        when(productMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        mockMvc.perform(get("/consumer/shop/1/products")
                        .param("pageNum", "1")
                        .param("pageSize", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1));

        verify(merchantShopMapper).selectById(1L);
        verify(productMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }
}