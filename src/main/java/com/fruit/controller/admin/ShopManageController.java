package com.fruit.controller.admin;

import com.fruit.common.result.PageResult;
import com.fruit.common.result.Result;
import com.fruit.entity.MerchantShop;
import com.fruit.service.MerchantShopService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员店铺管理控制器
 */
@Api(tags = "管理员-店铺管理")
@RestController
@RequestMapping("/admin/shop")
@RequiredArgsConstructor
public class ShopManageController {

    private final MerchantShopService merchantShopService;

    @ApiOperation("店铺列表")
    @GetMapping("/list")
    public Result<PageResult<MerchantShop>> list(
            @ApiParam("店铺状态") @RequestParam(required = false) Integer status,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {

        PageResult<MerchantShop> pageResult = merchantShopService.listForAdmin(status, pageNum, pageSize);
        return Result.success(pageResult);
    }

    @ApiOperation("店铺详情")
    @GetMapping("/detail/{id}")
    public Result<MerchantShop> detail(@PathVariable Long id) {
        MerchantShop shop = merchantShopService.getById(id);
        return Result.success(shop);
    }

    @ApiOperation("审核店铺")
    @PutMapping("/approve/{id}")
    public Result<Void> approve(@PathVariable Long id, @RequestParam Integer status) {
        merchantShopService.approveShop(id, status);
        return Result.success();
    }
}
