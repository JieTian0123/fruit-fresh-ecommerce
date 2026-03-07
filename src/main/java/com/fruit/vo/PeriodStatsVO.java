package com.fruit.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 时段统计VO
 */
@Data
public class PeriodStatsVO {

    /**
     * 时段总销售额
     */
    private BigDecimal periodSales;

    /**
     * 时段总订单数
     */
    private Long periodOrders;

    /**
     * 趋势标签（如：["周一","周二",...]）
     */
    private List<String> trendLabels;

    /**
     * 趋势销售额
     */
    private List<BigDecimal> trendSales;

    /**
     * 趋势订单数
     */
    private List<Long> trendOrders;

    /**
     * 环比增长率（百分比）
     */
    private Double growthRate;
}
