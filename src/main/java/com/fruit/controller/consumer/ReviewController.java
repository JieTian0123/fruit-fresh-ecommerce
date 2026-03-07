package com.fruit.controller.consumer;

import com.fruit.common.result.PageResult;
import com.fruit.common.result.Result;
import com.fruit.dto.ReviewDTO;
import com.fruit.service.ProductReviewService;
import com.fruit.utils.UserContext;
import com.fruit.vo.ReviewVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 消费者评价控制器
 */
@Api(tags = "消费者-商品评价")
@RestController
@RequestMapping("/consumer/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ProductReviewService productReviewService;

    @ApiOperation("提交评价")
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody ReviewDTO dto) {
        Long userId = UserContext.getUserId();
        productReviewService.addReview(userId, dto);
        return Result.success();
    }

    @ApiOperation("商品评价列表")
    @GetMapping("/product/{productId}")
    public Result<PageResult<ReviewVO>> productReviews(
            @PathVariable Long productId,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(productReviewService.getProductReviews(productId, pageNum, pageSize));
    }

    @ApiOperation("检查是否已评价")
    @GetMapping("/check")
    public Result<Boolean> checkReviewed(
            @RequestParam String orderNo,
            @RequestParam Long productId) {
        Long userId = UserContext.getUserId();
        return Result.success(productReviewService.hasReviewed(userId, orderNo, productId));
    }
}
