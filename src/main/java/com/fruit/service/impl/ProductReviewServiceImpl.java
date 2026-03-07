package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruit.common.exception.BusinessException;
import com.fruit.common.result.PageResult;
import com.fruit.common.result.ResultCode;
import com.fruit.dto.ReviewDTO;
import com.fruit.entity.*;
import com.fruit.enums.OrderStatusEnum;
import com.fruit.mapper.*;
import com.fruit.service.ProductReviewService;
import com.fruit.service.PointService;
import com.fruit.vo.ReviewVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品评价服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductReviewServiceImpl extends ServiceImpl<ProductReviewMapper, ProductReview> implements ProductReviewService {

    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final PointService pointService;

    @Override
    public void addReview(Long userId, ReviewDTO dto) {
        // 校验订单是否存在且属于当前用户
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getOrderNo, dto.getOrderNo());
        orderWrapper.eq(Order::getUserId, userId);
        Order order = orderMapper.selectOne(orderWrapper);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }

        // 校验订单状态为已完成
        if (!order.getStatus().equals(OrderStatusEnum.COMPLETED.getCode())) {
            throw new BusinessException(ResultCode.REVIEW_ORDER_NOT_COMPLETED);
        }

        // 校验商品在订单中
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderNo, dto.getOrderNo());
        itemWrapper.eq(OrderItem::getProductId, dto.getProductId());
        OrderItem orderItem = orderItemMapper.selectOne(itemWrapper);
        if (orderItem == null) {
            throw new BusinessException(ResultCode.REVIEW_PRODUCT_NOT_IN_ORDER);
        }

        // 校验是否已评价
        if (hasReviewed(userId, dto.getOrderNo(), dto.getProductId())) {
            throw new BusinessException(ResultCode.REVIEW_ALREADY_EXIST);
        }

        // 创建评价
        ProductReview review = new ProductReview();
        review.setOrderNo(dto.getOrderNo());
        review.setProductId(dto.getProductId());
        review.setUserId(userId);
        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
        review.setImages(dto.getImages());
        review.setStatus(1);
        baseMapper.insert(review);

        // 评价奖励积分（5星10分，4星8分，3星6分，其他5分）
        try {
            int points = dto.getRating() >= 5 ? 10 : dto.getRating() >= 4 ? 8 : dto.getRating() >= 3 ? 6 : 5;
            pointService.addPoints(userId, points, 4, String.valueOf(review.getId()), "商品评价奖励");
        } catch (Exception e) {
            log.warn("评价奖励积分失败: userId={}, reviewId={}", userId, review.getId(), e);
        }
    }

    @Override
    public PageResult<ReviewVO> getProductReviews(Long productId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<ProductReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductReview::getProductId, productId);
        wrapper.eq(ProductReview::getStatus, 1);
        wrapper.orderByDesc(ProductReview::getCreateTime);

        Page<ProductReview> page = new Page<>(pageNum, pageSize);
        Page<ProductReview> result = baseMapper.selectPage(page, wrapper);

        List<ReviewVO> voList = Collections.emptyList();
        if (!result.getRecords().isEmpty()) {
            // 批量查询用户信息
            List<Long> userIds = result.getRecords().stream()
                    .map(ProductReview::getUserId)
                    .distinct()
                    .collect(Collectors.toList());
            List<User> users = userMapper.selectBatchIds(userIds);
            Map<Long, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, u -> u));

            voList = result.getRecords().stream().map(review -> {
                ReviewVO vo = new ReviewVO();
                BeanUtils.copyProperties(review, vo);
                User user = userMap.get(review.getUserId());
                if (user != null) {
                    vo.setUsername(user.getUsername());
                    vo.setUserAvatar(user.getAvatar());
                }
                return vo;
            }).collect(Collectors.toList());
        }

        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), voList);
    }

    @Override
    public PageResult<ReviewVO> getMerchantReviews(Long merchantId, Integer pageNum, Integer pageSize) {
        // 查询商家的所有商品ID
        LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
        productWrapper.eq(Product::getMerchantId, merchantId);
        productWrapper.select(Product::getId);
        List<Product> products = productMapper.selectList(productWrapper);

        if (products.isEmpty()) {
            return PageResult.of(1L, (long) pageSize, 0L, Collections.emptyList());
        }

        List<Long> productIds = products.stream()
                .map(Product::getId)
                .collect(Collectors.toList());

        // 查询评价
        LambdaQueryWrapper<ProductReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ProductReview::getProductId, productIds);
        wrapper.orderByDesc(ProductReview::getCreateTime);

        Page<ProductReview> page = new Page<>(pageNum, pageSize);
        Page<ProductReview> result = baseMapper.selectPage(page, wrapper);

        List<ReviewVO> voList = Collections.emptyList();
        if (!result.getRecords().isEmpty()) {
            // 批量查询用户信息
            List<Long> userIds = result.getRecords().stream()
                    .map(ProductReview::getUserId)
                    .distinct()
                    .collect(Collectors.toList());
            List<User> users = userMapper.selectBatchIds(userIds);
            Map<Long, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, u -> u));

            // 批量查询商品信息
            List<Long> reviewProductIds = result.getRecords().stream()
                    .map(ProductReview::getProductId)
                    .distinct()
                    .collect(Collectors.toList());
            List<Product> reviewProducts = productMapper.selectBatchIds(reviewProductIds);
            Map<Long, Product> productMap = reviewProducts.stream()
                    .collect(Collectors.toMap(Product::getId, p -> p));

            voList = result.getRecords().stream().map(review -> {
                ReviewVO vo = new ReviewVO();
                BeanUtils.copyProperties(review, vo);
                User user = userMap.get(review.getUserId());
                if (user != null) {
                    vo.setUsername(user.getUsername());
                    vo.setUserAvatar(user.getAvatar());
                }
                Product product = productMap.get(review.getProductId());
                if (product != null) {
                    vo.setProductName(product.getName());
                    vo.setProductImage(product.getMainImage());
                }
                return vo;
            }).collect(Collectors.toList());
        }

        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), voList);
    }

    @Override
    public void replyReview(Long merchantId, Long reviewId, String reply) {
        ProductReview review = baseMapper.selectById(reviewId);
        if (review == null) {
            throw new BusinessException(ResultCode.REVIEW_NOT_EXIST);
        }

        // 校验商品属于该商家
        Product product = productMapper.selectById(review.getProductId());
        if (product == null || !product.getMerchantId().equals(merchantId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        review.setReply(reply);
        review.setReplyTime(LocalDateTime.now());
        baseMapper.updateById(review);
    }

    @Override
    public boolean hasReviewed(Long userId, String orderNo, Long productId) {
        LambdaQueryWrapper<ProductReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductReview::getUserId, userId);
        wrapper.eq(ProductReview::getOrderNo, orderNo);
        wrapper.eq(ProductReview::getProductId, productId);
        return baseMapper.selectCount(wrapper) > 0;
    }
}
