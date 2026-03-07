package com.fruit.controller.merchant;

import com.fruit.common.result.PageResult;
import com.fruit.common.result.Result;
import com.fruit.service.ProductReviewService;
import com.fruit.utils.UserContext;
import com.fruit.vo.ReviewVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商家评价控制器
 */
@Api(tags = "商家-商品评价")
@RestController
@RequestMapping("/merchant/review")
@RequiredArgsConstructor
public class MerchantReviewController {

    private final ProductReviewService productReviewService;

    @ApiOperation("评价列表")
    @GetMapping("/list")
    public Result<PageResult<ReviewVO>> list(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        Long merchantId = UserContext.getUserId();
        return Result.success(productReviewService.getMerchantReviews(merchantId, pageNum, pageSize));
    }

    @ApiOperation("回复评价")
    @PutMapping("/reply/{reviewId}")
    public Result<Void> reply(@PathVariable Long reviewId, @RequestBody Map<String, String> body) {
        Long merchantId = UserContext.getUserId();
        productReviewService.replyReview(merchantId, reviewId, body.get("reply"));
        return Result.success();
    }
}
