package com.fruit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fruit.entity.Announcement;

/**
 * 公告服务接口
 */
public interface AnnouncementService extends IService<Announcement> {

    /**
     * 分页查询公告列表（消费者端）
     */
    Page<Announcement> listAnnouncements(Integer pageNum, Integer pageSize, Integer type);

    /**
     * 获取公告详情
     */
    Announcement getDetail(Long id);

    /**
     * 增加浏览次数
     */
    void incrementViewCount(Long id);

    /**
     * 分页查询公告列表（管理员端）
     */
    Page<Announcement> listForAdmin(Integer pageNum, Integer pageSize, Integer status, String keyword);

    /**
     * 发布公告
     */
    void publish(Long id);

    /**
     * 下架公告
     */
    void unpublish(Long id);
}
