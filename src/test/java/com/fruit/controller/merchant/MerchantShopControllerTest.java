package com.fruit.controller.merchant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fruit.common.exception.GlobalExceptionHandler;
import com.fruit.dto.ShopDTO;
import com.fruit.entity.MerchantShop;
import com.fruit.entity.User;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MerchantShopControllerTest {

    @InjectMocks
    private MerchantShopController controller;

    @Mock
    private MerchantShopService merchantShopService;

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
    void info_Success() throws Exception {
        MerchantShop shop = new MerchantShop();
        shop.setShopName("My Shop");
        when(merchantShopService.getShopByMerchantId(1L)).thenReturn(shop);

        mockMvc.perform(get("/merchant/shop/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.shopName").value("My Shop"));
    }

    @Test
    void create_Success() throws Exception {
        ShopDTO dto = new ShopDTO();
        dto.setShopName("New Shop");
        dto.setDescription("Description");
        dto.setContactPhone("13800138000");

        mockMvc.perform(post("/merchant/shop/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(merchantShopService).createShop(eq(1L), any(ShopDTO.class));
    }

    @Test
    void update_Success() throws Exception {
        ShopDTO dto = new ShopDTO();
        dto.setShopName("Updated Shop");
        dto.setDescription("Updated Description");
        dto.setContactPhone("13800138000");

        mockMvc.perform(put("/merchant/shop/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(merchantShopService).updateShop(eq(1L), any(ShopDTO.class));
    }
}