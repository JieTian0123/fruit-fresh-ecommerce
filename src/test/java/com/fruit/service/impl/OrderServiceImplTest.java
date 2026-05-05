package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.common.exception.BusinessException;
import com.fruit.common.result.PageResult;
import com.fruit.common.result.ResultCode;
import com.fruit.dto.DeliverDTO;
import com.fruit.dto.OrderCreateDTO;
import com.fruit.entity.*;
import com.fruit.enums.OrderStatusEnum;
import com.fruit.mapper.*;
import com.fruit.service.NotificationService;
import com.fruit.service.PointService;
import com.fruit.service.VipService;
import com.fruit.vo.OrderDetailVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @InjectMocks
    private OrderServiceImpl orderService;

    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderItemMapper orderItemMapper;
    @Mock
    private CartMapper cartMapper;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private AddressMapper addressMapper;
    @Mock
    private UserCouponMapper userCouponMapper;
    @Mock
    private CouponMapper couponMapper;
    @Mock
    private PointService pointService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private VipService vipService;
    @Mock
    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(orderService, "baseMapper", orderMapper);
    }

    @Test
    @DisplayName("消费者查询订单列表 - 成功")
    void listForConsumer_Success() {
        Long userId = 1L;
        Integer status = OrderStatusEnum.PENDING_PAYMENT.getCode();
        Integer pageNum = 1;
        Integer pageSize = 10;

        Order order = new Order();
        order.setOrderNo("ORDER123");
        order.setUserId(userId);
        order.setStatus(status);

        Page<Order> page = new Page<>(pageNum, pageSize);
        page.setRecords(Collections.singletonList(order));
        page.setTotal(1);

        when(orderMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrderNo("ORDER123");
        when(orderItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(orderItem));

        PageResult<Order> result = orderService.listForConsumer(userId, status, pageNum, pageSize);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals("ORDER123", result.getList().get(0).getOrderNo());
        assertNotNull(result.getList().get(0).getOrderItems());
        assertEquals(1, result.getList().get(0).getOrderItems().size());
    }

    @Test
    @DisplayName("商家查询订单列表 - 成功")
    void listForMerchant_Success() {
        Long merchantId = 1L;
        Integer status = OrderStatusEnum.PENDING_DELIVERY.getCode();
        Integer pageNum = 1;
        Integer pageSize = 10;

        Order order = new Order();
        order.setOrderNo("ORDER123");
        order.setMerchantId(merchantId);
        order.setStatus(status);

        Page<Order> page = new Page<>(pageNum, pageSize);
        page.setRecords(Collections.singletonList(order));
        page.setTotal(1);

        when(orderMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrderNo("ORDER123");
        orderItem.setProductName("测试商品");
        when(orderItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(orderItem));

        PageResult<Order> result = orderService.listForMerchant(merchantId, status, pageNum, pageSize);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals("ORDER123", result.getList().get(0).getOrderNo());
        assertNotNull(result.getList().get(0).getOrderItems());
        assertEquals("测试商品", result.getList().get(0).getOrderItems().get(0).getProductName());
    }

    @Test
    @DisplayName("管理员查询订单列表 - 成功")
    void listForAdmin_Success() {
        Integer status = OrderStatusEnum.COMPLETED.getCode();
        String orderNo = "ORDER123";
        Integer pageNum = 1;
        Integer pageSize = 10;

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setStatus(status);

        Page<Order> page = new Page<>(pageNum, pageSize);
        page.setRecords(Collections.singletonList(order));
        page.setTotal(1);

        when(orderMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        PageResult<Order> result = orderService.listForAdmin(status, orderNo, pageNum, pageSize);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals("ORDER123", result.getList().get(0).getOrderNo());
    }

    @Test
    @DisplayName("获取订单详情 - 成功")
    void getOrderDetail_Success() {
        String orderNo = "ORDER123";
        Long userId = 1L;

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setStatus(OrderStatusEnum.PENDING_PAYMENT.getCode());

        when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(order);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrderNo(orderNo);
        when(orderItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(orderItem));

        OrderDetailVO result = orderService.getOrderDetail(orderNo, userId);

        assertNotNull(result);
        assertEquals(orderNo, result.getOrderNo());
        assertEquals(OrderStatusEnum.PENDING_PAYMENT.getDesc(), result.getStatusName());
        assertEquals(1, result.getOrderItems().size());
    }

    @Test
    @DisplayName("获取订单详情 - 订单不存在")
    void getOrderDetail_OrderNotExist() {
        String orderNo = "ORDER123";
        Long userId = 1L;

        when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.getOrderDetail(orderNo, userId);
        });

        assertEquals(ResultCode.ORDER_NOT_EXIST.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("创建订单 - 立即购买模式 - 成功")
    void createOrder_BuyNow_Success() {
        Long userId = 1L;
        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setAddressId(1L);
        dto.setProductId(1L);
        dto.setQuantity(2);
        dto.setRemark("测试备注");

        Address address = new Address();
        address.setId(1L);
        address.setUserId(userId);
        address.setReceiverName("张三");
        address.setReceiverPhone("13800138000");
        address.setProvince("广东省");
        address.setCity("广州市");
        address.setDistrict("天河区");
        address.setDetailAddress("某某街道");

        when(addressMapper.selectById(1L)).thenReturn(address);

        Product product = new Product();
        product.setId(1L);
        product.setMerchantId(1L);
        product.setName("苹果");
        product.setPrice(new BigDecimal("10.00"));
        product.setStock(100);
        product.setSales(0);

        when(productMapper.selectById(1L)).thenReturn(product);
        when(orderItemMapper.insert(any(OrderItem.class))).thenReturn(1);
        when(productMapper.updateById(any(Product.class))).thenReturn(1);
        when(orderMapper.insert(any(Order.class))).thenReturn(1);

        String orderNo = orderService.createOrder(userId, dto);

        assertNotNull(orderNo);
        verify(orderItemMapper, times(1)).insert(any(OrderItem.class));
        verify(productMapper, times(1)).updateById(any(Product.class));
        verify(orderMapper, times(1)).insert(any(Order.class));
    }

    @Test
    @DisplayName("创建订单 - 购物车模式 - 成功")
    void createOrder_Cart_Success() {
        Long userId = 1L;
        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setAddressId(1L);

        Address address = new Address();
        address.setId(1L);
        address.setUserId(userId);
        when(addressMapper.selectById(1L)).thenReturn(address);

        Cart cart = new Cart();
        cart.setProductId(1L);
        cart.setQuantity(2);
        when(cartMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(cart));

        Product product = new Product();
        product.setId(1L);
        product.setMerchantId(1L);
        product.setPrice(new BigDecimal("10.00"));
        product.setStock(100);
        product.setSales(0);
        when(productMapper.selectById(1L)).thenReturn(product);

        when(orderItemMapper.insert(any(OrderItem.class))).thenReturn(1);
        when(productMapper.updateById(any(Product.class))).thenReturn(1);
        when(cartMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);
        when(orderMapper.insert(any(Order.class))).thenReturn(1);

        String orderNo = orderService.createOrder(userId, dto);

        assertNotNull(orderNo);
        verify(cartMapper, times(1)).delete(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("创建订单 - 地址不存在")
    void createOrder_AddressNotExist() {
        Long userId = 1L;
        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setAddressId(1L);

        when(addressMapper.selectById(1L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.createOrder(userId, dto);
        });

        assertEquals(ResultCode.ADDRESS_NOT_EXIST.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("创建订单 - 商品库存不足")
    void createOrder_StockNotEnough() {
        Long userId = 1L;
        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setAddressId(1L);
        dto.setProductId(1L);
        dto.setQuantity(10);

        Address address = new Address();
        address.setId(1L);
        address.setUserId(userId);
        when(addressMapper.selectById(1L)).thenReturn(address);

        Product product = new Product();
        product.setId(1L);
        product.setStock(5); // 库存不足
        when(productMapper.selectById(1L)).thenReturn(product);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.createOrder(userId, dto);
        });

        assertEquals(ResultCode.STOCK_NOT_ENOUGH.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("创建订单 - 使用优惠券 - 成功")
    void createOrder_WithCoupon_Success() {
        Long userId = 1L;
        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setAddressId(1L);
        dto.setProductId(1L);
        dto.setQuantity(2);
        dto.setCouponId(1L);

        Address address = new Address();
        address.setId(1L);
        address.setUserId(userId);
        when(addressMapper.selectById(1L)).thenReturn(address);

        Product product = new Product();
        product.setId(1L);
        product.setPrice(new BigDecimal("50.00")); // 总价 100
        product.setStock(100);
        product.setSales(0);
        when(productMapper.selectById(1L)).thenReturn(product);

        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setId(1L);
        userCoupon.setUserId(userId);
        userCoupon.setStatus(0);
        userCoupon.setValidFrom(LocalDateTime.now().minusDays(1));
        userCoupon.setValidUntil(LocalDateTime.now().plusDays(1));
        userCoupon.setCouponId(1L);
        when(userCouponMapper.selectById(1L)).thenReturn(userCoupon);

        Coupon coupon = new Coupon();
        coupon.setId(1L);
        coupon.setStatus(1);
        coupon.setMinimumAmount(new BigDecimal("50.00"));
        coupon.setCouponType(1); // 满减券
        coupon.setDiscountAmount(new BigDecimal("10.00"));
        coupon.setUsedQuantity(0);
        when(couponMapper.selectById(1L)).thenReturn(coupon);

        when(orderItemMapper.insert(any(OrderItem.class))).thenReturn(1);
        when(productMapper.updateById(any(Product.class))).thenReturn(1);
        when(userCouponMapper.updateById(any(UserCoupon.class))).thenReturn(1);
        when(couponMapper.updateById(any(Coupon.class))).thenReturn(1);
        when(orderMapper.insert(any(Order.class))).thenReturn(1);

        String orderNo = orderService.createOrder(userId, dto);

        assertNotNull(orderNo);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderMapper).insert(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();

        assertEquals(new BigDecimal("100.00"), savedOrder.getTotalAmount());
        assertEquals(new BigDecimal("10.00"), savedOrder.getDiscountAmount());
        assertEquals(new BigDecimal("90.00"), savedOrder.getPayAmount());
    }

    @Test
    @DisplayName("取消订单 - 成功")
    void cancelOrder_Success() {
        Long userId = 1L;
        String orderNo = "ORDER123";

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setStatus(OrderStatusEnum.PENDING_PAYMENT.getCode());
        order.setCouponId(1L);

        when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(order);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);

        OrderItem orderItem = new OrderItem();
        orderItem.setProductId(1L);
        orderItem.setQuantity(2);
        when(orderItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(orderItem));

        Product product = new Product();
        product.setId(1L);
        product.setStock(10);
        product.setSales(5);
        when(productMapper.selectById(1L)).thenReturn(product);

        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setId(1L);
        userCoupon.setStatus(1);
        userCoupon.setCouponId(1L);
        when(userCouponMapper.selectById(1L)).thenReturn(userCoupon);

        Coupon coupon = new Coupon();
        coupon.setId(1L);
        coupon.setUsedQuantity(1);
        when(couponMapper.selectById(1L)).thenReturn(coupon);

        orderService.cancelOrder(userId, orderNo);

        verify(orderMapper, times(1)).updateById(any(Order.class));
        verify(productMapper, times(1)).updateById(any(Product.class));
        verify(userCouponMapper, times(1)).updateById(any(UserCoupon.class));
        verify(couponMapper, times(1)).updateById(any(Coupon.class));
    }

    @Test
    @DisplayName("取消订单 - 状态错误")
    void cancelOrder_StatusError() {
        Long userId = 1L;
        String orderNo = "ORDER123";

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setStatus(OrderStatusEnum.PENDING_DELIVERY.getCode()); // 非待付款状态

        when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(order);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.cancelOrder(userId, orderNo);
        });

        assertEquals(ResultCode.ORDER_CANNOT_CANCEL.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("支付订单 - 成功")
    void payOrder_Success() {
        Long userId = 1L;
        String orderNo = "ORDER123";

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setStatus(OrderStatusEnum.PENDING_PAYMENT.getCode());

        when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(order);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);

        orderService.payOrder(userId, orderNo);

        verify(orderMapper, times(1)).updateById(any(Order.class));
        assertEquals(OrderStatusEnum.PENDING_DELIVERY.getCode(), order.getStatus());
        assertNotNull(order.getPayTime());
    }

    @Test
    @DisplayName("支付订单 - 状态错误")
    void payOrder_StatusError() {
        Long userId = 1L;
        String orderNo = "ORDER123";

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setStatus(OrderStatusEnum.PENDING_DELIVERY.getCode());

        when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(order);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.payOrder(userId, orderNo);
        });

        assertEquals(ResultCode.ORDER_CANNOT_PAY.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("发货 - 成功")
    void deliverOrder_Success() {
        Long merchantId = 1L;
        DeliverDTO dto = new DeliverDTO();
        dto.setOrderNo("ORDER123");
        dto.setDeliveryCompany("顺丰速运");
        dto.setDeliveryNo("SF123456789");

        Order order = new Order();
        order.setId(1L);
        order.setOrderNo(dto.getOrderNo());
        order.setMerchantId(merchantId);
        order.setUserId(1L);
        order.setStatus(OrderStatusEnum.PENDING_DELIVERY.getCode());

        when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(order);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);

        orderService.deliverOrder(merchantId, dto);

        verify(orderMapper, times(1)).updateById(any(Order.class));
        verify(notificationService, times(1)).createNotification(anyLong(), anyString(), anyString(), anyString(), anyLong());
        assertEquals(OrderStatusEnum.PENDING_RECEIVE.getCode(), order.getStatus());
        assertEquals(dto.getDeliveryCompany(), order.getDeliveryCompany());
        assertEquals(dto.getDeliveryNo(), order.getDeliveryNo());
        assertNotNull(order.getDeliveryTime());
    }

    @Test
    @DisplayName("确认收货 - 成功")
    void confirmReceive_Success() {
        Long userId = 1L;
        String orderNo = "ORDER123";

        Order order = new Order();
        order.setId(1L);
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setStatus(OrderStatusEnum.PENDING_RECEIVE.getCode());
        order.setTotalAmount(new BigDecimal("100.00"));

        when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(order);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);

        User user = new User();
        user.setId(userId);
        user.setTotalConsumption(new BigDecimal("50.00"));
        when(userMapper.selectById(userId)).thenReturn(user);

        orderService.confirmReceive(userId, orderNo);

        verify(orderMapper, times(1)).updateById(any(Order.class));
        verify(notificationService, times(1)).createNotification(anyLong(), anyString(), anyString(), anyString(), anyLong());
        verify(pointService, times(1)).addPoints(eq(userId), eq(100), eq(3), eq("1"), anyString());
        verify(vipService, times(1)).checkAndUpgradeByOrders(userId);
        verify(userMapper, times(1)).updateById(any(User.class));

        assertEquals(OrderStatusEnum.COMPLETED.getCode(), order.getStatus());
        assertNotNull(order.getReceiveTime());
        assertEquals(new BigDecimal("150.00"), user.getTotalConsumption());
    }

    @Test
    @DisplayName("申请退款 - 成功")
    void refundApply_Success() {
        Long userId = 1L;
        String orderNo = "ORDER123";

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setStatus(OrderStatusEnum.PENDING_DELIVERY.getCode());

        when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(order);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);

        orderService.refundApply(userId, orderNo);

        verify(orderMapper, times(1)).updateById(any(Order.class));
        assertEquals(OrderStatusEnum.REFUNDING.getCode(), order.getStatus());
    }

    @Test
    @DisplayName("申请退款 - 状态错误")
    void refundApply_StatusError() {
        Long userId = 1L;
        String orderNo = "ORDER123";

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setStatus(OrderStatusEnum.COMPLETED.getCode());

        when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(order);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.refundApply(userId, orderNo);
        });

        assertEquals(ResultCode.ORDER_CANNOT_REFUND.getCode(), exception.getCode());
    }
}
