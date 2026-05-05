package com.fruit.controller.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fruit.common.exception.GlobalExceptionHandler;
import com.fruit.common.result.PageResult;
import com.fruit.entity.Banner;
import com.fruit.entity.Product;
import com.fruit.service.BannerService;
import com.fruit.service.ProductService;
import com.fruit.vo.HomeActivityVO;
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
class HomeControllerTest {

    @InjectMocks
    private HomeController homeController;

    @Mock
    private BannerService bannerService;

    @Mock
    private ProductService productService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(homeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void index() throws Exception {
        Banner banner = new Banner();
        banner.setId(1L);
        banner.setImageUrl("http://example.com/banner.jpg");
        List<Banner> banners = Arrays.asList(banner);

        Product hotProduct = new Product();
        hotProduct.setId(1L);
        hotProduct.setName("Hot Apple");
        PageResult<Product> hotPageResult = PageResult.of(1L, 8L, 1L, Arrays.asList(hotProduct));

        Product newProduct = new Product();
        newProduct.setId(2L);
        newProduct.setName("New Banana");
        PageResult<Product> newPageResult = PageResult.of(1L, 8L, 1L, Arrays.asList(newProduct));

        when(bannerService.getHomeBanners()).thenReturn(banners);
        when(productService.listForConsumer(null, null, "sales", "desc", 1, 8)).thenReturn(hotPageResult);
        when(productService.listForConsumer(null, null, "createTime", "desc", 1, 8)).thenReturn(newPageResult);

        mockMvc.perform(get("/home/index"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.banners[0].imageUrl").value("http://example.com/banner.jpg"))
                .andExpect(jsonPath("$.data.hotProducts[0].name").value("Hot Apple"))
                .andExpect(jsonPath("$.data.newProducts[0].name").value("New Banana"));

        verify(bannerService).getHomeBanners();
        verify(productService).listForConsumer(null, null, "sales", "desc", 1, 8);
        verify(productService).listForConsumer(null, null, "createTime", "desc", 1, 8);
    }

    @Test
    void activities() throws Exception {
        HomeActivityVO activity = new HomeActivityVO();
        activity.setCode("DAILY_NEW");
        activity.setTitle("新鲜到店");
        activity.setImageUrl("/uploads/images/04c8c5a01c1d4dafafc0acb32822dfcf.jpg");

        when(bannerService.getEnabledHomeActivities()).thenReturn(Arrays.asList(activity));

        mockMvc.perform(get("/home/activities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].code").value("DAILY_NEW"))
                .andExpect(jsonPath("$.data[0].title").value("新鲜到店"));

        verify(bannerService).getEnabledHomeActivities();
    }
}
