package com.fruit.controller.consumer;

import com.fruit.common.result.PageResult;
import com.fruit.common.result.Result;
import com.fruit.entity.Product;
import com.fruit.entity.ProductTraceability;
import com.fruit.service.ProductService;
import com.fruit.service.ProductTraceabilityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消费者商品控制器
 */
@Api(tags = "消费者-商品")
@RestController
@RequestMapping("/consumer/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductTraceabilityService traceabilityService;

    @ApiOperation("商品列表")
    @GetMapping("/list")
    public Result<PageResult<Product>> list(
            @ApiParam("分类ID") @RequestParam(required = false) Long categoryId,
            @ApiParam("关键词") @RequestParam(required = false) String keyword,
            @ApiParam("排序字段") @RequestParam(required = false) String sortField,
            @ApiParam("排序字段") @RequestParam(required = false) String sortBy,
            @ApiParam("排序方式") @RequestParam(defaultValue = "desc") String sortOrder,
            @ApiParam("活动类型") @RequestParam(required = false) String activity,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {

        String effectiveSort = sortField != null ? sortField : (sortBy != null ? sortBy : "createTime");
        PageResult<Product> pageResult = productService.listForConsumer(categoryId, keyword, effectiveSort, sortOrder, activity, pageNum, pageSize);
        return Result.success(pageResult);
    }

    @ApiOperation("搜索商品")
    @GetMapping("/search")
    public Result<PageResult<Product>> search(
            @ApiParam("关键词") @RequestParam String keyword,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "12") Integer pageSize) {
        PageResult<Product> pageResult = productService.listForConsumer(null, keyword, "createTime", "desc", pageNum, pageSize);
        return Result.success(pageResult);
    }

    @ApiOperation("热销商品")
    @GetMapping("/hot")
    public Result<List<Product>> hot(@ApiParam("数量") @RequestParam(defaultValue = "8") Integer limit) {
        List<Product> list = productService.getHotProducts(limit);
        return Result.success(list);
    }

    @ApiOperation("新品商品")
    @GetMapping("/new")
    public Result<List<Product>> newProducts(@ApiParam("数量") @RequestParam(defaultValue = "8") Integer limit) {
        List<Product> list = productService.getNewProducts(limit);
        return Result.success(list);
    }

    @ApiOperation("推荐商品")
    @GetMapping("/recommend")
    public Result<List<Product>> recommend(@ApiParam("数量") @RequestParam(defaultValue = "8") Integer limit) {
        List<Product> list = productService.getRecommendProducts(limit);
        return Result.success(list);
    }

    @ApiOperation("商品详情")
    @GetMapping("/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        Product product = productService.getProductDetail(id);
        return Result.success(product);
    }

    @ApiOperation("商品溯源信息")
    @GetMapping("/{id}/traceability")
    public Result<List<ProductTraceability>> traceability(@PathVariable Long id) {
        List<ProductTraceability> list = traceabilityService.listByProductId(id);
        return Result.success(list);
    }
}
