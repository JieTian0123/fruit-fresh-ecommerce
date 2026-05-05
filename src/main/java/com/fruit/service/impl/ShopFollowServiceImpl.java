package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruit.common.exception.BusinessException;
import com.fruit.common.result.ResultCode;
import com.fruit.entity.MerchantShop;
import com.fruit.entity.Product;
import com.fruit.entity.ShopFollow;
import com.fruit.mapper.MerchantShopMapper;
import com.fruit.mapper.ProductMapper;
import com.fruit.mapper.ShopFollowMapper;
import com.fruit.service.ShopFollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 关注店铺服务实现
 */
@Service
@RequiredArgsConstructor
public class ShopFollowServiceImpl extends ServiceImpl<ShopFollowMapper, ShopFollow> implements ShopFollowService {

    private final MerchantShopMapper merchantShopMapper;
    private final ProductMapper productMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void followShop(Long userId, Long shopId) {
        // 检查店铺是否存在
        MerchantShop shop = merchantShopMapper.selectById(shopId);
        if (shop == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        // 检查是否已关注（active记录，@TableLogic 自动过滤 deleted=1）
        LambdaQueryWrapper<ShopFollow> activeWrapper = new LambdaQueryWrapper<>();
        activeWrapper.eq(ShopFollow::getUserId, userId)
                .eq(ShopFollow::getShopId, shopId);
        long count = count(activeWrapper);
        if (count > 0) {
            throw new BusinessException("您已关注该店铺");
        }

        // 使用自定义SQL绕过@TableLogic查找已软删除的记录
        ShopFollow existing = getBaseMapper().selectDeletedByUserAndShop(userId, shopId);

        if (existing != null) {
            // 使用自定义SQL恢复已软删除记录（绕过@TableLogic）
            getBaseMapper().restoreById(existing.getId());
        } else {
            // 新增关注记录
            ShopFollow follow = new ShopFollow();
            follow.setUserId(userId);
            follow.setShopId(shopId);
            save(follow);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfollowShop(Long userId, Long shopId) {
        LambdaQueryWrapper<ShopFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopFollow::getUserId, userId)
                .eq(ShopFollow::getShopId, shopId);
        
        remove(wrapper);
    }

    @Override
    public boolean isFollowed(Long userId, Long shopId) {
        LambdaQueryWrapper<ShopFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopFollow::getUserId, userId)
                .eq(ShopFollow::getShopId, shopId);
        return count(wrapper) > 0;
    }

    @Override
    public List<MerchantShop> getFollowedShops(Long userId) {
        // 查询用户关注的所有店铺ID
        LambdaQueryWrapper<ShopFollow> followWrapper = new LambdaQueryWrapper<>();
        followWrapper.eq(ShopFollow::getUserId, userId);
        List<ShopFollow> follows = list(followWrapper);

        if (follows.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        // 查询店铺信息
        List<Long> shopIds = follows.stream()
                .map(ShopFollow::getShopId)
                .collect(Collectors.toList());

        List<MerchantShop> shops = merchantShopMapper.selectBatchIds(shopIds);
        if (shops.isEmpty()) {
            return shops;
        }

        List<Long> merchantIds = shops.stream()
                .map(MerchantShop::getMerchantId)
                .collect(Collectors.toList());

        LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
        productWrapper.in(Product::getMerchantId, merchantIds)
                .eq(Product::getStatus, 1)
                .select(Product::getMerchantId);

        List<Product> products = productMapper.selectList(productWrapper);
        java.util.Map<Long, Long> productCountMap = products.stream()
                .collect(Collectors.groupingBy(Product::getMerchantId, Collectors.counting()));

        shops.forEach(shop -> shop.setProductCount(productCountMap.getOrDefault(shop.getMerchantId(), 0L).intValue()));
        return shops;
    }
}
