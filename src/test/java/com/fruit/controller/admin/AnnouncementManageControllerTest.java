package com.fruit.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fruit.common.exception.GlobalExceptionHandler;
import com.fruit.entity.Announcement;
import com.fruit.service.AnnouncementService;
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
class AnnouncementManageControllerTest {

    @InjectMocks
    private AnnouncementManageController controller;

    @Mock
    private AnnouncementService announcementService;

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
        Page<Announcement> page = new Page<>(1, 10);
        Announcement announcement = new Announcement();
        announcement.setId(1L);
        announcement.setTitle("Test Announcement");
        page.setRecords(Collections.singletonList(announcement));
        page.setTotal(1);

        when(announcementService.listForAdmin(1, 10, 1, "Test")).thenReturn(page);

        mockMvc.perform(get("/admin/announcement/list")
                .param("pageNum", "1")
                .param("pageSize", "10")
                .param("status", "1")
                .param("keyword", "Test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(1))
                .andExpect(jsonPath("$.data.records[0].title").value("Test Announcement"));

        verify(announcementService).listForAdmin(1, 10, 1, "Test");
    }

    @Test
    void detail_Success() throws Exception {
        Announcement announcement = new Announcement();
        announcement.setId(1L);
        announcement.setTitle("Test Announcement");

        when(announcementService.getById(1L)).thenReturn(announcement);

        mockMvc.perform(get("/admin/announcement/detail/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Test Announcement"));

        verify(announcementService).getById(1L);
    }

    @Test
    void add_Success() throws Exception {
        Announcement announcement = new Announcement();
        announcement.setTitle("New Announcement");

        mockMvc.perform(post("/admin/announcement/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(announcement)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(announcementService).save(any(Announcement.class));
    }

    @Test
    void update_Success() throws Exception {
        Announcement announcement = new Announcement();
        announcement.setId(1L);
        announcement.setTitle("Updated Announcement");

        mockMvc.perform(put("/admin/announcement/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(announcement)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(announcementService).updateById(any(Announcement.class));
    }

    @Test
    void publish_Success() throws Exception {
        mockMvc.perform(post("/admin/announcement/publish/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(announcementService).publish(1L);
    }

    @Test
    void unpublish_Success() throws Exception {
        mockMvc.perform(post("/admin/announcement/unpublish/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(announcementService).unpublish(1L);
    }

    @Test
    void delete_Success() throws Exception {
        mockMvc.perform(delete("/admin/announcement/delete/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(announcementService).removeById(1L);
    }
}
