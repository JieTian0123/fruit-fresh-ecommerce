package com.fruit.service;

import com.fruit.common.result.PageResult;
import com.fruit.entity.SystemNotification;

/**
 * 系统通知服务接口
 */
public interface NotificationService {

    /**
     * 获取用户通知列表(分页)
     */
    PageResult<SystemNotification> getNotifications(Long userId, String type, Integer pageNum, Integer pageSize);

    /**
     * 标记通知为已读
     */
    void markAsRead(Long userId, Long notificationId);

    /**
     * 标记所有通知为已读
     */
    void markAllAsRead(Long userId);

    /**
     * 获取未读通知数量
     */
    Integer getUnreadCount(Long userId);

    /**
     * 创建通知
     */
    void createNotification(Long userId, String type, String title, String content, Long relatedId);
}
