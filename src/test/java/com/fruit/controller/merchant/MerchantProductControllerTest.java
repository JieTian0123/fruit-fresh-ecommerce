package com.fruit.controller.merchant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fruit.common.exception.GlobalExceptionHandler;
import com.fruit.common.result.PageResult;
import com.fruit.dto.ProductDTO;
import com.fruit.entity.Product;
import com.fruit.entity.User;
import com.fruit.service.ProductService;
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

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MerchantProductControllerTest {

    @InjectMocks
    private MerchantProductController controller;

    @Mock
    private ProductService productService;

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
        PageResult<Product> pageResult = PageResult.of(1L, 10L, 1L, Collections.singletonList(new Product()));
        when(productService.listForMerchant(anyLong(), any(), anyInt(), anyInt())).thenReturn(pageResult);

        mockMvc.perform(get("/merchant/product/list")
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void detail_Success() throws Exception {
        Product product = new Product();
        product.setId(100L);
        product.setName("Apple");
        when(productService.getById(100L)).thenReturn(product);

        mockMvc.perform(get("/merchant/product/detail/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("Apple"));
    }

    @Test
    void add_Success() throws Exception {
        ProductDTO dto = new ProductDTO();
        dto.setName("Apple");
        dto.setCategoryId(1L);
        dto.setPrice(new BigDecimal("10.00"));
        dto.setStock(100);
        dto.setMainImage("image.jpg");

        mockMvc.perform(post("/merchant/product/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(productService).addProduct(eq(1L), any(ProductDTO.class));
    }

    @Test
    void update_Success() throws Exception {
        ProductDTO dto = new ProductDTO();
        dto.setName("Apple");
        dto.setCategoryId(1L);
        dto.setPrice(new BigDecimal("10.00"));
        dto.setStock(100);
        dto.setMainImage("image.jpg");

        mockMvc.perform(put("/merchant/product/update/100")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(productService).updateProduct(eq(1L), eq(100L), any(ProductDTO.class));
    }

    @Test
    void onSale_Success() throws Exception {
        mockMvc.perform(put("/merchant/product/onSale/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(productService).onSale(1L, 100L);
    }

    @Test
    void offSale_Success() throws Exception {
        mockMvc.perform(put("/merchant/product/offSale/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(productService).offSale(1L, 100L);
    }

    @Test
    void delete_Success() throws Exception {
        mockMvc.perform(delete("/merchant/product/delete/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(productService).deleteProduct(1L, 100L);
    }
}