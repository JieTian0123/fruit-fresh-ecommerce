package com.fruit.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fruit.common.exception.GlobalExceptionHandler;
import com.fruit.dto.HomeActivityDTO;
import com.fruit.service.BannerService;
import com.fruit.vo.HomeActivityVO;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HomeActivityManageControllerTest {

    @InjectMocks
    private HomeActivityManageController controller;

    @Mock
    private BannerService bannerService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void list_Success() throws Exception {
        HomeActivityVO activity = new HomeActivityVO();
        activity.setCode("DAILY_NEW");
        activity.setTitle("新鲜到店");
        when(bannerService.getHomeActivitiesForAdmin()).thenReturn(Collections.singletonList(activity));

        mockMvc.perform(get("/admin/activity/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].code").value("DAILY_NEW"));

        verify(bannerService).getHomeActivitiesForAdmin();
    }

    @Test
    void update_Success() throws Exception {
        HomeActivityDTO dto = new HomeActivityDTO();
        dto.setImageUrl("/uploads/images/04c8c5a01c1d4dafafc0acb32822dfcf.jpg");
        dto.setLinkUrl("/category?tag=new");
        dto.setStatus(1);

        mockMvc.perform(put("/admin/activity/DAILY_NEW")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(bannerService).updateHomeActivity(eq("DAILY_NEW"), any(HomeActivityDTO.class));
    }
}
