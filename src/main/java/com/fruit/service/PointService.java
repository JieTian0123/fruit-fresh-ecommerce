package com.fruit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruit.common.result.PageResult;
import com.fruit.entity.UserPointsLog;

/**
 * 积分服务接口
 */
public interface PointService extends IService<UserPointsLog> {

    /**
     * 添加积分(签到、消费、评价等)
     */
    void addPoints(Long userId, Integer points, Integer sourceType, String sourceId, String description);

    /**
     * 扣减积分(兑换)
     */
    void deductPoints(Long userId, Integer points, Integer sourceType, String sourceId, String description);

    /**
     * 获取积分记录
     */
    PageResult<UserPointsLog> getPointsLog(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取用户当前积分
     */
    Integer getUserPoints(Long userId);
}
