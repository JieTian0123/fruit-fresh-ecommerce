package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruit.common.exception.BusinessException;
import com.fruit.common.result.ResultCode;
import com.fruit.entity.Announcement;
import com.fruit.mapper.AnnouncementMapper;
import com.fruit.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 公告服务实现
 */
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {

    @Override
    public Page<Announcement> listAnnouncements(Integer pageNum, Integer pageSize, Integer type) {
        Page<Announcement> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询已发布的公告
        wrapper.eq(Announcement::getStatus, 1);
        
        if (type != null) {
            wrapper.eq(Announcement::getType, type);
        }
        
        wrapper.orderByDesc(Announcement::getPublishTime)
                .orderByDesc(Announcement::getCreateTime);
        
        return page(page, wrapper);
    }

    @Override
    public Announcement getDetail(Long id) {
        Announcement announcement = getById(id);
        if (announcement == null || announcement.getStatus() != 1) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        return announcement;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementViewCount(Long id) {
        Announcement announcement = getById(id);
        if (announcement != null) {
            announcement.setViewCount(announcement.getViewCount() + 1);
            updateById(announcement);
        }
    }

    @Override
    public Page<Announcement> listForAdmin(Integer pageNum, Integer pageSize, Integer status, String keyword) {
        Page<Announcement> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null) {
            wrapper.eq(Announcement::getStatus, status);
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Announcement::getTitle, keyword);
        }
        
        wrapper.orderByDesc(Announcement::getCreateTime);
        
        return page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(Long id) {
        Announcement announcement = getById(id);
        if (announcement == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        
        announcement.setStatus(1);
        announcement.setPublishTime(LocalDateTime.now());
        updateById(announcement);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unpublish(Long id) {
        Announcement announcement = getById(id);
        if (announcement == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        
        announcement.setStatus(2);
        updateById(announcement);
    }
}
