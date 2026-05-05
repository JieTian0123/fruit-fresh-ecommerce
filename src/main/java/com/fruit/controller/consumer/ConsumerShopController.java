package com.fruit.controller.consumer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.common.result.PageResult;
import com.fruit.common.result.Result;
import com.fruit.entity.MerchantShop;
import com.fruit.entity.Product;
import com.fruit.mapper.MerchantShopMapper;
import com.fruit.mapper.ProductMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消费者端-店铺浏览控制器
 */
@Api(tags = "消费者-店铺浏览")
@RestController
@RequestMapping("/consumer/shop")
@RequiredArgsConstructor
public class ConsumerShopController {

    private final MerchantShopMapper merchantShopMapper;
    private final ProductMapper productMapper;

    @ApiOperation("获取店铺信息")
    @GetMapping("/{id}")
    public Result<MerchantShop> getShopInfo(@PathVariable Long id) {
        MerchantShop shop = merchantShopMapper.selectById(id);
        return Result.success(shop);
    }

    @ApiOperation("获取店铺商品列表")
    @GetMapping("/{id}/products")
    public Result<PageResult<Product>> getShopProducts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "12") Integer pageSize) {
        MerchantShop shop = merchantShopMapper.selectById(id);
        PageResult<Product> pageResult = new PageResult<>();
        pageResult.setPageNum(pageNum.longValue());
        pageResult.setPageSize(pageSize.longValue());

        if (shop == null || shop.getStatus() == null || shop.getStatus() != 1) {
            pageResult.setTotal(0L);
            pageResult.setPages(0L);
            pageResult.setList(java.util.Collections.emptyList());
            return Result.success(pageResult);
        }

        Page<Product> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getMerchantId, shop.getMerchantId());
        wrapper.eq(Product::getStatus, 1);
        wrapper.orderByDesc(Product::getCreateTime);
        Page<Product> result = productMapper.selectPage(page, wrapper);

        pageResult.setPageNum(pageNum.longValue());
        pageResult.setPageSize(pageSize.longValue());
        pageResult.setTotal(result.getTotal());
        pageResult.setPages(result.getPages());
        pageResult.setList(result.getRecords());
        return Result.success(pageResult);
    }
}
