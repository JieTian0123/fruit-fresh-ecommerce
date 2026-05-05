package com.fruit.controller.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fruit.common.exception.GlobalExceptionHandler;
import com.fruit.common.result.PageResult;
import com.fruit.entity.SystemNotification;
import com.fruit.entity.User;
import com.fruit.service.NotificationService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @InjectMocks
    private NotificationController notificationController;

    @Mock
    private NotificationService notificationService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController)
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
    void getNotifications() throws Exception {
        PageResult<SystemNotification> pageResult = new PageResult<>();
        pageResult.setList(Arrays.asList(new SystemNotification()));
        pageResult.setTotal(1L);

        when(notificationService.getNotifications(1L, "system", 1, 10)).thenReturn(pageResult);

        mockMvc.perform(get("/consumer/notification/list")
                        .param("type", "system")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1));

        verify(notificationService).getNotifications(1L, "system", 1, 10);
    }

    @Test
    void markAsRead() throws Exception {
        mockMvc.perform(put("/consumer/notification/read/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(notificationService).markAsRead(1L, 1L);
    }

    @Test
    void markAllAsRead() throws Exception {
        mockMvc.perform(put("/consumer/notification/read-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(notificationService).markAllAsRead(1L);
    }

    @Test
    void getUnreadCount() throws Exception {
        when(notificationService.getUnreadCount(1L)).thenReturn(5);

        mockMvc.perform(get("/consumer/notification/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(5));

        verify(notificationService).getUnreadCount(1L);
    }
}