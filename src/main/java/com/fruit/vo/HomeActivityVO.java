package com.fruit.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 首页固定活动展示对象
 */
@Data
public class HomeActivityVO {

    private Long id;

    private String code;

    private String title;

    private String subtitle;

    private String badge;

    private String actionText;

    private String imageUrl;

    private String linkUrl;

    private String theme;

    private Integer sort;

    private Integer status;

    private LocalDateTime createTime;
}
