package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruit.common.exception.BusinessException;
import com.fruit.common.result.ResultCode;
import com.fruit.entity.MerchantShop;
import com.fruit.entity.ShopFollow;
import com.fruit.mapper.MerchantShopMapper;
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

        // 查询是否存在逻辑删除记录（需绕过 @TableLogic 过滤）
        QueryWrapper<ShopFollow> deletedWrapper = new QueryWrapper<>();
        deletedWrapper.eq("user_id", userId)
                .eq("shop_id", shopId)
                .eq("deleted", 1);
        ShopFollow existing = getBaseMapper().selectOne(deletedWrapper);

        if (existing != null) {
            // 恢复逻辑删除记录
            existing.setDeleted(0);
            getBaseMapper().updateById(existing);
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

        return merchantShopMapper.selectBatchIds(shopIds);
    }
}
