package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruit.common.exception.BusinessException;
import com.fruit.common.result.PageResult;
import com.fruit.common.result.ResultCode;
import com.fruit.dto.DeliverDTO;
import com.fruit.dto.OrderCreateDTO;
import com.fruit.entity.*;
import com.fruit.enums.OrderStatusEnum;
import com.fruit.mapper.*;
import com.fruit.service.OrderService;
import com.fruit.service.PointService;
import com.fruit.service.NotificationService;
import com.fruit.service.VipService;
import com.fruit.utils.OrderNoUtils;
import com.fruit.vo.OrderDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final OrderItemMapper orderItemMapper;
    private final CartMapper cartMapper;
    private final ProductMapper productMapper;
    private final AddressMapper addressMapper;
    private final UserCouponMapper userCouponMapper;
    private final CouponMapper couponMapper;
    private final PointService pointService;
    private final NotificationService notificationService;
    private final VipService vipService;
    private final UserMapper userMapper;
    private final MerchantShopMapper merchantShopMapper;

    @Override
    public PageResult<Order> listForConsumer(Long userId, Integer status, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);

        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }

        wrapper.orderByDesc(Order::getCreateTime);

        Page<Order> page = new Page<>(pageNum, pageSize);
        Page<Order> result = baseMapper.selectPage(page, wrapper);

        fillOrderItems(result.getRecords());

        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    public PageResult<Order> listForMerchant(Long merchantId, Integer status, Integer pageNum, Integer pageSize) {
        return listForMerchant(merchantId, status, null, pageNum, pageSize);
    }

    @Override
    public PageResult<Order> listForMerchant(Long merchantId, Integer status, String orderNo, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getMerchantId, merchantId);

        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }

        if (StringUtils.isNotBlank(orderNo)) {
            wrapper.like(Order::getOrderNo, orderNo);
        }

        wrapper.orderByDesc(Order::getCreateTime);

        Page<Order> page = new Page<>(pageNum, pageSize);
        Page<Order> result = baseMapper.selectPage(page, wrapper);

        fillOrderItems(result.getRecords());

        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    public PageResult<Order> listForAdmin(Integer status, String orderNo, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }

        if (StringUtils.isNotBlank(orderNo)) {
            wrapper.like(Order::getOrderNo, orderNo);
        }

        wrapper.orderByDesc(Order::getCreateTime);

        Page<Order> page = new Page<>(pageNum, pageSize);
        Page<Order> result = baseMapper.selectPage(page, wrapper);

        fillOrderItems(result.getRecords());

        // 填充商家名称
        if (!result.getRecords().isEmpty()) {
            // 获取所有商家ID
            List<Long> merchantIds = result.getRecords().stream()
                    .map(Order::getMerchantId)
                    .filter(id -> id != null)
                    .distinct()
                    .collect(Collectors.toList());

            // 查询商家店铺名称
            Map<Long, String> merchantNameMap = new HashMap<>();
            if (!merchantIds.isEmpty()) {
                LambdaQueryWrapper<MerchantShop> shopWrapper = new LambdaQueryWrapper<>();
                shopWrapper.in(MerchantShop::getMerchantId, merchantIds);
                List<MerchantShop> shops = merchantShopMapper.selectList(shopWrapper);
                merchantNameMap = shops.stream()
                        .collect(Collectors.toMap(MerchantShop::getMerchantId, MerchantShop::getShopName, (a, b) -> a));
            }

            Map<Long, String> finalMerchantNameMap = merchantNameMap;
            result.getRecords().forEach(order -> {
                if (order.getMerchantId() != null) {
                    order.setMerchantName(finalMerchantNameMap.get(order.getMerchantId()));
                }
            });
        }

        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    public OrderDetailVO getOrderDetail(String orderNo, Long userId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo);
        if (userId != null) {
            wrapper.eq(Order::getUserId, userId);
        }

        Order order = baseMapper.selectOne(wrapper);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }

        // 查询订单明细
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderNo, orderNo);
        List<OrderItem> orderItems = orderItemMapper.selectList(itemWrapper);

        OrderDetailVO vo = new OrderDetailVO();
        BeanUtils.copyProperties(order, vo);
        vo.setOrderItems(orderItems);
        vo.setStatusName(OrderStatusEnum.getByCode(order.getStatus()).getDesc());

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(Long userId, OrderCreateDTO dto) {
        // 获取地址
        Address address = addressMapper.selectById(dto.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ADDRESS_NOT_EXIST);
        }

        String orderNo = OrderNoUtils.generateOrderNo();
        BigDecimal totalAmount = BigDecimal.ZERO;
        Long merchantId = null;

        // 判断是立即购买模式还是购物车模式
        if (dto.getProductId() != null && dto.getQuantity() != null && dto.getQuantity() > 0) {
            // 立即购买模式：直接使用 productId 和 quantity
            Product product = productMapper.selectById(dto.getProductId());
            if (product == null) {
                throw new BusinessException(ResultCode.PRODUCT_NOT_EXIST);
            }

            if (product.getStock() < dto.getQuantity()) {
                throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
            }

            merchantId = product.getMerchantId();

            // 创建订单明细
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderNo(orderNo);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setPrice(product.getPrice());
            orderItem.setQuantity(dto.getQuantity());
            orderItem.setTotalAmount(product.getPrice().multiply(new BigDecimal(dto.getQuantity())));
            orderItemMapper.insert(orderItem);

            totalAmount = orderItem.getTotalAmount();

            // 扣减库存
            product.setStock(product.getStock() - dto.getQuantity());
            product.setSales(product.getSales() + dto.getQuantity());
            productMapper.updateById(product);
        } else {
            // 购物车模式：获取选中的购物车商品
            LambdaQueryWrapper<Cart> cartWrapper = new LambdaQueryWrapper<>();
            cartWrapper.eq(Cart::getUserId, userId);
            cartWrapper.eq(Cart::getSelected, 1);
            List<Cart> carts = cartMapper.selectList(cartWrapper);

            if (carts.isEmpty()) {
                throw new BusinessException(ResultCode.CART_EMPTY);
            }

            for (Cart cart : carts) {
                Product product = productMapper.selectById(cart.getProductId());
                if (product == null) {
                    throw new BusinessException(ResultCode.PRODUCT_NOT_EXIST);
                }

                if (product.getStock() < cart.getQuantity()) {
                    throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
                }

                merchantId = product.getMerchantId();

                // 创建订单明细
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderNo(orderNo);
                orderItem.setProductId(product.getId());
                orderItem.setProductName(product.getName());
                orderItem.setProductImage(product.getMainImage());
                orderItem.setPrice(product.getPrice());
                orderItem.setQuantity(cart.getQuantity());
                orderItem.setTotalAmount(product.getPrice().multiply(new BigDecimal(cart.getQuantity())));
                orderItemMapper.insert(orderItem);

                totalAmount = totalAmount.add(orderItem.getTotalAmount());

                // 扣减库存
                product.setStock(product.getStock() - cart.getQuantity());
                product.setSales(product.getSales() + cart.getQuantity());
                productMapper.updateById(product);
            }

            // 清除购物车
            cartMapper.delete(cartWrapper);
        }

        // 创建订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setMerchantId(merchantId);
        order.setTotalAmount(totalAmount);
        order.setFreightAmount(BigDecimal.ZERO);
        order.setRemark(dto.getRemark());

        // 优惠券抵扣逻辑
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (dto.getCouponId() != null) {
            UserCoupon userCoupon = userCouponMapper.selectById(dto.getCouponId());
            if (userCoupon == null || !userCoupon.getUserId().equals(userId) || userCoupon.getStatus() != 0) {
                throw new BusinessException("优惠券不可用");
            }
            // 检查有效期
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(userCoupon.getValidFrom()) || now.isAfter(userCoupon.getValidUntil())) {
                throw new BusinessException("优惠券已过期");
            }
            Coupon coupon = couponMapper.selectById(userCoupon.getCouponId());
            if (coupon == null || coupon.getStatus() != 1) {
                throw new BusinessException("优惠券不可用");
            }
            // 检查最低消费金额
            if (coupon.getMinimumAmount() != null && totalAmount.compareTo(coupon.getMinimumAmount()) < 0) {
                throw new BusinessException("订单金额未达到优惠券使用条件");
            }
            // 计算折扣金额
            if (coupon.getCouponType() == 1) {
                // 满减券
                discountAmount = coupon.getDiscountAmount();
            } else if (coupon.getCouponType() == 2) {
                // 折扣券
                discountAmount = totalAmount.subtract(totalAmount.multiply(coupon.getDiscountRate()));
                if (coupon.getMaximumDiscount() != null && discountAmount.compareTo(coupon.getMaximumDiscount()) > 0) {
                    discountAmount = coupon.getMaximumDiscount();
                }
            } else if (coupon.getCouponType() == 3) {
                // 无门槛券
                discountAmount = coupon.getDiscountAmount();
            }
            // 抵扣不能超过订单金额
            if (discountAmount.compareTo(totalAmount) > 0) {
                discountAmount = totalAmount;
            }
            // 标记优惠券已使用
            userCoupon.setStatus(1);
            userCoupon.setUseTime(LocalDateTime.now());
            userCoupon.setOrderNo(orderNo);
            userCouponMapper.updateById(userCoupon);
            // 更新优惠券已使用数量
            coupon.setUsedQuantity(coupon.getUsedQuantity() + 1);
            couponMapper.updateById(coupon);
            order.setCouponId(dto.getCouponId());
            order.setCouponAmount(discountAmount);
        }

        order.setDiscountAmount(discountAmount);
        order.setPayAmount(totalAmount.subtract(discountAmount));
        order.setStatus(OrderStatusEnum.PENDING_PAYMENT.getCode());
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setReceiverAddress(
                address.getProvince() + address.getCity() + address.getDistrict() + address.getDetailAddress());
        baseMapper.insert(order);

        return orderNo;
    }

    @Override
    public void cancelOrder(Long userId, String orderNo) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo);
        wrapper.eq(Order::getUserId, userId);

        Order order = baseMapper.selectOne(wrapper);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }

        if (!order.getStatus().equals(OrderStatusEnum.PENDING_PAYMENT.getCode())) {
            throw new BusinessException(ResultCode.ORDER_CANNOT_CANCEL);
        }

        order.setStatus(OrderStatusEnum.CANCELLED.getCode());
        baseMapper.updateById(order);

        // 恢复库存
        restoreStock(orderNo);

        // 返还优惠券
        if (order.getCouponId() != null) {
            UserCoupon userCoupon = userCouponMapper.selectById(order.getCouponId());
            if (userCoupon != null && userCoupon.getStatus() == 1) {
                userCoupon.setStatus(0);
                userCoupon.setUseTime(null);
                userCoupon.setOrderNo(null);
                userCouponMapper.updateById(userCoupon);
                // 优惠券已使用数量减1
                Coupon coupon = couponMapper.selectById(userCoupon.getCouponId());
                if (coupon != null && coupon.getUsedQuantity() > 0) {
                    coupon.setUsedQuantity(coupon.getUsedQuantity() - 1);
                    couponMapper.updateById(coupon);
                }
            }
        }
    }

    @Override
    public void payOrder(Long userId, String orderNo) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo);
        wrapper.eq(Order::getUserId, userId);

        Order order = baseMapper.selectOne(wrapper);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }

        if (!order.getStatus().equals(OrderStatusEnum.PENDING_PAYMENT.getCode())) {
            throw new BusinessException(ResultCode.ORDER_CANNOT_PAY);
        }

        order.setStatus(OrderStatusEnum.PENDING_DELIVERY.getCode());
        order.setPayTime(LocalDateTime.now());
        baseMapper.updateById(order);
    }

    @Override
    public void deliverOrder(Long merchantId, DeliverDTO dto) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, dto.getOrderNo());
        wrapper.eq(Order::getMerchantId, merchantId);

        Order order = baseMapper.selectOne(wrapper);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }

        if (!order.getStatus().equals(OrderStatusEnum.PENDING_DELIVERY.getCode())) {
            throw new BusinessException(ResultCode.ORDER_CANNOT_DELIVER);
        }

        order.setStatus(OrderStatusEnum.PENDING_RECEIVE.getCode());
        order.setDeliveryTime(LocalDateTime.now());
        order.setDeliveryCompany(dto.getDeliveryCompany());
        order.setDeliveryNo(dto.getDeliveryNo());
        baseMapper.updateById(order);

        // 通知用户订单已发货
        try {
            notificationService.createNotification(
                    order.getUserId(), "order", "订单已发货",
                    "您的订单 #" + order.getOrderNo() + " 已发货，请注意查收",
                    order.getId());
        } catch (Exception e) {
            log.warn("发送发货通知失败: orderNo={}", dto.getOrderNo(), e);
        }
    }

    @Override
    public void confirmReceive(Long userId, String orderNo) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo);
        wrapper.eq(Order::getUserId, userId);

        Order order = baseMapper.selectOne(wrapper);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }

        if (!order.getStatus().equals(OrderStatusEnum.PENDING_RECEIVE.getCode())) {
            throw new BusinessException(ResultCode.ORDER_CANNOT_CONFIRM);
        }

        order.setStatus(OrderStatusEnum.COMPLETED.getCode());
        order.setReceiveTime(LocalDateTime.now());
        baseMapper.updateById(order);

        // 通知用户订单已完成
        try {
            notificationService.createNotification(
                    userId, "order", "订单已完成",
                    "订单 #" + orderNo + " 已完成，欢迎再次光临",
                    order.getId());
        } catch (Exception e) {
            log.warn("发送完成通知失败: orderNo={}", orderNo, e);
        }
        // 确认收货后奖励积分（1元=1积分）
        try {
            int points = order.getTotalAmount().intValue();
            if (points > 0) {
                pointService.addPoints(userId, points, 3, String.valueOf(order.getId()), "订单消费奖励");
            }
        } catch (Exception e) {
            // 积分奖励失败不影响确认收货
            log.warn("确认收货后奖励积分失败: userId={}, orderNo={}", userId, orderNo, e);
        }
        // 检查是否达到VIP自动升级条件
        try {
            vipService.checkAndUpgradeByOrders(userId);
        } catch (Exception e) {
            log.warn("VIP自动升级检查失败: userId={}, orderNo={}", userId, orderNo, e);
        }
        // 更新累计消费金额
        try {
            User user = userMapper.selectById(userId);
            if (user != null) {
                BigDecimal current = user.getTotalConsumption() != null ? user.getTotalConsumption() : BigDecimal.ZERO;
                user.setTotalConsumption(current.add(order.getTotalAmount()));
                userMapper.updateById(user);
            }
        } catch (Exception e) {
            log.warn("更新累计消费失败: userId={}, orderNo={}", userId, orderNo, e);
        }
    }

    @Override
    public void refundApply(Long userId, String orderNo) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo);
        wrapper.eq(Order::getUserId, userId);

        Order order = baseMapper.selectOne(wrapper);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }

        // 只有待发货和待收货状态可以申请退款
        if (!order.getStatus().equals(OrderStatusEnum.PENDING_DELIVERY.getCode())
                && !order.getStatus().equals(OrderStatusEnum.PENDING_RECEIVE.getCode())) {
            throw new BusinessException(ResultCode.ORDER_CANNOT_REFUND);
        }

        order.setStatus(OrderStatusEnum.REFUNDING.getCode());
        baseMapper.updateById(order);
    }

    /**
     * 恢复库存
     */
    private void restoreStock(String orderNo) {
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderNo, orderNo);
        List<OrderItem> items = orderItemMapper.selectList(wrapper);

        for (OrderItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                product.setSales(product.getSales() - item.getQuantity());
                productMapper.updateById(product);
            }
        }
    }

    private void fillOrderItems(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }

        List<String> orderNos = orders.stream()
                .map(Order::getOrderNo)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        if (orderNos.isEmpty()) {
            return;
        }

        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.in(OrderItem::getOrderNo, orderNos);
        List<OrderItem> allItems = orderItemMapper.selectList(itemWrapper);

        Map<String, List<OrderItem>> itemMap = allItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderNo));

        orders.forEach(order -> order.setOrderItems(
                itemMap.getOrDefault(order.getOrderNo(), Collections.emptyList())));
    }
}
