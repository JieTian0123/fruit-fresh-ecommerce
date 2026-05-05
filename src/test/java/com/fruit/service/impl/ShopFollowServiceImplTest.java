package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import com.fruit.common.exception.BusinessException;
import com.fruit.common.result.ResultCode;
import com.fruit.entity.MerchantShop;
import com.fruit.entity.Product;
import com.fruit.entity.ShopFollow;
import com.fruit.mapper.MerchantShopMapper;
import com.fruit.mapper.ProductMapper;
import com.fruit.mapper.ShopFollowMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("关注店铺服务测试")
class ShopFollowServiceImplTest {

    @Mock
    private MerchantShopMapper merchantShopMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ShopFollowMapper shopFollowMapper;

    @InjectMocks
    private ShopFollowServiceImpl shopFollowService;

    @BeforeAll
    static void initMyBatisPlus() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, ShopFollow.class);
        TableInfoHelper.initTableInfo(assistant, Product.class);
    }

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(shopFollowService, "baseMapper", shopFollowMapper);
    }

    @Test
    @DisplayName("关注店铺 - 新增关注")
    void followShop_NewFollow() {
        Long userId = 1L;
        Long shopId = 100L;

        when(merchantShopMapper.selectById(shopId)).thenReturn(new MerchantShop());
        when(shopFollowMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(shopFollowMapper.selectDeletedByUserAndShop(userId, shopId)).thenReturn(null);

        shopFollowService.followShop(userId, shopId);

        verify(shopFollowMapper).insert(argThat(follow ->
            follow.getUserId().equals(userId) &&
            follow.getShopId().equals(shopId)
        ));
    }

    @Test
    @DisplayName("关注店铺 - 恢复已删除的关注")
    void followShop_RestoreDeleted() {
        Long userId = 1L;
        Long shopId = 100L;

        when(merchantShopMapper.selectById(shopId)).thenReturn(new MerchantShop());
        when(shopFollowMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        ShopFollow deletedFollow = new ShopFollow();
        deletedFollow.setId(50L);
        when(shopFollowMapper.selectDeletedByUserAndShop(userId, shopId)).thenReturn(deletedFollow);

        shopFollowService.followShop(userId, shopId);

        verify(shopFollowMapper).restoreById(50L);
        verify(shopFollowMapper, never()).insert(any(ShopFollow.class));
    }

    @Test
    @DisplayName("关注店铺 - 店铺不存在")
    void followShop_ShopNotFound() {
        Long userId = 1L;
        Long shopId = 100L;

        when(merchantShopMapper.selectById(shopId)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () ->
            shopFollowService.followShop(userId, shopId)
        );
        assertEquals(ResultCode.DATA_NOT_EXIST.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("关注店铺 - 已经关注")
    void followShop_AlreadyFollowed() {
        Long userId = 1L;
        Long shopId = 100L;

        when(merchantShopMapper.selectById(shopId)).thenReturn(new MerchantShop());
        when(shopFollowMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        BusinessException exception = assertThrows(BusinessException.class, () ->
            shopFollowService.followShop(userId, shopId)
        );
        assertEquals("您已关注该店铺", exception.getMessage());
    }

    @Test
    @DisplayName("取消关注店铺")
    void unfollowShop() {
        Long userId = 1L;
        Long shopId = 100L;

        shopFollowService.unfollowShop(userId, shopId);

        verify(shopFollowMapper).delete(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("是否已关注")
    void isFollowed() {
        Long userId = 1L;
        Long shopId = 100L;

        when(shopFollowMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        boolean result = shopFollowService.isFollowed(userId, shopId);

        assertTrue(result);
        verify(shopFollowMapper).selectCount(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("获取关注的店铺列表 - 有店铺")
    void getFollowedShops_WithShops() {
        Long userId = 1L;

        ShopFollow follow1 = new ShopFollow();
        follow1.setShopId(100L);
        ShopFollow follow2 = new ShopFollow();
        follow2.setShopId(200L);
        when(shopFollowMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(follow1, follow2));

        MerchantShop shop1 = new MerchantShop();
        shop1.setId(100L);
        shop1.setMerchantId(10L);
        MerchantShop shop2 = new MerchantShop();
        shop2.setId(200L);
        shop2.setMerchantId(20L);
        when(merchantShopMapper.selectBatchIds(Arrays.asList(100L, 200L))).thenReturn(Arrays.asList(shop1, shop2));

        Product product1 = new Product();
        product1.setMerchantId(10L);
        Product product2 = new Product();
        product2.setMerchantId(10L);
        Product product3 = new Product();
        product3.setMerchantId(20L);
        when(productMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(product1, product2, product3));

        List<MerchantShop> result = shopFollowService.getFollowedShops(userId);

        assertEquals(2, result.size());
        assertEquals(2, result.get(0).getProductCount()); // shop1 has 2 products
        assertEquals(1, result.get(1).getProductCount()); // shop2 has 1 product
    }

    @Test
    @DisplayName("获取关注的店铺列表 - 无关注")
    void getFollowedShops_Empty() {
        Long userId = 1L;

        when(shopFollowMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        List<MerchantShop> result = shopFollowService.getFollowedShops(userId);

        assertTrue(result.isEmpty());
        verify(merchantShopMapper, never()).selectBatchIds(anyList());
    }
}
