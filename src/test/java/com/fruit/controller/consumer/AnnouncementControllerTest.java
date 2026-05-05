package com.fruit.controller.consumer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fruit.common.exception.GlobalExceptionHandler;
import com.fruit.entity.Announcement;
import com.fruit.entity.User;
import com.fruit.service.AnnouncementService;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AnnouncementControllerTest {

    @InjectMocks
    private AnnouncementController announcementController;

    @Mock
    private AnnouncementService announcementService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(announcementController)
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
        Page<Announcement> page = new Page<>();
        page.setRecords(Arrays.asList(new Announcement()));
        page.setTotal(1L);

        when(announcementService.listAnnouncements(1, 10, 1)).thenReturn(page);

        mockMvc.perform(get("/announcement/list")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("type", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1));

        verify(announcementService).listAnnouncements(1, 10, 1);
    }

    @Test
    void detail() throws Exception {
        Announcement announcement = new Announcement();
        announcement.setId(1L);
        when(announcementService.getDetail(1L)).thenReturn(announcement);

        mockMvc.perform(get("/announcement/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(announcementService).getDetail(1L);
        verify(announcementService).incrementViewCount(1L);
    }
}