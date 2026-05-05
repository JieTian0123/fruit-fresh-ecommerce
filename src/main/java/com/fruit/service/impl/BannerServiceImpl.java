package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruit.common.exception.BusinessException;
import com.fruit.common.result.PageResult;
import com.fruit.common.result.ResultCode;
import com.fruit.dto.BannerDTO;
import com.fruit.dto.HomeActivityDTO;
import com.fruit.entity.Banner;
import com.fruit.enums.StatusEnum;
import com.fruit.mapper.BannerMapper;
import com.fruit.service.BannerService;
import com.fruit.vo.HomeActivityVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 轮播图服务实现类
 */
@Service
@RequiredArgsConstructor
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {

    private static final Map<String, ActivitySlot> ACTIVITY_SLOTS = new LinkedHashMap<>();

    static {
        ACTIVITY_SLOTS.put("DAILY_NEW", new ActivitySlot(
                "DAILY_NEW",
                101,
                "新鲜到店",
                "刚采摘入库生鲜",
                "新鲜优选",
                "看新鲜",
                "/activity/fresh-new.svg",
                "/activity/fresh-new",
                "daily",
                1));
        ACTIVITY_SLOTS.put("NEW_TASTE", new ActivitySlot(
                "NEW_TASTE",
                102,
                "临期特惠",
                "保质期过半低价购",
                "动态降价",
                "看特惠",
                "/activity/near-expiry.svg",
                "/activity/near-expiry",
                "deal",
                2));
        ACTIVITY_SLOTS.put("FULL_REDUCTION", new ActivitySlot(
                "FULL_REDUCTION",
                103,
                "热销榜单",
                "平台高销量生鲜",
                "人气热卖",
                "看热销",
                "/activity/hot-sales.svg",
                "/activity/hot-sales",
                "hot",
                3));
    }

    @Override
    public List<Banner> getHomeBanners() {
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Banner::getStatus, StatusEnum.ENABLED.getCode());
        wrapper.notIn(Banner::getLinkType, getActivityLinkTypes());
        wrapper.orderByAsc(Banner::getSort);

        return baseMapper.selectList(wrapper);
    }

    @Override
    public List<HomeActivityVO> getEnabledHomeActivities() {
        return getHomeActivitiesForAdmin().stream()
                .filter(activity -> StatusEnum.ENABLED.getCode().equals(activity.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public List<HomeActivityVO> getHomeActivitiesForAdmin() {
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Banner::getLinkType, getActivityLinkTypes());
        List<Banner> banners = baseMapper.selectList(wrapper);

        Map<Integer, Banner> bannerMap = banners.stream()
                .collect(Collectors.toMap(Banner::getLinkType, banner -> banner, (left, right) -> left));

        List<HomeActivityVO> result = new ArrayList<>();
        for (ActivitySlot slot : ACTIVITY_SLOTS.values()) {
            result.add(toHomeActivityVO(slot, bannerMap.get(slot.linkType)));
        }
        return result;
    }

    @Override
    public void updateHomeActivity(String code, HomeActivityDTO dto) {
        ActivitySlot slot = ACTIVITY_SLOTS.get(code);
        if (slot == null) {
            throw new BusinessException("首页活动不存在");
        }

        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Banner::getLinkType, slot.linkType);
        Banner banner = baseMapper.selectOne(wrapper);
        if (banner == null) {
            banner = new Banner();
            banner.setTitle(slot.title);
            banner.setImageUrl(slot.defaultImageUrl);
            banner.setLinkUrl(slot.defaultLinkUrl);
            banner.setLinkType(slot.linkType);
            banner.setSort(slot.sort);
            banner.setStatus(StatusEnum.ENABLED.getCode());
        }

        if (dto.getImageUrl() != null && !dto.getImageUrl().trim().isEmpty()) {
            banner.setImageUrl(dto.getImageUrl().trim());
        }
        if (dto.getLinkUrl() != null) {
            banner.setLinkUrl(dto.getLinkUrl().trim());
        }
        if (dto.getStatus() != null) {
            banner.setStatus(dto.getStatus());
        }
        banner.setTitle(slot.title);
        banner.setLinkType(slot.linkType);
        banner.setSort(slot.sort);

        if (banner.getId() == null) {
            baseMapper.insert(banner);
        } else {
            baseMapper.updateById(banner);
        }
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

    private List<Integer> getActivityLinkTypes() {
        return ACTIVITY_SLOTS.values().stream()
                .map(slot -> slot.linkType)
                .collect(Collectors.toList());
    }

    private HomeActivityVO toHomeActivityVO(ActivitySlot slot, Banner banner) {
        HomeActivityVO vo = new HomeActivityVO();
        vo.setCode(slot.code);
        vo.setTitle(slot.title);
        vo.setSubtitle(slot.subtitle);
        vo.setBadge(slot.badge);
        vo.setActionText(slot.actionText);
        vo.setTheme(slot.theme);
        vo.setSort(slot.sort);

        if (banner == null) {
            vo.setImageUrl(slot.defaultImageUrl);
            vo.setLinkUrl(slot.defaultLinkUrl);
            vo.setStatus(StatusEnum.ENABLED.getCode());
            return vo;
        }

        vo.setId(banner.getId());
        vo.setImageUrl(normalizeActivityImage(slot, banner.getImageUrl()));
        vo.setLinkUrl(normalizeActivityLink(slot, banner.getLinkUrl()));
        vo.setStatus(banner.getStatus());
        vo.setCreateTime(banner.getCreateTime());
        return vo;
    }

    private String normalizeActivityImage(ActivitySlot slot, String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return slot.defaultImageUrl;
        }
        if ("DAILY_NEW".equals(slot.code) && imageUrl.contains("daily-new")) {
            return slot.defaultImageUrl;
        }
        if ("NEW_TASTE".equals(slot.code) && imageUrl.contains("seasonal-fresh")) {
            return slot.defaultImageUrl;
        }
        if ("FULL_REDUCTION".equals(slot.code) && (imageUrl.contains("full-reduction") || imageUrl.contains("fresh-deal"))) {
            return slot.defaultImageUrl;
        }
        return imageUrl;
    }

    private String normalizeActivityLink(ActivitySlot slot, String linkUrl) {
        if (linkUrl == null || linkUrl.trim().isEmpty()) {
            return slot.defaultLinkUrl;
        }
        if ("DAILY_NEW".equals(slot.code) && (linkUrl.contains("/category") || linkUrl.contains("/daily-new"))) {
            return slot.defaultLinkUrl;
        }
        if ("NEW_TASTE".equals(slot.code) && (linkUrl.contains("/category") || linkUrl.contains("/seasonal-fresh"))) {
            return slot.defaultLinkUrl;
        }
        if ("FULL_REDUCTION".equals(slot.code) && (linkUrl.contains("/coupons") || linkUrl.contains("/fresh-deal"))) {
            return slot.defaultLinkUrl;
        }
        return linkUrl;
    }

    private static class ActivitySlot {
        private final String code;
        private final Integer linkType;
        private final String title;
        private final String subtitle;
        private final String badge;
        private final String actionText;
        private final String defaultImageUrl;
        private final String defaultLinkUrl;
        private final String theme;
        private final Integer sort;

        private ActivitySlot(String code, Integer linkType, String title, String subtitle, String badge,
                             String actionText, String defaultImageUrl, String defaultLinkUrl,
                             String theme, Integer sort) {
            this.code = code;
            this.linkType = linkType;
            this.title = title;
            this.subtitle = subtitle;
            this.badge = badge;
            this.actionText = actionText;
            this.defaultImageUrl = defaultImageUrl;
            this.defaultLinkUrl = defaultLinkUrl;
            this.theme = theme;
            this.sort = sort;
        }
    }
}
