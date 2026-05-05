package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fruit.common.exception.BusinessException;
import com.fruit.common.result.ResultCode;
import com.fruit.entity.Cart;
import com.fruit.entity.Product;
import com.fruit.mapper.CartMapper;
import com.fruit.mapper.ProductMapper;
import com.fruit.vo.CartVO;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
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
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @InjectMocks
    private CartServiceImpl cartService;

    @Mock
    private CartMapper cartMapper;

    @Mock
    private ProductMapper productMapper;

    @BeforeAll
    static void init() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), Cart.class);
    }

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(cartService, "baseMapper", cartMapper);
    }

    @Test
    @DisplayName("获取购物车列表 - 成功")
    void getCartList_Success() {
        Long userId = 1L;

        Cart cart1 = new Cart();
        cart1.setId(100L);
        cart1.setUserId(userId);
        cart1.setProductId(200L);
        cart1.setQuantity(2);
        cart1.setSelected(1);

        Cart cart2 = new Cart();
        cart2.setId(101L);
        cart2.setUserId(userId);
        cart2.setProductId(201L);
        cart2.setQuantity(1);
        cart2.setSelected(0);

        when(cartMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(cart1, cart2));

        Product product1 = new Product();
        product1.setId(200L);
        product1.setName("Apple");
        product1.setMainImage("apple.jpg");
        product1.setPrice(new BigDecimal("10.0"));
        product1.setStock(100);

        when(productMapper.selectById(200L)).thenReturn(product1);
        when(productMapper.selectById(201L)).thenReturn(null);

        List<CartVO> result = cartService.getCartList(userId);

        assertEquals(2, result.size());

        CartVO vo1 = result.get(0);
        assertEquals(100L, vo1.getId());
        assertEquals(200L, vo1.getProductId());
        assertEquals("Apple", vo1.getProductName());
        assertEquals("apple.jpg", vo1.getProductImage());
        assertEquals(new BigDecimal("10.0"), vo1.getPrice());
        assertEquals(100, vo1.getStock());

        CartVO vo2 = result.get(1);
        assertEquals(101L, vo2.getId());
        assertEquals(201L, vo2.getProductId());
        assertNull(vo2.getProductName());
    }

    @Test
    @DisplayName("添加购物车 - 新商品")
    void addCart_NewProduct() {
        Long userId = 1L;
        Long productId = 200L;
        Integer quantity = 2;

        Product product = new Product();
        product.setId(productId);
        product.setMerchantId(300L);

        when(productMapper.selectById(productId)).thenReturn(product);
        when(cartMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        cartService.addCart(userId, productId, quantity);

        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(cartMapper).insert(cartCaptor.capture());

        Cart savedCart = cartCaptor.getValue();
        assertEquals(userId, savedCart.getUserId());
        assertEquals(productId, savedCart.getProductId());
        assertEquals(300L, savedCart.getMerchantId());
        assertEquals(quantity, savedCart.getQuantity());
        assertEquals(1, savedCart.getSelected());
    }

    @Test
    @DisplayName("添加购物车 - 已存在商品")
    void addCart_ExistingProduct() {
        Long userId = 1L;
        Long productId = 200L;
        Integer quantity = 2;

        Product product = new Product();
        product.setId(productId);
        product.setMerchantId(300L);

        Cart existCart = new Cart();
        existCart.setId(100L);
        existCart.setUserId(userId);
        existCart.setProductId(productId);
        existCart.setQuantity(3);

        when(productMapper.selectById(productId)).thenReturn(product);
        when(cartMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existCart);

        cartService.addCart(userId, productId, quantity);

        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(cartMapper).updateById(cartCaptor.capture());

        Cart updatedCart = cartCaptor.getValue();
        assertEquals(100L, updatedCart.getId());
        assertEquals(5, updatedCart.getQuantity());
    }

    @Test
    @DisplayName("添加购物车 - 商品不存在")
    void addCart_ProductNotExist() {
        Long userId = 1L;
        Long productId = 200L;
        Integer quantity = 2;

        when(productMapper.selectById(productId)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            cartService.addCart(userId, productId, quantity);
        });

        assertEquals(ResultCode.PRODUCT_NOT_EXIST.getCode(), exception.getCode());
        verify(cartMapper, never()).selectOne(any());
        verify(cartMapper, never()).insert(any());
        verify(cartMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("更新购物车数量 - 成功")
    void updateQuantity_Success() {
        Long userId = 1L;
        Long cartId = 100L;
        Integer quantity = 5;

        Cart cart = new Cart();
        cart.setId(cartId);
        cart.setUserId(userId);
        cart.setQuantity(2);

        when(cartMapper.selectById(cartId)).thenReturn(cart);

        cartService.updateQuantity(userId, cartId, quantity);

        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(cartMapper).updateById(cartCaptor.capture());

        Cart updatedCart = cartCaptor.getValue();
        assertEquals(cartId, updatedCart.getId());
        assertEquals(quantity, updatedCart.getQuantity());
    }

    @Test
    @DisplayName("更新购物车数量 - 购物车不存在或不属于该用户")
    void updateQuantity_CartNotExist() {
        Long userId = 1L;
        Long cartId = 100L;
        Integer quantity = 5;

        when(cartMapper.selectById(cartId)).thenReturn(null);

        BusinessException exception1 = assertThrows(BusinessException.class, () -> {
            cartService.updateQuantity(userId, cartId, quantity);
        });
        assertEquals(ResultCode.CART_NOT_EXIST.getCode(), exception1.getCode());

        Cart cart = new Cart();
        cart.setId(cartId);
        cart.setUserId(2L);

        when(cartMapper.selectById(cartId)).thenReturn(cart);

        BusinessException exception2 = assertThrows(BusinessException.class, () -> {
            cartService.updateQuantity(userId, cartId, quantity);
        });
        assertEquals(ResultCode.CART_NOT_EXIST.getCode(), exception2.getCode());

        verify(cartMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("删除购物车 - 成功")
    void deleteCart_Success() {
        Long userId = 1L;
        Long cartId = 100L;

        Cart cart = new Cart();
        cart.setId(cartId);
        cart.setUserId(userId);

        when(cartMapper.selectById(cartId)).thenReturn(cart);

        cartService.deleteCart(userId, cartId);

        verify(cartMapper).deleteById(cartId);
    }

    @Test
    @DisplayName("删除购物车 - 购物车不存在或不属于该用户")
    void deleteCart_CartNotExist() {
        Long userId = 1L;
        Long cartId = 100L;

        when(cartMapper.selectById(cartId)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            cartService.deleteCart(userId, cartId);
        });
        assertEquals(ResultCode.CART_NOT_EXIST.getCode(), exception.getCode());

        verify(cartMapper, never()).deleteById(any(Long.class));
    }

    @Test
    @DisplayName("清空购物车 - 成功")
    void clearCart_Success() {
        Long userId = 1L;

        cartService.clearCart(userId);

        verify(cartMapper).delete(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("更新选中状态 - 成功")
    void updateSelected_Success() {
        Long userId = 1L;
        Long cartId = 100L;
        Integer selected = 1;

        Cart cart = new Cart();
        cart.setId(cartId);
        cart.setUserId(userId);
        cart.setSelected(0);

        when(cartMapper.selectById(cartId)).thenReturn(cart);

        cartService.updateSelected(userId, cartId, selected);

        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(cartMapper).updateById(cartCaptor.capture());

        Cart updatedCart = cartCaptor.getValue();
        assertEquals(cartId, updatedCart.getId());
        assertEquals(selected, updatedCart.getSelected());
    }

    @Test
    @DisplayName("更新选中状态 - 购物车不存在或不属于该用户")
    void updateSelected_CartNotExist() {
        Long userId = 1L;
        Long cartId = 100L;
        Integer selected = 1;

        when(cartMapper.selectById(cartId)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            cartService.updateSelected(userId, cartId, selected);
        });
        assertEquals(ResultCode.CART_NOT_EXIST.getCode(), exception.getCode());

        verify(cartMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("全选/全不选 - 成功")
    void selectAll_Success() {
        Long userId = 1L;
        Integer selected = 1;

        cartService.selectAll(userId, selected);

        verify(cartMapper).update(eq(null), any(LambdaUpdateWrapper.class));
    }
}
