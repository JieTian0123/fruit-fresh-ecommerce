package com.fruit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruit.common.result.PageResult;
import com.fruit.dto.BannerDTO;
import com.fruit.entity.Banner;

import java.util.List;

/**
 * 轮播图服务接口
 */
public interface BannerService extends IService<Banner> {

    /**
     * 获取首页轮播图
     */
    List<Banner> getHomeBanners();

    /**
     * 管理员轮播图列表
     */
    PageResult<Banner> listForAdmin(Integer pageNum, Integer pageSize);

    /**
     * 添加轮播图
     */
    void addBanner(BannerDTO dto);

    /**
     * 修改轮播图
     */
    void updateBanner(Long id, BannerDTO dto);

    /**
     * 删除轮播图
     */
    void deleteBanner(Long id);

    /**
     * 修改轮播图状态
     */
    void updateStatus(Long id, Integer status);
}
