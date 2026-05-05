package com.fruit.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fruit.entity.Order;
import com.fruit.entity.Product;
import com.fruit.mapper.OrderMapper;
import com.fruit.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统定时任务
 * 1. 过期商品自动下架
 * 2. 发货7天后自动确认收货
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SystemScheduledTask {

    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;

    /**
     * 过期商品自动下架
     * 每小时执行一次（整点执行）
     *
     * 过期判断：productionDate + shelfLifeDays < 当前日期
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void autoOffShelfExpiredProducts() {
        log.info("开始执行过期商品自动下架任务...");

        try {
            LocalDate today = LocalDate.now();

            // 查询上架中且有生产日期和保质期的商品
            LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Product::getStatus, 1) // status=1 表示上架中
                    .isNotNull(Product::getProductionDate)
                    .isNotNull(Product::getShelfLifeDays);

            List<Product> products = productMapper.selectList(wrapper);

            int offShelfCount = 0;
            for (Product product : products) {
                // 计算过期日期
                LocalDate expirationDate = product.getProductionDate().plusDays(product.getShelfLifeDays());

                if (expirationDate.isBefore(today)) {
                    // 已过期，下架
                    product.setStatus(0); // status=0 表示下架
                    productMapper.updateById(product);
                    offShelfCount++;
                    log.info("已下架过期商品: id={}, name={}, productionDate={}, shelfLifeDays={}, expirationDate={}",
                            product.getId(), product.getName(), product.getProductionDate(),
                            product.getShelfLifeDays(), expirationDate);
                }
            }

            if (offShelfCount == 0) {
                log.info("没有需要下架的过期商品");
            } else {
                log.info("过期商品自动下架任务完成，共下架 {} 件商品", offShelfCount);
            }
        } catch (Exception e) {
            log.error("过期商品自动下架任务执行失败", e);
            throw e;
        }
    }

    /**
     * 发货7天后自动确认收货
     * 每天凌晨2点执行
     *
     * 订单状态：0-待付款 1-待发货 2-待收货 3-已完成 4-已取消
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void autoConfirmReceipt() {
        log.info("开始执行自动确认收货任务...");

        try {
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

            // 查询发货超过7天且状态为"待收货"的订单（status = 2 表示待收货）
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Order::getStatus, 2) // status=2 表示待收货
                    .isNotNull(Order::getDeliveryTime)
                    .lt(Order::getDeliveryTime, sevenDaysAgo);

            List<Order> pendingOrders = orderMapper.selectList(wrapper);

            if (pendingOrders.isEmpty()) {
                log.info("没有需要自动确认收货的订单");
                return;
            }

            LocalDateTime now = LocalDateTime.now();

            // 批量更新为已完成
            for (Order order : pendingOrders) {
                order.setStatus(3); // status=3 表示已完成
                order.setReceiveTime(now);
                orderMapper.updateById(order);
                log.info("已自动确认收货: orderNo={}, deliveryTime={}",
                        order.getOrderNo(), order.getDeliveryTime());
            }

            log.info("自动确认收货任务完成，共处理 {} 个订单", pendingOrders.size());
        } catch (Exception e) {
            log.error("自动确认收货任务执行失败", e);
            throw e;
        }
    }
}
