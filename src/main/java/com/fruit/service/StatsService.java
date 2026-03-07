package com.fruit.service;

import com.fruit.vo.PeriodStatsVO;

import java.util.Map;

/**
 * 统计服务接口
 */
public interface StatsService {

    /**
     * 管理员概览统计
     */
    Map<String, Object> getAdminOverview();

    /**
     * 管理员增长率统计（较昨日）
     */
    Map<String, Object> getAdminGrowth();

    /**
     * 商家概览统计
     */
    Map<String, Object> getMerchantOverview(Long merchantId);

    /**
     * 管理员按时段统计
     * @param period 时段：today, week, month, quarter, year
     */
    PeriodStatsVO getAdminPeriodStats(String period);

    /**
     * 商家按时段统计
     * @param merchantId 商家ID
     * @param period 时段：today, week, month, quarter, year
     */
    PeriodStatsVO getMerchantPeriodStats(Long merchantId, String period);
}
