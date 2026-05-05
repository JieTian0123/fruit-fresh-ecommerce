package com.fruit.service;

import com.fruit.vo.PeriodStatsVO;

import java.util.List;
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

    /**
     * 获取订单状态分布
     * @return 各状态的订单数量
     */
    List<Map<String, Object>> getOrderStatusDistribution();

    /**
     * 获取用户增长趋势
     * @param period 时段：week, month
     * @return 每日/每周新增用户数
     */
    List<Map<String, Object>> getUserGrowthTrend(String period);
}
