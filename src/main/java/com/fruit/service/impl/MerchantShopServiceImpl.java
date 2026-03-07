package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruit.common.exception.BusinessException;
import com.fruit.common.result.PageResult;
import com.fruit.common.result.ResultCode;
import com.fruit.dto.ShopDTO;
import com.fruit.entity.MerchantShop;
import com.fruit.enums.StatusEnum;
import com.fruit.mapper.MerchantShopMapper;
import com.fruit.service.MerchantShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 店铺服务实现类
 */
@Service
@RequiredArgsConstructor
public class MerchantShopServiceImpl extends ServiceImpl<MerchantShopMapper, MerchantShop> implements MerchantShopService {

    @Override
    public MerchantShop getShopByMerchantId(Long merchantId) {
        LambdaQueryWrapper<MerchantShop> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MerchantShop::getMerchantId, merchantId);

        return baseMapper.selectOne(wrapper);
    }

    @Override
    public void createShop(Long merchantId, ShopDTO dto) {
        // 检查是否已有店铺
        MerchantShop existShop = getShopByMerchantId(merchantId);
        if (existShop != null) {
            throw new BusinessException(ResultCode.SHOP_ALREADY_EXIST);
        }

        MerchantShop shop = new MerchantShop();
        BeanUtils.copyProperties(dto, shop);
        shop.setMerchantId(merchantId);
        shop.setStatus(StatusEnum.PENDING.getCode()); // 待审核

        baseMapper.insert(shop);
    }

    @Override
    public void updateShop(Long merchantId, ShopDTO dto) {
        MerchantShop shop = getShopByMerchantId(merchantId);
        if (shop == null) {
            throw new BusinessException(ResultCode.SHOP_NOT_EXIST);
        }

        BeanUtils.copyProperties(dto, shop);
        baseMapper.updateById(shop);
    }

    @Override
    public PageResult<MerchantShop> listForAdmin(Integer status, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<MerchantShop> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(MerchantShop::getStatus, status);
        }

        wrapper.orderByDesc(MerchantShop::getCreateTime);

        Page<MerchantShop> page = new Page<>(pageNum, pageSize);
        Page<MerchantShop> result = baseMapper.selectPage(page, wrapper);

        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    public void approveShop(Long shopId, Integer status) {
        MerchantShop shop = baseMapper.selectById(shopId);
        if (shop == null) {
            throw new BusinessException(ResultCode.SHOP_NOT_EXIST);
        }

        shop.setStatus(status);
        baseMapper.updateById(shop);
    }
}
