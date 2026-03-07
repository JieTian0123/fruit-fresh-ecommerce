package com.fruit.controller.merchant;

import com.fruit.common.result.Result;
import com.fruit.dto.ShopDTO;
import com.fruit.entity.MerchantShop;
import com.fruit.service.MerchantShopService;
import com.fruit.utils.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 商家店铺控制器
 */
@Api(tags = "商家-店铺")
@RestController
@RequestMapping("/merchant/shop")
@RequiredArgsConstructor
public class MerchantShopController {

    private final MerchantShopService merchantShopService;

    @ApiOperation("获取店铺信息")
    @GetMapping("/info")
    public Result<MerchantShop> info() {
        Long merchantId = UserContext.getUserId();
        MerchantShop shop = merchantShopService.getShopByMerchantId(merchantId);
        return Result.success(shop);
    }

    @ApiOperation("创建店铺")
    @PostMapping("/create")
    public Result<Void> create(@Valid @RequestBody ShopDTO dto) {
        Long merchantId = UserContext.getUserId();
        merchantShopService.createShop(merchantId, dto);
        return Result.success();
    }

    @ApiOperation("修改店铺信息")
    @PutMapping("/update")
    public Result<Void> update(@Valid @RequestBody ShopDTO dto) {
        Long merchantId = UserContext.getUserId();
        merchantShopService.updateShop(merchantId, dto);
        return Result.success();
    }
}
