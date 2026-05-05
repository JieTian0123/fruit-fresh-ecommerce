package com.fruit.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fruit.common.exception.GlobalExceptionHandler;
import com.fruit.entity.User;
import com.fruit.enums.StatusEnum;
import com.fruit.service.UserService;
import com.fruit.utils.UserContext;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserManageControllerTest {

    @InjectMocks
    private UserManageController controller;

    @Mock
    private UserService userService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    static void beforeAll() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new Configuration(), ""), User.class);
    }

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
        Page<User> page = new Page<>(1, 10);
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        page.setRecords(Collections.singletonList(user));
        page.setTotal(1L);

        when(userService.page(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        mockMvc.perform(get("/admin/user/list")
                .param("userType", "0")
                .param("status", "1")
                .param("pageNum", "1")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].id").value(1))
                .andExpect(jsonPath("$.data.list[0].username").value("testuser"));

        verify(userService).page(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void list_DefaultsToConsumerUsers() throws Exception {
        Page<User> page = new Page<>(1, 10);
        page.setRecords(Collections.emptyList());
        page.setTotal(0L);
        when(userService.page(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        mockMvc.perform(get("/admin/user/list")
                .param("pageNum", "1")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        ArgumentCaptor<LambdaQueryWrapper<User>> wrapperCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(userService).page(any(Page.class), wrapperCaptor.capture());
        String sqlSegment = wrapperCaptor.getValue().getSqlSegment();
        assertTrue(sqlSegment.contains("user_type") || sqlSegment.contains("userType"));
    }

    @Test
    void detail_Success() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        when(userService.getById(1L)).thenReturn(user);

        mockMvc.perform(get("/admin/user/detail/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("testuser"));

        verify(userService).getById(1L);
    }

    @Test
    void approve_Success() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setStatus(StatusEnum.PENDING.getCode());

        when(userService.getById(1L)).thenReturn(user);

        mockMvc.perform(put("/admin/user/approve/1")
                .param("status", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService).getById(1L);
        verify(userService).updateById(any(User.class));
    }

    @Test
    void disable_Success() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setStatus(StatusEnum.ENABLED.getCode());

        when(userService.getById(1L)).thenReturn(user);

        mockMvc.perform(put("/admin/user/disable/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService).getById(1L);
        verify(userService).updateById(any(User.class));
    }

    @Test
    void enable_Success() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setStatus(StatusEnum.DISABLED.getCode());

        when(userService.getById(1L)).thenReturn(user);

        mockMvc.perform(put("/admin/user/enable/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService).getById(1L);
        verify(userService).updateById(any(User.class));
    }
}
