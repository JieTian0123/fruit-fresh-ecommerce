package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.common.exception.BusinessException;
import com.fruit.common.result.PageResult;
import com.fruit.common.result.ResultCode;
import com.fruit.entity.SystemNotification;
import com.fruit.mapper.SystemNotificationMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Mock
    private SystemNotificationMapper notificationMapper;

    @BeforeAll
    static void init() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SystemNotification.class);
    }

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(notificationService, "baseMapper", notificationMapper);
    }

    @Test
    @DisplayName("获取通知列表 - 成功")
    void getNotifications_Success() {
        Page<SystemNotification> mockPage = new Page<>(1, 10);
        mockPage.setTotal(1);
        SystemNotification n1 = new SystemNotification();
        n1.setId(1L);
        mockPage.setRecords(Arrays.asList(n1));

        when(notificationMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        PageResult<SystemNotification> result = notificationService.getNotifications(1L, "system", 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
        verify(notificationMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("标记为已读 - 成功")
    void markAsRead_Success() {
        SystemNotification notification = new SystemNotification();
        notification.setId(1L);
        notification.setUserId(1L);
        notification.setIsRead(0);

        when(notificationMapper.selectById(1L)).thenReturn(notification);

        notificationService.markAsRead(1L, 1L);

        ArgumentCaptor<SystemNotification> captor = ArgumentCaptor.forClass(SystemNotification.class);
        verify(notificationMapper).updateById(captor.capture());

        assertEquals(1, captor.getValue().getIsRead());
    }

    @Test
    @DisplayName("标记为已读 - 通知不存在")
    void markAsRead_NotExist() {
        when(notificationMapper.selectById(1L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            notificationService.markAsRead(1L, 1L);
        });
        assertEquals(ResultCode.DATA_NOT_EXIST.getCode(), exception.getCode());
        verify(notificationMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("标记为已读 - 不属于该用户")
    void markAsRead_Forbidden() {
        SystemNotification notification = new SystemNotification();
        notification.setId(1L);
        notification.setUserId(2L);

        when(notificationMapper.selectById(1L)).thenReturn(notification);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            notificationService.markAsRead(1L, 1L);
        });
        assertEquals(ResultCode.FORBIDDEN.getCode(), exception.getCode());
        verify(notificationMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("全部标记为已读 - 成功")
    void markAllAsRead_Success() {
        notificationService.markAllAsRead(1L);

        verify(notificationMapper).update(eq(null), any(LambdaUpdateWrapper.class));
    }

    @Test
    @DisplayName("获取未读数量 - 成功")
    void getUnreadCount_Success() {
        when(notificationMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

        Integer count = notificationService.getUnreadCount(1L);

        assertEquals(5, count);
        verify(notificationMapper).selectCount(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("创建通知 - 成功")
    void createNotification_Success() {
        notificationService.createNotification(1L, "system", "Title", "Content", 100L);

        ArgumentCaptor<SystemNotification> captor = ArgumentCaptor.forClass(SystemNotification.class);
        verify(notificationMapper).insert(captor.capture());

        SystemNotification saved = captor.getValue();
        assertEquals(1L, saved.getUserId());
        assertEquals("system", saved.getType());
        assertEquals("Title", saved.getTitle());
        assertEquals("Content", saved.getContent());
        assertEquals(100L, saved.getRelatedId());
        assertEquals(0, saved.getIsRead());
    }
}
