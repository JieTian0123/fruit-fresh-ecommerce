package com.fruit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruit.entity.MerchantShop;
import com.fruit.entity.ShopFollow;

/**
 * 关注店铺服务接口
 */
public interface ShopFollowService extends IService<ShopFollow> {

    /**
     * 关注店铺
     */
    void followShop(Long userId, Long shopId);

    /**
     * 取消关注
     */
    void unfollowShop(Long userId, Long shopId);

    /**
     * 检查是否已关注
     */
    boolean isFollowed(Long userId, Long shopId);

    /**
     * 获取用户关注的店铺列表
     */
    java.util.List<MerchantShop> getFollowedShops(Long userId);
}
