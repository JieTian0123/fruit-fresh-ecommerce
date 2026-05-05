package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.common.exception.BusinessException;
import com.fruit.common.result.PageResult;
import com.fruit.common.result.ResultCode;
import com.fruit.dto.ReviewDTO;
import com.fruit.entity.*;
import com.fruit.enums.OrderStatusEnum;
import com.fruit.mapper.*;
import com.fruit.service.PointService;
import com.fruit.vo.ReviewVO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductReviewServiceImplTest {

    @Mock
    private ProductReviewMapper productReviewMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderItemMapper orderItemMapper;

    @Mock
    private PointService pointService;

    @InjectMocks
    private ProductReviewServiceImpl productReviewService;

    @BeforeAll
    static void initTableInfo() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new Configuration(), "");
        TableInfoHelper.initTableInfo(assistant, Order.class);
        TableInfoHelper.initTableInfo(assistant, OrderItem.class);
        TableInfoHelper.initTableInfo(assistant, ProductReview.class);
        TableInfoHelper.initTableInfo(assistant, Product.class);
        TableInfoHelper.initTableInfo(assistant, User.class);
    }

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(productReviewService, "baseMapper", productReviewMapper);
    }

    @Test
    void addReview_Success() {
        Long userId = 1L;
        ReviewDTO dto = new ReviewDTO();
        dto.setOrderNo("ORDER123");
        dto.setProductId(100L);
        dto.setRating(5);
        dto.setContent("Great product!");

        Order order = new Order();
        order.setStatus(OrderStatusEnum.COMPLETED.getCode());
        when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(order);

        OrderItem orderItem = new OrderItem();
        when(orderItemMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(orderItem);

        when(productReviewMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(productReviewMapper.insert(any(ProductReview.class))).thenReturn(1);

        assertDoesNotThrow(() -> productReviewService.addReview(userId, dto));

        verify(productReviewMapper).insert(any(ProductReview.class));
        verify(pointService).addPoints(eq(userId), eq(10), eq(4), any(), anyString());
    }

    @Test
    void addReview_OrderNotExist_ThrowsException() {
        Long userId = 1L;
        ReviewDTO dto = new ReviewDTO();
        dto.setOrderNo("ORDER123");

        when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> productReviewService.addReview(userId, dto));
        assertEquals(ResultCode.ORDER_NOT_EXIST.getCode(), exception.getCode());
    }

    @Test
    void addReview_OrderNotCompleted_ThrowsException() {
        Long userId = 1L;
        ReviewDTO dto = new ReviewDTO();
        dto.setOrderNo("ORDER123");

        Order order = new Order();
        order.setStatus(OrderStatusEnum.PENDING_PAYMENT.getCode());
        when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(order);

        BusinessException exception = assertThrows(BusinessException.class, () -> productReviewService.addReview(userId, dto));
        assertEquals(ResultCode.REVIEW_ORDER_NOT_COMPLETED.getCode(), exception.getCode());
    }

    @Test
    void addReview_ProductNotInOrder_ThrowsException() {
        Long userId = 1L;
        ReviewDTO dto = new ReviewDTO();
        dto.setOrderNo("ORDER123");
        dto.setProductId(100L);

        Order order = new Order();
        order.setStatus(OrderStatusEnum.COMPLETED.getCode());
        when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(order);

        when(orderItemMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> productReviewService.addReview(userId, dto));
        assertEquals(ResultCode.REVIEW_PRODUCT_NOT_IN_ORDER.getCode(), exception.getCode());
    }

    @Test
    void addReview_AlreadyReviewed_ThrowsException() {
        Long userId = 1L;
        ReviewDTO dto = new ReviewDTO();
        dto.setOrderNo("ORDER123");
        dto.setProductId(100L);

        Order order = new Order();
        order.setStatus(OrderStatusEnum.COMPLETED.getCode());
        when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(order);

        OrderItem orderItem = new OrderItem();
        when(orderItemMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(orderItem);

        when(productReviewMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> productReviewService.addReview(userId, dto));
        assertEquals(ResultCode.REVIEW_ALREADY_EXIST.getCode(), exception.getCode());
    }

    @Test
    void getProductReviews_Success() {
        Long productId = 100L;

        ProductReview review = new ProductReview();
        review.setUserId(1L);
        review.setContent("Good");

        Page<ProductReview> page = new Page<>(1, 10);
        page.setRecords(Collections.singletonList(review));
        page.setTotal(1);

        when(productReviewMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        when(userMapper.selectBatchIds(anyList())).thenReturn(Collections.singletonList(user));

        PageResult<ReviewVO> result = productReviewService.getProductReviews(productId, 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals("testuser", result.getList().get(0).getUsername());
    }

    @Test
    void getMerchantReviews_Success() {
        Long merchantId = 1L;

        Product product = new Product();
        product.setId(100L);
        product.setName("Apple");
        when(productMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(product));

        ProductReview review = new ProductReview();
        review.setUserId(2L);
        review.setProductId(100L);

        Page<ProductReview> page = new Page<>(1, 10);
        page.setRecords(Collections.singletonList(review));
        page.setTotal(1);

        when(productReviewMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        User user = new User();
        user.setId(2L);
        user.setUsername("testuser");
        when(userMapper.selectBatchIds(anyList())).thenReturn(Collections.singletonList(user));

        when(productMapper.selectBatchIds(anyList())).thenReturn(Collections.singletonList(product));

        PageResult<ReviewVO> result = productReviewService.getMerchantReviews(merchantId, 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals("testuser", result.getList().get(0).getUsername());
        assertEquals("Apple", result.getList().get(0).getProductName());
    }

    @Test
    void getMerchantReviews_NoProducts_ReturnsEmpty() {
        Long merchantId = 1L;
        when(productMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        PageResult<ReviewVO> result = productReviewService.getMerchantReviews(merchantId, 1, 10);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
        assertTrue(result.getList().isEmpty());
    }

    @Test
    void replyReview_Success() {
        Long merchantId = 1L;
        Long reviewId = 10L;
        String reply = "Thank you!";

        ProductReview review = new ProductReview();
        review.setProductId(100L);
        when(productReviewMapper.selectById(reviewId)).thenReturn(review);

        Product product = new Product();
        product.setMerchantId(merchantId);
        when(productMapper.selectById(100L)).thenReturn(product);

        when(productReviewMapper.updateById(any(ProductReview.class))).thenReturn(1);

        assertDoesNotThrow(() -> productReviewService.replyReview(merchantId, reviewId, reply));

        verify(productReviewMapper).updateById(any(ProductReview.class));
        assertEquals(reply, review.getReply());
        assertNotNull(review.getReplyTime());
    }

    @Test
    void replyReview_ReviewNotExist_ThrowsException() {
        Long merchantId = 1L;
        Long reviewId = 10L;

        when(productReviewMapper.selectById(reviewId)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> productReviewService.replyReview(merchantId, reviewId, "reply"));
        assertEquals(ResultCode.REVIEW_NOT_EXIST.getCode(), exception.getCode());
    }

    @Test
    void replyReview_Forbidden_ThrowsException() {
        Long merchantId = 1L;
        Long reviewId = 10L;

        ProductReview review = new ProductReview();
        review.setProductId(100L);
        when(productReviewMapper.selectById(reviewId)).thenReturn(review);

        Product product = new Product();
        product.setMerchantId(2L); // Different merchant
        when(productMapper.selectById(100L)).thenReturn(product);

        BusinessException exception = assertThrows(BusinessException.class, () -> productReviewService.replyReview(merchantId, reviewId, "reply"));
        assertEquals(ResultCode.FORBIDDEN.getCode(), exception.getCode());
    }

    @Test
    void hasReviewed_ReturnsTrue() {
        when(productReviewMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        assertTrue(productReviewService.hasReviewed(1L, "ORDER123", 100L));
    }

    @Test
    void hasReviewed_ReturnsFalse() {
        when(productReviewMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        assertFalse(productReviewService.hasReviewed(1L, "ORDER123", 100L));
    }
}
