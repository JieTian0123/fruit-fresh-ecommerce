package com.fruit.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 轮播图DTO
 */
@Data
public class BannerDTO {

    private Long id;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "图片不能为空")
    private String imageUrl;

    private String linkUrl;

    private Integer linkType;

    private Integer sort;

    private Integer status;
}
