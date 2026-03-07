package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruit.common.exception.BusinessException;
import com.fruit.common.result.PageResult;
import com.fruit.common.result.ResultCode;
import com.fruit.entity.SystemNotification;
import com.fruit.mapper.SystemNotificationMapper;
import com.fruit.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 系统通知服务实现类
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<SystemNotificationMapper, SystemNotification> implements NotificationService {

    @Override
    public PageResult<SystemNotification> getNotifications(Long userId, String type, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<SystemNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemNotification::getUserId, userId);
        if (StringUtils.hasText(type)) {
            wrapper.eq(SystemNotification::getType, type);
        }
        wrapper.orderByDesc(SystemNotification::getCreateTime);

        Page<SystemNotification> page = new Page<>(pageNum, pageSize);
        Page<SystemNotification> result = baseMapper.selectPage(page, wrapper);

        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long userId, Long notificationId) {
        SystemNotification notification = baseMapper.selectById(notificationId);
        if (notification == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        notification.setIsRead(1);
        baseMapper.updateById(notification);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId) {
        LambdaUpdateWrapper<SystemNotification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SystemNotification::getUserId, userId)
                .eq(SystemNotification::getIsRead, 0)
                .set(SystemNotification::getIsRead, 1);
        baseMapper.update(null, wrapper);
    }

    @Override
    public Integer getUnreadCount(Long userId) {
        LambdaQueryWrapper<SystemNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemNotification::getUserId, userId)
                .eq(SystemNotification::getIsRead, 0);
        return Math.toIntExact(baseMapper.selectCount(wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createNotification(Long userId, String type, String title, String content, Long relatedId) {
        SystemNotification notification = new SystemNotification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notification.setIsRead(0);
        baseMapper.insert(notification);
    }
}
