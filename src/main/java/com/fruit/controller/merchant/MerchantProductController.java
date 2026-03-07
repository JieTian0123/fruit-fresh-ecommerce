package com.fruit.controller.merchant;

import com.fruit.common.result.PageResult;
import com.fruit.common.result.Result;
import com.fruit.dto.ProductDTO;
import com.fruit.entity.Product;
import com.fruit.service.ProductService;
import com.fruit.utils.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 商家商品控制器
 */
@Api(tags = "商家-商品")
@RestController
@RequestMapping("/merchant/product")
@RequiredArgsConstructor
public class MerchantProductController {

    private final ProductService productService;

    @ApiOperation("商品列表")
    @GetMapping("/list")
    public Result<PageResult<Product>> list(
            @ApiParam("商品状态") @RequestParam(required = false) Integer status,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {

        Long merchantId = UserContext.getUserId();
        PageResult<Product> pageResult = productService.listForMerchant(merchantId, status, pageNum, pageSize);
        return Result.success(pageResult);
    }

    @ApiOperation("商品详情")
    @GetMapping("/detail/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        Product product = productService.getById(id);
        return Result.success(product);
    }

    @ApiOperation("添加商品")
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody ProductDTO dto) {
        Long merchantId = UserContext.getUserId();
        productService.addProduct(merchantId, dto);
        return Result.success();
    }

    @ApiOperation("修改商品")
    @PutMapping("/update/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ProductDTO dto) {
        Long merchantId = UserContext.getUserId();
        productService.updateProduct(merchantId, id, dto);
        return Result.success();
    }

    @ApiOperation("商品上架")
    @PutMapping("/onSale/{id}")
    public Result<Void> onSale(@PathVariable Long id) {
        Long merchantId = UserContext.getUserId();
        productService.onSale(merchantId, id);
        return Result.success();
    }

    @ApiOperation("商品下架")
    @PutMapping("/offSale/{id}")
    public Result<Void> offSale(@PathVariable Long id) {
        Long merchantId = UserContext.getUserId();
        productService.offSale(merchantId, id);
        return Result.success();
    }

    @ApiOperation("删除商品")
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long merchantId = UserContext.getUserId();
        productService.deleteProduct(merchantId, id);
        return Result.success();
    }
}
