package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruit.common.exception.BusinessException;
import com.fruit.common.result.PageResult;
import com.fruit.common.result.ResultCode;
import com.fruit.dto.BannerDTO;
import com.fruit.entity.Banner;
import com.fruit.enums.StatusEnum;
import com.fruit.mapper.BannerMapper;
import com.fruit.service.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 轮播图服务实现类
 */
@Service
@RequiredArgsConstructor
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {

    @Override
    public List<Banner> getHomeBanners() {
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Banner::getStatus, StatusEnum.ENABLED.getCode());
        wrapper.orderByAsc(Banner::getSort);

        return baseMapper.selectList(wrapper);
    }

    @Override
    public PageResult<Banner> listForAdmin(Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Banner::getSort);
        wrapper.orderByDesc(Banner::getCreateTime);

        Page<Banner> page = new Page<>(pageNum, pageSize);
        Page<Banner> result = baseMapper.selectPage(page, wrapper);

        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    public void addBanner(BannerDTO dto) {
        Banner banner = new Banner();
        BeanUtils.copyProperties(dto, banner);
        banner.setStatus(StatusEnum.ENABLED.getCode());

        baseMapper.insert(banner);
    }

    @Override
    public void updateBanner(Long id, BannerDTO dto) {
        Banner banner = baseMapper.selectById(id);
        if (banner == null) {
            throw new BusinessException(ResultCode.BANNER_NOT_EXIST);
        }

        BeanUtils.copyProperties(dto, banner);
        banner.setId(id);

        baseMapper.updateById(banner);
    }

    @Override
    public void deleteBanner(Long id) {
        baseMapper.deleteById(id);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Banner banner = baseMapper.selectById(id);
        if (banner == null) {
            throw new BusinessException(ResultCode.BANNER_NOT_EXIST);
        }

        banner.setStatus(status);
        baseMapper.updateById(banner);
    }
}
