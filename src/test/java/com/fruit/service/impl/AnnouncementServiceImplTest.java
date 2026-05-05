package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.common.exception.BusinessException;
import com.fruit.common.result.ResultCode;
import com.fruit.entity.Announcement;
import com.fruit.mapper.AnnouncementMapper;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnnouncementServiceImplTest {

    @InjectMocks
    private AnnouncementServiceImpl announcementService;

    @Mock
    private AnnouncementMapper announcementMapper;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(announcementService, "baseMapper", announcementMapper);
    }

    @Test
    @DisplayName("获取公告列表 - 成功")
    void listAnnouncements_Success() {
        Page<Announcement> mockPage = new Page<>();
        Announcement a1 = new Announcement();
        a1.setId(1L);
        mockPage.setRecords(Arrays.asList(a1));

        when(announcementMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        Page<Announcement> result = announcementService.listAnnouncements(1, 10, 1);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        verify(announcementMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("获取公告详情 - 成功")
    void getDetail_Success() {
        Announcement announcement = new Announcement();
        announcement.setId(1L);
        announcement.setStatus(1);

        when(announcementMapper.selectById(1L)).thenReturn(announcement);

        Announcement result = announcementService.getDetail(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("获取公告详情 - 不存在或未发布")
    void getDetail_NotExistOrNotPublished() {
        when(announcementMapper.selectById(1L)).thenReturn(null);

        BusinessException exception1 = assertThrows(BusinessException.class, () -> {
            announcementService.getDetail(1L);
        });
        assertEquals(ResultCode.DATA_NOT_EXIST.getCode(), exception1.getCode());

        Announcement announcement = new Announcement();
        announcement.setId(2L);
        announcement.setStatus(0);
        when(announcementMapper.selectById(2L)).thenReturn(announcement);

        BusinessException exception2 = assertThrows(BusinessException.class, () -> {
            announcementService.getDetail(2L);
        });
        assertEquals(ResultCode.DATA_NOT_EXIST.getCode(), exception2.getCode());
    }

    @Test
    @DisplayName("增加浏览量 - 成功")
    void incrementViewCount_Success() {
        Announcement announcement = new Announcement();
        announcement.setId(1L);
        announcement.setViewCount(10);

        when(announcementMapper.selectById(1L)).thenReturn(announcement);

        announcementService.incrementViewCount(1L);

        ArgumentCaptor<Announcement> captor = ArgumentCaptor.forClass(Announcement.class);
        verify(announcementMapper).updateById(captor.capture());

        assertEquals(11, captor.getValue().getViewCount());
    }

    @Test
    @DisplayName("增加浏览量 - 公告不存在")
    void incrementViewCount_NotExist() {
        when(announcementMapper.selectById(1L)).thenReturn(null);

        announcementService.incrementViewCount(1L);

        verify(announcementMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("管理员获取公告列表 - 成功")
    void listForAdmin_Success() {
        Page<Announcement> mockPage = new Page<>();
        Announcement a1 = new Announcement();
        a1.setId(1L);
        mockPage.setRecords(Arrays.asList(a1));

        when(announcementMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        Page<Announcement> result = announcementService.listForAdmin(1, 10, 1, "keyword");

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        verify(announcementMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("发布公告 - 成功")
    void publish_Success() {
        Announcement announcement = new Announcement();
        announcement.setId(1L);
        announcement.setStatus(0);

        when(announcementMapper.selectById(1L)).thenReturn(announcement);

        announcementService.publish(1L);

        ArgumentCaptor<Announcement> captor = ArgumentCaptor.forClass(Announcement.class);
        verify(announcementMapper).updateById(captor.capture());

        assertEquals(1, captor.getValue().getStatus());
        assertNotNull(captor.getValue().getPublishTime());
    }

    @Test
    @DisplayName("发布公告 - 不存在")
    void publish_NotExist() {
        when(announcementMapper.selectById(1L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            announcementService.publish(1L);
        });
        assertEquals(ResultCode.DATA_NOT_EXIST.getCode(), exception.getCode());
        verify(announcementMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("下架公告 - 成功")
    void unpublish_Success() {
        Announcement announcement = new Announcement();
        announcement.setId(1L);
        announcement.setStatus(1);

        when(announcementMapper.selectById(1L)).thenReturn(announcement);

        announcementService.unpublish(1L);

        ArgumentCaptor<Announcement> captor = ArgumentCaptor.forClass(Announcement.class);
        verify(announcementMapper).updateById(captor.capture());

        assertEquals(2, captor.getValue().getStatus());
    }

    @Test
    @DisplayName("下架公告 - 不存在")
    void unpublish_NotExist() {
        when(announcementMapper.selectById(1L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            announcementService.unpublish(1L);
        });
        assertEquals(ResultCode.DATA_NOT_EXIST.getCode(), exception.getCode());
        verify(announcementMapper, never()).updateById(any());
    }
}
