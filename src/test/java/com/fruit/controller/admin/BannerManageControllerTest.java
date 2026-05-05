package com.fruit.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fruit.common.exception.GlobalExceptionHandler;
import com.fruit.common.result.PageResult;
import com.fruit.dto.BannerDTO;
import com.fruit.entity.Banner;
import com.fruit.service.BannerService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BannerManageControllerTest {

    @InjectMocks
    private BannerManageController controller;

    @Mock
    private BannerService bannerService;

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
        PageResult<Banner> pageResult = new PageResult<>();
        Banner banner = new Banner();
        banner.setId(1L);
        banner.setTitle("Test Banner");
        pageResult.setList(Collections.singletonList(banner));
        pageResult.setTotal(1L);

        when(bannerService.listForAdmin(1, 10)).thenReturn(pageResult);

        mockMvc.perform(get("/admin/banner/list")
                .param("pageNum", "1")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].id").value(1))
                .andExpect(jsonPath("$.data.list[0].title").value("Test Banner"));

        verify(bannerService).listForAdmin(1, 10);
    }

    @Test
    void detail_Success() throws Exception {
        Banner banner = new Banner();
        banner.setId(1L);
        banner.setTitle("Test Banner");

        when(bannerService.getById(1L)).thenReturn(banner);

        mockMvc.perform(get("/admin/banner/detail/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Test Banner"));

        verify(bannerService).getById(1L);
    }

    @Test
    void add_Success() throws Exception {
        BannerDTO dto = new BannerDTO();
        dto.setTitle("New Banner");
        dto.setImageUrl("http://example.com/image.jpg");

        mockMvc.perform(post("/admin/banner/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(bannerService).addBanner(any(BannerDTO.class));
    }

    @Test
    void update_Success() throws Exception {
        BannerDTO dto = new BannerDTO();
        dto.setTitle("Updated Banner");
        dto.setImageUrl("http://example.com/image.jpg");

        mockMvc.perform(put("/admin/banner/update/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(bannerService).updateBanner(eq(1L), any(BannerDTO.class));
    }

    @Test
    void delete_Success() throws Exception {
        mockMvc.perform(delete("/admin/banner/delete/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(bannerService).deleteBanner(1L);
    }

    @Test
    void updateStatus_Success() throws Exception {
        mockMvc.perform(put("/admin/banner/status/1")
                .param("status", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(bannerService).updateStatus(1L, 1);
    }
}
