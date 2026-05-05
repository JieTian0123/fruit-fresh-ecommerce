package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.common.exception.BusinessException;
import com.fruit.common.result.PageResult;
import com.fruit.common.result.ResultCode;
import com.fruit.dto.ShopDTO;
import com.fruit.entity.MerchantShop;
import com.fruit.enums.StatusEnum;
import com.fruit.mapper.MerchantShopMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MerchantShopServiceImplTest {

    @Mock
    private MerchantShopMapper merchantShopMapper;

    @InjectMocks
    private MerchantShopServiceImpl merchantShopService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(merchantShopService, "baseMapper", merchantShopMapper);
    }

    @Test
    void getShopByMerchantId_Success() {
        Long merchantId = 1L;
        MerchantShop shop = new MerchantShop();
        shop.setMerchantId(merchantId);

        when(merchantShopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);

        MerchantShop result = merchantShopService.getShopByMerchantId(merchantId);

        assertNotNull(result);
        assertEquals(merchantId, result.getMerchantId());
        verify(merchantShopMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    void createShop_Success() {
        Long merchantId = 1L;
        ShopDTO dto = new ShopDTO();
        dto.setShopName("Test Shop");

        when(merchantShopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(merchantShopMapper.insert(any(MerchantShop.class))).thenReturn(1);

        merchantShopService.createShop(merchantId, dto);

        verify(merchantShopMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
        verify(merchantShopMapper, times(1)).insert(argThat(shop ->
            merchantId.equals(shop.getMerchantId()) &&
            "Test Shop".equals(shop.getShopName()) &&
            StatusEnum.PENDING.getCode().equals(shop.getStatus())
        ));
    }

    @Test
    void createShop_AlreadyExist_ThrowsException() {
        Long merchantId = 1L;
        ShopDTO dto = new ShopDTO();

        MerchantShop existingShop = new MerchantShop();
        existingShop.setMerchantId(merchantId);

        when(merchantShopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingShop);

        BusinessException exception = assertThrows(BusinessException.class, () -> merchantShopService.createShop(merchantId, dto));
        assertEquals(ResultCode.SHOP_ALREADY_EXIST.getCode(), exception.getCode());
        verify(merchantShopMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
        verify(merchantShopMapper, never()).insert(any(MerchantShop.class));
    }

    @Test
    void updateShop_Success() {
        Long merchantId = 1L;
        ShopDTO dto = new ShopDTO();
        dto.setShopName("Updated Shop");

        MerchantShop existingShop = new MerchantShop();
        existingShop.setId(100L);
        existingShop.setMerchantId(merchantId);
        existingShop.setShopName("Old Shop");

        when(merchantShopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingShop);
        when(merchantShopMapper.updateById(any(MerchantShop.class))).thenReturn(1);

        merchantShopService.updateShop(merchantId, dto);

        verify(merchantShopMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
        verify(merchantShopMapper, times(1)).updateById(argThat(shop ->
            Long.valueOf(100L).equals(shop.getId()) &&
            "Updated Shop".equals(shop.getShopName())
        ));
    }

    @Test
    void updateShop_NotExist_ThrowsException() {
        Long merchantId = 1L;
        ShopDTO dto = new ShopDTO();

        when(merchantShopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> merchantShopService.updateShop(merchantId, dto));
        assertEquals(ResultCode.SHOP_NOT_EXIST.getCode(), exception.getCode());
        verify(merchantShopMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
        verify(merchantShopMapper, never()).updateById(any(MerchantShop.class));
    }

    @Test
    void listForAdmin_Success() {
        MerchantShop shop = new MerchantShop();
        shop.setId(1L);
        Page<MerchantShop> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(shop));
        page.setTotal(1);

        when(merchantShopMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        PageResult<MerchantShop> result = merchantShopService.listForAdmin(StatusEnum.PENDING.getCode(), 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
        verify(merchantShopMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void approveShop_Success() {
        Long shopId = 1L;
        Integer status = StatusEnum.ENABLED.getCode();

        MerchantShop existingShop = new MerchantShop();
        existingShop.setId(shopId);
        existingShop.setStatus(StatusEnum.PENDING.getCode());

        when(merchantShopMapper.selectById(shopId)).thenReturn(existingShop);
        when(merchantShopMapper.updateById(any(MerchantShop.class))).thenReturn(1);

        merchantShopService.approveShop(shopId, status);

        verify(merchantShopMapper, times(1)).selectById(shopId);
        verify(merchantShopMapper, times(1)).updateById(argThat(shop ->
            shopId.equals(shop.getId()) &&
            status.equals(shop.getStatus())
        ));
    }

    @Test
    void approveShop_NotExist_ThrowsException() {
        Long shopId = 1L;
        Integer status = StatusEnum.ENABLED.getCode();

        when(merchantShopMapper.selectById(shopId)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> merchantShopService.approveShop(shopId, status));
        assertEquals(ResultCode.SHOP_NOT_EXIST.getCode(), exception.getCode());
        verify(merchantShopMapper, times(1)).selectById(shopId);
        verify(merchantShopMapper, never()).updateById(any(MerchantShop.class));
    }
}
