package com.fruit.controller.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fruit.common.exception.GlobalExceptionHandler;
import com.fruit.common.result.PageResult;
import com.fruit.entity.Product;
import com.fruit.entity.ProductTraceability;
import com.fruit.entity.User;
import com.fruit.service.ProductService;
import com.fruit.service.ProductTraceabilityService;
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
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @InjectMocks
    private ProductController productController;

    @Mock
    private ProductService productService;

    @Mock
    private ProductTraceabilityService traceabilityService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
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
        Product product = new Product();
        product.setId(1L);
        product.setName("Apple");
        PageResult<Product> pageResult = PageResult.of(1L, 10L, 1L, Arrays.asList(product));

        when(productService.listForConsumer(1L, "Apple", "createTime", "desc", null, 1, 10)).thenReturn(pageResult);

        mockMvc.perform(get("/consumer/product/list")
                .param("categoryId", "1")
                .param("keyword", "Apple")
                .param("sortField", "createTime")
                .param("sortOrder", "desc")
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].name").value("Apple"));

        verify(productService).listForConsumer(1L, "Apple", "createTime", "desc", null, 1, 10);
    }

    @Test
    void search() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setName("Apple");
        PageResult<Product> pageResult = PageResult.of(1L, 12L, 1L, Arrays.asList(product));

        when(productService.listForConsumer(null, "Apple", "createTime", "desc", 1, 12)).thenReturn(pageResult);

        mockMvc.perform(get("/consumer/product/search")
                .param("keyword", "Apple")
                .param("pageNum", "1")
                .param("pageSize", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].name").value("Apple"));

        verify(productService).listForConsumer(null, "Apple", "createTime", "desc", 1, 12);
    }

    @Test
    void hot() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setName("Apple");
        List<Product> list = Arrays.asList(product);

        when(productService.getHotProducts(8)).thenReturn(list);

        mockMvc.perform(get("/consumer/product/hot")
                .param("limit", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].name").value("Apple"));

        verify(productService).getHotProducts(8);
    }

    @Test
    void newProducts() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setName("Apple");
        List<Product> list = Arrays.asList(product);

        when(productService.getNewProducts(8)).thenReturn(list);

        mockMvc.perform(get("/consumer/product/new")
                .param("limit", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].name").value("Apple"));

        verify(productService).getNewProducts(8);
    }

    @Test
    void recommend() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setName("Apple");
        List<Product> list = Arrays.asList(product);

        when(productService.getRecommendProducts(8)).thenReturn(list);

        mockMvc.perform(get("/consumer/product/recommend")
                .param("limit", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].name").value("Apple"));

        verify(productService).getRecommendProducts(8);
    }

    @Test
    void detail() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setName("Apple");

        when(productService.getProductDetail(1L)).thenReturn(product);

        mockMvc.perform(get("/consumer/product/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("Apple"));

        verify(productService).getProductDetail(1L);
    }

    @Test
    void traceability() throws Exception {
        ProductTraceability pt = new ProductTraceability();
        pt.setId(1L);
        pt.setNodeName("Farm");
        List<ProductTraceability> list = Arrays.asList(pt);

        when(traceabilityService.listByProductId(1L)).thenReturn(list);

        mockMvc.perform(get("/consumer/product/1/traceability"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].nodeName").value("Farm"));

        verify(traceabilityService).listByProductId(1L);
    }
}
