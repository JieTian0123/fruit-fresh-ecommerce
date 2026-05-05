package com.fruit.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fruit.common.exception.GlobalExceptionHandler;
import com.fruit.common.result.PageResult;
import com.fruit.entity.MerchantShop;
import com.fruit.service.MerchantShopService;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ShopManageControllerTest {

    @InjectMocks
    private ShopManageController controller;

    @Mock
    private MerchantShopService merchantShopService;

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
        PageResult<MerchantShop> pageResult = new PageResult<>();
        MerchantShop shop = new MerchantShop();
        shop.setId(1L);
        shop.setShopName("Test Shop");
        pageResult.setList(Collections.singletonList(shop));
        pageResult.setTotal(1L);

        when(merchantShopService.listForAdmin(1, 1, 10)).thenReturn(pageResult);

        mockMvc.perform(get("/admin/shop/list")
                .param("status", "1")
                .param("pageNum", "1")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].id").value(1))
                .andExpect(jsonPath("$.data.list[0].shopName").value("Test Shop"));

        verify(merchantShopService).listForAdmin(1, 1, 10);
    }

    @Test
    void detail_Success() throws Exception {
        MerchantShop shop = new MerchantShop();
        shop.setId(1L);
        shop.setShopName("Test Shop");

        when(merchantShopService.getById(1L)).thenReturn(shop);

        mockMvc.perform(get("/admin/shop/detail/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.shopName").value("Test Shop"));

        verify(merchantShopService).getById(1L);
    }

    @Test
    void approve_Success() throws Exception {
        mockMvc.perform(put("/admin/shop/approve/1")
                .param("status", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(merchantShopService).approveShop(1L, 1);
    }
}
