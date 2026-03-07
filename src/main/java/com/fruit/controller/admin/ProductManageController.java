package com.fruit.controller.admin;

import com.fruit.common.result.PageResult;
import com.fruit.common.result.Result;
import com.fruit.entity.Product;
import com.fruit.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员商品管理控制器
 */
@Api(tags = "管理员-商品管理")
@RestController
@RequestMapping("/admin/product")
@RequiredArgsConstructor
public class ProductManageController {

    private final ProductService productService;

    @ApiOperation("商品列表")
    @GetMapping("/list")
    public Result<PageResult<Product>> list(
            @ApiParam("商品状态") @RequestParam(required = false) Integer status,
            @ApiParam("关键词") @RequestParam(required = false) String keyword,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {

        PageResult<Product> pageResult = productService.listForAdmin(status, keyword, pageNum, pageSize);
        return Result.success(pageResult);
    }

    @ApiOperation("商品详情")
    @GetMapping("/detail/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        Product product = productService.getById(id);
        return Result.success(product);
    }

    @ApiOperation("审核商品")
    @PutMapping("/approve/{id}")
    public Result<Void> approve(@PathVariable Long id, @RequestParam Integer status) {
        productService.approve(id, status);
        return Result.success();
    }

    @ApiOperation("删除商品")
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.removeById(id);
        return Result.success();
    }
}
