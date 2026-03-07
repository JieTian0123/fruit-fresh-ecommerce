package com.fruit.controller.consumer;

import com.fruit.common.result.Result;
import com.fruit.entity.MerchantShop;
import com.fruit.service.ShopFollowService;
import com.fruit.utils.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消费者关注店铺控制器
 */
@Api(tags = "消费者-关注店铺")
@RestController
@RequestMapping("/shop-follow")
@RequiredArgsConstructor
public class ShopFollowController {

    private final ShopFollowService shopFollowService;

    @ApiOperation("关注店铺")
    @PostMapping("/follow/{shopId}")
    public Result<Void> followShop(@PathVariable Long shopId) {
        Long userId = UserContext.getUserId();
        shopFollowService.followShop(userId, shopId);
        return Result.success();
    }

    @ApiOperation("取消关注")
    @PostMapping("/unfollow/{shopId}")
    public Result<Void> unfollowShop(@PathVariable Long shopId) {
        Long userId = UserContext.getUserId();
        shopFollowService.unfollowShop(userId, shopId);
        return Result.success();
    }

    @ApiOperation("检查是否已关注")
    @GetMapping("/check/{shopId}")
    public Result<Boolean> isFollowed(@PathVariable Long shopId) {
        Long userId = UserContext.getUserId();
        boolean followed = shopFollowService.isFollowed(userId, shopId);
        return Result.success(followed);
    }

    @ApiOperation("获取我关注的店铺列表")
    @GetMapping("/my")
    public Result<List<MerchantShop>> getMyFollowedShops() {
        Long userId = UserContext.getUserId();
        List<MerchantShop> shops = shopFollowService.getFollowedShops(userId);
        return Result.success(shops);
    }
}
