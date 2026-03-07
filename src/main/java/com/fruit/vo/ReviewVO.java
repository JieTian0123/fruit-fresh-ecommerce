package com.fruit.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价VO
 */
@Data
public class ReviewVO {

    private Long id;

    private String orderNo;

    private Long productId;

    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户头像
     */
    private String userAvatar;

    private Integer rating;

    private String content;

    private String images;

    private String reply;

    private LocalDateTime replyTime;

    private Integer status;

    private LocalDateTime createTime;

    /**
     * 商品名称（商家视图）
     */
    private String productName;

    /**
     * 商品图片（商家视图）
     */
    private String productImage;
}
