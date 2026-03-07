package com.fruit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruit.common.result.PageResult;
import com.fruit.dto.ReviewDTO;
import com.fruit.entity.ProductReview;
import com.fruit.vo.ReviewVO;

/**
 * 商品评价服务接口
 */
public interface ProductReviewService extends IService<ProductReview> {

    /**
     * 提交评价
     */
    void addReview(Long userId, ReviewDTO dto);

    /**
     * 商品评价列表
     */
    PageResult<ReviewVO> getProductReviews(Long productId, Integer pageNum, Integer pageSize);

    /**
     * 商家评价列表
     */
    PageResult<ReviewVO> getMerchantReviews(Long merchantId, Integer pageNum, Integer pageSize);

    /**
     * 回复评价
     */
    void replyReview(Long merchantId, Long reviewId, String reply);

    /**
     * 检查是否已评价
     */
    boolean hasReviewed(Long userId, String orderNo, Long productId);
}
