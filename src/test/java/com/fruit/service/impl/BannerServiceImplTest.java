package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.common.exception.BusinessException;
import com.fruit.common.result.PageResult;
import com.fruit.common.result.ResultCode;
import com.fruit.dto.HomeActivityDTO;
import com.fruit.dto.BannerDTO;
import com.fruit.entity.Banner;
import com.fruit.enums.StatusEnum;
import com.fruit.mapper.BannerMapper;
import com.fruit.vo.HomeActivityVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BannerServiceImplTest {

    @Mock
    private BannerMapper bannerMapper;

    @InjectMocks
    private BannerServiceImpl bannerService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(bannerService, "baseMapper", bannerMapper);
    }

    @Test
    void getHomeBanners_Success() {
        Banner banner1 = new Banner();
        banner1.setId(1L);
        Banner banner2 = new Banner();
        banner2.setId(2L);
        List<Banner> banners = Arrays.asList(banner1, banner2);

        when(bannerMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(banners);

        List<Banner> result = bannerService.getHomeBanners();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(bannerMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void getHomeActivities_ReturnsFixedSlotsWithDefaults() {
        when(bannerMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList());

        List<HomeActivityVO> result = bannerService.getHomeActivitiesForAdmin();

        assertEquals(3, result.size());
        assertEquals("DAILY_NEW", result.get(0).getCode());
        assertEquals("NEW_TASTE", result.get(1).getCode());
        assertEquals("FULL_REDUCTION", result.get(2).getCode());
        assertEquals("临期特惠", result.get(1).getTitle());
        assertEquals("热销榜单", result.get(2).getTitle());
        assertEquals("/activity/fresh-new", result.get(0).getLinkUrl());
        assertEquals("/activity/hot-sales", result.get(2).getLinkUrl());
        assertTrue(result.stream().allMatch(activity -> activity.getImageUrl() != null && !activity.getImageUrl().isEmpty()));
    }

    @Test
    void updateHomeActivity_UpsertsFixedSlot() {
        HomeActivityDTO dto = new HomeActivityDTO();
        dto.setImageUrl("/uploads/images/04c8c5a01c1d4dafafc0acb32822dfcf.jpg");
        dto.setLinkUrl("/category?tag=new");
        dto.setStatus(1);
        when(bannerMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(bannerMapper.insert(any(Banner.class))).thenReturn(1);

        bannerService.updateHomeActivity("DAILY_NEW", dto);

        verify(bannerMapper).insert(argThat(banner ->
                Integer.valueOf(101).equals(banner.getLinkType()) &&
                        "新鲜到店".equals(banner.getTitle()) &&
                        dto.getImageUrl().equals(banner.getImageUrl())));
    }

    @Test
    void listForAdmin_Success() {
        Banner banner = new Banner();
        banner.setId(1L);
        Page<Banner> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(banner));
        page.setTotal(1);

        when(bannerMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        PageResult<Banner> result = bannerService.listForAdmin(1, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
        verify(bannerMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void addBanner_Success() {
        BannerDTO dto = new BannerDTO();
        dto.setTitle("Test Banner");
        dto.setImageUrl("http://test.com/image.jpg");

        when(bannerMapper.insert(any(Banner.class))).thenReturn(1);

        bannerService.addBanner(dto);

        verify(bannerMapper, times(1)).insert(argThat(banner ->
            "Test Banner".equals(banner.getTitle()) &&
            StatusEnum.ENABLED.getCode().equals(banner.getStatus())
        ));
    }

    @Test
    void updateBanner_Success() {
        Long id = 1L;
        BannerDTO dto = new BannerDTO();
        dto.setTitle("Updated Banner");

        Banner existingBanner = new Banner();
        existingBanner.setId(id);
        existingBanner.setTitle("Old Banner");

        when(bannerMapper.selectById(id)).thenReturn(existingBanner);
        when(bannerMapper.updateById(any(Banner.class))).thenReturn(1);

        bannerService.updateBanner(id, dto);

        verify(bannerMapper, times(1)).selectById(id);
        verify(bannerMapper, times(1)).updateById(argThat(banner ->
            id.equals(banner.getId()) &&
            "Updated Banner".equals(banner.getTitle())
        ));
    }

    @Test
    void updateBanner_NotExist_ThrowsException() {
        Long id = 1L;
        BannerDTO dto = new BannerDTO();

        when(bannerMapper.selectById(id)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> bannerService.updateBanner(id, dto));
        assertEquals(ResultCode.BANNER_NOT_EXIST.getCode(), exception.getCode());
        verify(bannerMapper, times(1)).selectById(id);
        verify(bannerMapper, never()).updateById(any(Banner.class));
    }

    @Test
    void deleteBanner_Success() {
        Long id = 1L;

        when(bannerMapper.deleteById(id)).thenReturn(1);

        bannerService.deleteBanner(id);

        verify(bannerMapper, times(1)).deleteById(id);
    }

    @Test
    void updateStatus_Success() {
        Long id = 1L;
        Integer status = StatusEnum.DISABLED.getCode();

        Banner existingBanner = new Banner();
        existingBanner.setId(id);
        existingBanner.setStatus(StatusEnum.ENABLED.getCode());

        when(bannerMapper.selectById(id)).thenReturn(existingBanner);
        when(bannerMapper.updateById(any(Banner.class))).thenReturn(1);

        bannerService.updateStatus(id, status);

        verify(bannerMapper, times(1)).selectById(id);
        verify(bannerMapper, times(1)).updateById(argThat(banner ->
            id.equals(banner.getId()) &&
            status.equals(banner.getStatus())
        ));
    }

    @Test
    void updateStatus_NotExist_ThrowsException() {
        Long id = 1L;
        Integer status = StatusEnum.DISABLED.getCode();

        when(bannerMapper.selectById(id)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> bannerService.updateStatus(id, status));
        assertEquals(ResultCode.BANNER_NOT_EXIST.getCode(), exception.getCode());
        verify(bannerMapper, times(1)).selectById(id);
        verify(bannerMapper, never()).updateById(any(Banner.class));
    }
}
