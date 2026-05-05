package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fruit.entity.MerchantShop;
import com.fruit.entity.Order;
import com.fruit.entity.Product;
import com.fruit.entity.User;
import com.fruit.mapper.MerchantShopMapper;
import com.fruit.mapper.OrderMapper;
import com.fruit.mapper.ProductMapper;
import com.fruit.mapper.UserMapper;
import com.fruit.service.StatsService;
import com.fruit.vo.PeriodStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计服务实现类
 */
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final MerchantShopMapper merchantShopMapper;

    @Override
    public Map<String, Object> getAdminOverview() {
        Map<String, Object> result = new HashMap<>();

        // 用户总数（消费者）
        LambdaQueryWrapper<User> consumerWrapper = new LambdaQueryWrapper<>();
        consumerWrapper.eq(User::getUserType, 0);
        long totalUsers = userMapper.selectCount(consumerWrapper);
        result.put("totalUsers", totalUsers);

        // 商家总数
        LambdaQueryWrapper<User> merchantWrapper = new LambdaQueryWrapper<>();
        merchantWrapper.eq(User::getUserType, 1);
        long totalMerchants = userMapper.selectCount(merchantWrapper);
        result.put("totalMerchants", totalMerchants);

        // 商品总数
        long totalProducts = productMapper.selectCount(null);
        result.put("totalProducts", totalProducts);

        // 订单总数
        long totalOrders = orderMapper.selectCount(null);
        result.put("totalOrders", totalOrders);

        // 总销售额（已完成订单的payAmount之和）
        LambdaQueryWrapper<Order> completedWrapper = new LambdaQueryWrapper<>();
        completedWrapper.eq(Order::getStatus, 3);
        List<Order> completedOrders = orderMapper.selectList(completedWrapper);
        BigDecimal totalSales = completedOrders.stream()
                .map(o -> o.getPayAmount() != null ? o.getPayAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("totalSales", totalSales);

        // 今日起始和结束时间
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        // 今日订单数
        LambdaQueryWrapper<Order> todayOrderWrapper = new LambdaQueryWrapper<>();
        todayOrderWrapper.ge(Order::getCreateTime, todayStart);
        todayOrderWrapper.le(Order::getCreateTime, todayEnd);
        long todayOrders = orderMapper.selectCount(todayOrderWrapper);
        result.put("todayOrders", todayOrders);

        // 今日销售额（今日已付款的订单payAmount之和，即状态>=1的今日订单）
        LambdaQueryWrapper<Order> todaySalesWrapper = new LambdaQueryWrapper<>();
        todaySalesWrapper.ge(Order::getCreateTime, todayStart);
        todaySalesWrapper.le(Order::getCreateTime, todayEnd);
        todaySalesWrapper.ge(Order::getStatus, 1);
        todaySalesWrapper.ne(Order::getStatus, 4); // 排除已取消
        List<Order> todayPaidOrders = orderMapper.selectList(todaySalesWrapper);
        BigDecimal todaySales = todayPaidOrders.stream()
                .map(o -> o.getPayAmount() != null ? o.getPayAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("todaySales", todaySales);

        // 今日新增用户
        LambdaQueryWrapper<User> todayUserWrapper = new LambdaQueryWrapper<>();
        todayUserWrapper.eq(User::getUserType, 0);
        todayUserWrapper.ge(User::getCreateTime, todayStart);
        todayUserWrapper.le(User::getCreateTime, todayEnd);
        long todayNewUsers = userMapper.selectCount(todayUserWrapper);
        result.put("todayNewUsers", todayNewUsers);

        return result;
    }

    @Override
    public Map<String, Object> getAdminGrowth() {
        Map<String, Object> result = new HashMap<>();

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        LocalDateTime yesterdayStart = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime yesterdayEnd = LocalDate.now().minusDays(1).atTime(LocalTime.MAX);

        // 今日订单数
        LambdaQueryWrapper<Order> todayOrderWrapper = new LambdaQueryWrapper<>();
        todayOrderWrapper.ge(Order::getCreateTime, todayStart);
        todayOrderWrapper.le(Order::getCreateTime, todayEnd);
        long todayOrders = orderMapper.selectCount(todayOrderWrapper);

        // 昨日订单数
        LambdaQueryWrapper<Order> yesterdayOrderWrapper = new LambdaQueryWrapper<>();
        yesterdayOrderWrapper.ge(Order::getCreateTime, yesterdayStart);
        yesterdayOrderWrapper.le(Order::getCreateTime, yesterdayEnd);
        long yesterdayOrders = orderMapper.selectCount(yesterdayOrderWrapper);

        // 订单增长率
        double ordersGrowth = calculateGrowthRate(todayOrders, yesterdayOrders);
        result.put("ordersGrowth", ordersGrowth);

        // 今日销售额
        LambdaQueryWrapper<Order> todaySalesWrapper = new LambdaQueryWrapper<>();
        todaySalesWrapper.ge(Order::getCreateTime, todayStart);
        todaySalesWrapper.le(Order::getCreateTime, todayEnd);
        todaySalesWrapper.ge(Order::getStatus, 1);
        todaySalesWrapper.ne(Order::getStatus, 4);
        List<Order> todayPaidOrders = orderMapper.selectList(todaySalesWrapper);
        BigDecimal todaySales = todayPaidOrders.stream()
                .map(o -> o.getPayAmount() != null ? o.getPayAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 昨日销售额
        LambdaQueryWrapper<Order> yesterdaySalesWrapper = new LambdaQueryWrapper<>();
        yesterdaySalesWrapper.ge(Order::getCreateTime, yesterdayStart);
        yesterdaySalesWrapper.le(Order::getCreateTime, yesterdayEnd);
        yesterdaySalesWrapper.ge(Order::getStatus, 1);
        yesterdaySalesWrapper.ne(Order::getStatus, 4);
        List<Order> yesterdayPaidOrders = orderMapper.selectList(yesterdaySalesWrapper);
        BigDecimal yesterdaySales = yesterdayPaidOrders.stream()
                .map(o -> o.getPayAmount() != null ? o.getPayAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 销售额增长率
        double salesGrowth = calculateGrowthRate(todaySales, yesterdaySales);
        result.put("salesGrowth", salesGrowth);

        return result;
    }

    @Override
    public Map<String, Object> getMerchantOverview(Long merchantId) {
        Map<String, Object> result = new HashMap<>();

        // 获取商家的店铺
        LambdaQueryWrapper<MerchantShop> shopWrapper = new LambdaQueryWrapper<>();
        shopWrapper.eq(MerchantShop::getMerchantId, merchantId);
        MerchantShop shop = merchantShopMapper.selectOne(shopWrapper);

        // 在售商品数
        LambdaQueryWrapper<Product> onSaleWrapper = new LambdaQueryWrapper<>();
        onSaleWrapper.eq(Product::getMerchantId, merchantId);
        onSaleWrapper.eq(Product::getStatus, 1);
        long onSaleProducts = productMapper.selectCount(onSaleWrapper);
        result.put("onSaleProducts", onSaleProducts);

        // 商品总数
        LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
        productWrapper.eq(Product::getMerchantId, merchantId);
        long totalProducts = productMapper.selectCount(productWrapper);
        result.put("totalProducts", totalProducts);

        // 待发货订单数
        LambdaQueryWrapper<Order> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(Order::getMerchantId, merchantId);
        pendingWrapper.eq(Order::getStatus, 1);
        long pendingShipment = orderMapper.selectCount(pendingWrapper);
        result.put("pendingShipment", pendingShipment);

        // 订单总数
        LambdaQueryWrapper<Order> totalOrderWrapper = new LambdaQueryWrapper<>();
        totalOrderWrapper.eq(Order::getMerchantId, merchantId);
        long totalOrders = orderMapper.selectCount(totalOrderWrapper);
        result.put("totalOrders", totalOrders);

        // 总销售额
        LambdaQueryWrapper<Order> salesWrapper = new LambdaQueryWrapper<>();
        salesWrapper.eq(Order::getMerchantId, merchantId);
        salesWrapper.eq(Order::getStatus, 3);
        List<Order> completedOrders = orderMapper.selectList(salesWrapper);
        BigDecimal totalSales = completedOrders.stream()
                .map(o -> o.getPayAmount() != null ? o.getPayAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("totalSales", totalSales);

        // 今日数据
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        // 今日订单数
        LambdaQueryWrapper<Order> todayOrderWrapper = new LambdaQueryWrapper<>();
        todayOrderWrapper.eq(Order::getMerchantId, merchantId);
        todayOrderWrapper.ge(Order::getCreateTime, todayStart);
        todayOrderWrapper.le(Order::getCreateTime, todayEnd);
        long todayOrders = orderMapper.selectCount(todayOrderWrapper);
        result.put("todayOrders", todayOrders);

        // 今日销售额
        LambdaQueryWrapper<Order> todaySalesWrapper = new LambdaQueryWrapper<>();
        todaySalesWrapper.eq(Order::getMerchantId, merchantId);
        todaySalesWrapper.ge(Order::getCreateTime, todayStart);
        todaySalesWrapper.le(Order::getCreateTime, todayEnd);
        todaySalesWrapper.ge(Order::getStatus, 1);
        todaySalesWrapper.ne(Order::getStatus, 4);
        List<Order> todayPaidOrders = orderMapper.selectList(todaySalesWrapper);
        BigDecimal todaySales = todayPaidOrders.stream()
                .map(o -> o.getPayAmount() != null ? o.getPayAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("todaySales", todaySales);

        return result;
    }

    @Override
    public PeriodStatsVO getAdminPeriodStats(String period) {
        return buildPeriodStats(period, null);
    }

    @Override
    public PeriodStatsVO getMerchantPeriodStats(Long merchantId, String period) {
        return buildPeriodStats(period, merchantId);
    }

    /**
     * 构建时段统计数据
     * @param period 时段类型
     * @param merchantId 商家ID，null表示全平台
     */
    private PeriodStatsVO buildPeriodStats(String period, Long merchantId) {
        PeriodStatsVO vo = new PeriodStatsVO();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime periodStart;
        LocalDateTime periodEnd = now;
        LocalDateTime prevStart;
        LocalDateTime prevEnd;

        // 趋势数据的时间切片
        List<LocalDateTime[]> slices;
        List<String> labels;

        switch (period) {
            case "today":
                periodStart = LocalDate.now().atStartOfDay();
                prevStart = LocalDate.now().minusDays(1).atStartOfDay();
                prevEnd = LocalDate.now().minusDays(1).atTime(LocalTime.MAX);
                // 按2小时间隔，12个数据点
                slices = new ArrayList<>();
                labels = new ArrayList<>();
                for (int i = 0; i < 12; i++) {
                    LocalDateTime sliceStart = periodStart.plusHours(i * 2);
                    LocalDateTime sliceEnd = periodStart.plusHours(i * 2 + 2).minusSeconds(1);
                    slices.add(new LocalDateTime[]{sliceStart, sliceEnd});
                    labels.add(String.format("%02d:00", i * 2));
                }
                break;
            case "week":
                periodStart = LocalDate.now().minusDays(6).atStartOfDay();
                prevStart = LocalDate.now().minusDays(13).atStartOfDay();
                prevEnd = LocalDate.now().minusDays(7).atTime(LocalTime.MAX);
                // 按天，7个数据点
                slices = new ArrayList<>();
                labels = new ArrayList<>();
                DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("MM/dd");
                for (int i = 0; i < 7; i++) {
                    LocalDate day = LocalDate.now().minusDays(6 - i);
                    slices.add(new LocalDateTime[]{day.atStartOfDay(), day.atTime(LocalTime.MAX)});
                    labels.add(day.format(dayFmt));
                }
                break;
            case "month":
                periodStart = LocalDate.now().minusDays(29).atStartOfDay();
                prevStart = LocalDate.now().minusDays(59).atStartOfDay();
                prevEnd = LocalDate.now().minusDays(30).atTime(LocalTime.MAX);
                // 按天，30个数据点
                slices = new ArrayList<>();
                labels = new ArrayList<>();
                DateTimeFormatter monthDayFmt = DateTimeFormatter.ofPattern("MM/dd");
                for (int i = 0; i < 30; i++) {
                    LocalDate day = LocalDate.now().minusDays(29 - i);
                    slices.add(new LocalDateTime[]{day.atStartOfDay(), day.atTime(LocalTime.MAX)});
                    labels.add(day.format(monthDayFmt));
                }
                break;
            case "quarter":
                periodStart = LocalDate.now().minusMonths(3).withDayOfMonth(1).atStartOfDay();
                prevStart = LocalDate.now().minusMonths(6).withDayOfMonth(1).atStartOfDay();
                prevEnd = LocalDate.now().minusMonths(3).withDayOfMonth(1).minusDays(1).atTime(LocalTime.MAX);
                // 按月，3个数据点
                slices = new ArrayList<>();
                labels = new ArrayList<>();
                for (int i = 2; i >= 0; i--) {
                    LocalDate monthStart = LocalDate.now().minusMonths(i).withDayOfMonth(1);
                    LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
                    slices.add(new LocalDateTime[]{monthStart.atStartOfDay(), monthEnd.atTime(LocalTime.MAX)});
                    labels.add(monthStart.getMonthValue() + "\u6708");
                }
                break;
            case "year":
                periodStart = LocalDate.now().withMonth(1).withDayOfMonth(1).atStartOfDay();
                prevStart = LocalDate.now().minusYears(1).withMonth(1).withDayOfMonth(1).atStartOfDay();
                prevEnd = LocalDate.now().minusYears(1).withMonth(12).withDayOfMonth(31).atTime(LocalTime.MAX);
                // 按月，12个数据点
                slices = new ArrayList<>();
                labels = new ArrayList<>();
                for (int i = 1; i <= 12; i++) {
                    LocalDate monthStart = LocalDate.now().withMonth(i).withDayOfMonth(1);
                    LocalDate monthEnd;
                    if (i == 12) {
                        monthEnd = LocalDate.now().withMonth(12).withDayOfMonth(31);
                    } else {
                        monthEnd = LocalDate.now().withMonth(i + 1).withDayOfMonth(1).minusDays(1);
                    }
                    slices.add(new LocalDateTime[]{monthStart.atStartOfDay(), monthEnd.atTime(LocalTime.MAX)});
                    labels.add(i + "\u6708");
                }
                break;
            default:
                // 默认当天
                periodStart = LocalDate.now().atStartOfDay();
                prevStart = LocalDate.now().minusDays(1).atStartOfDay();
                prevEnd = LocalDate.now().minusDays(1).atTime(LocalTime.MAX);
                slices = new ArrayList<>();
                labels = new ArrayList<>();
                for (int i = 0; i < 12; i++) {
                    LocalDateTime sliceStart2 = periodStart.plusHours(i * 2);
                    LocalDateTime sliceEnd2 = periodStart.plusHours(i * 2 + 2).minusSeconds(1);
                    slices.add(new LocalDateTime[]{sliceStart2, sliceEnd2});
                    labels.add(String.format("%02d:00", i * 2));
                }
                break;
        }

        // 查询当前时段的订单（已付款且未取消）
        List<Order> periodOrders = queryPaidOrders(periodStart, periodEnd, merchantId);
        BigDecimal periodSales = sumPayAmount(periodOrders);
        vo.setPeriodSales(periodSales);
        vo.setPeriodOrders((long) periodOrders.size());

        // 查询上一时段的订单
        List<Order> prevOrders = queryPaidOrders(prevStart, prevEnd, merchantId);
        BigDecimal prevSales = sumPayAmount(prevOrders);

        // 计算增长率
        double growthRate = calculateGrowthRate(periodSales, prevSales);
        vo.setGrowthRate(growthRate);

        // 构建趋势数据
        vo.setTrendLabels(labels);
        List<BigDecimal> trendSales = new ArrayList<>();
        List<Long> trendOrders = new ArrayList<>();
        for (LocalDateTime[] slice : slices) {
            List<Order> sliceOrders = queryPaidOrders(slice[0], slice[1], merchantId);
            trendSales.add(sumPayAmount(sliceOrders));
            trendOrders.add((long) sliceOrders.size());
        }
        vo.setTrendSales(trendSales);
        vo.setTrendOrders(trendOrders);

        return vo;
    }

    /**
     * 查询指定时间范围内的已付款订单
     */
    private List<Order> queryPaidOrders(LocalDateTime start, LocalDateTime end, Long merchantId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (merchantId != null) {
            wrapper.eq(Order::getMerchantId, merchantId);
        }
        wrapper.ge(Order::getCreateTime, start);
        wrapper.le(Order::getCreateTime, end);
        wrapper.ge(Order::getStatus, 1);
        wrapper.ne(Order::getStatus, 4);
        return orderMapper.selectList(wrapper);
    }

    /**
     * 计算订单列表的payAmount之和
     */
    private BigDecimal sumPayAmount(List<Order> orders) {
        return orders.stream()
                .map(o -> o.getPayAmount() != null ? o.getPayAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 计算增长率（百分比，保留1位小数）
     */
    private double calculateGrowthRate(long current, long previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return BigDecimal.valueOf(current - previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(previous), 1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * 计算增长率（BigDecimal版本）
     */
    private double calculateGrowthRate(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        return current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    @Override
    public List<Map<String, Object>> getOrderStatusDistribution() {
        List<Map<String, Object>> result = new ArrayList<>();

        // 状态映射：0-待付款 1-待发货 2-待收货 3-已完成 4-已取消 5-申请退款 6-已退款
        String[] statusNames = {"待付款", "待发货", "待收货", "已完成", "已取消", "申请退款", "已退款"};
        String[] statusColors = {"#E6A23C", "#409EFF", "#67C23A", "#228B22", "#909399", "#F56C6C", "#909399"};

        for (int i = 0; i <= 6; i++) {
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Order::getStatus, i);
            long count = orderMapper.selectCount(wrapper);

            // 只返回有订单的状态
            if (count > 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("value", count);
                item.put("name", statusNames[i]);
                item.put("status", i);
                Map<String, String> itemStyle = new HashMap<>();
                itemStyle.put("color", statusColors[i]);
                item.put("itemStyle", itemStyle);
                result.add(item);
            }
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getUserGrowthTrend(String period) {
        List<Map<String, Object>> result = new ArrayList<>();

        int days = "month".equals(period) ? 30 : 7;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(LocalTime.MAX);

            // 统计当天新增用户数
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUserType, 0); // 只统计消费者
            wrapper.ge(User::getCreateTime, dayStart);
            wrapper.le(User::getCreateTime, dayEnd);
            long count = userMapper.selectCount(wrapper);

            Map<String, Object> item = new HashMap<>();
            item.put("date", date.format(formatter));
            item.put("count", count);
            result.add(item);
        }

        return result;
    }
}
