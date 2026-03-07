package com.fruit.vo;

import com.fruit.entity.Banner;
import com.fruit.entity.Product;
import lombok.Data;

import java.util.List;

/**
 * 首页VO
 */
@Data
public class HomeVO {

    /**
     * 轮播图
     */
    private List<Banner> banners;

    /**
     * 热销商品
     */
    private List<Product> hotProducts;

    /**
     * 新品推荐
     */
    private List<Product> newProducts;
}
