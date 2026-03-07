package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruit.common.exception.BusinessException;
import com.fruit.common.result.ResultCode;
import com.fruit.dto.AddressDTO;
import com.fruit.entity.Address;
import com.fruit.mapper.AddressMapper;
import com.fruit.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 地址服务实现类
 */
@Service
@RequiredArgsConstructor
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {

    @Override
    public List<Address> getAddressList(Long userId) {
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, userId);
        wrapper.orderByDesc(Address::getIsDefault);
        wrapper.orderByDesc(Address::getCreateTime);

        return baseMapper.selectList(wrapper);
    }

    @Override
    public void addAddress(Long userId, AddressDTO dto) {
        Address address = new Address();
        BeanUtils.copyProperties(dto, address);
        address.setUserId(userId);

        // 如果是第一个地址，设为默认
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, userId);
        Long count = baseMapper.selectCount(wrapper);

        if (count == 0) {
            address.setIsDefault(1);
        } else {
            address.setIsDefault(0);
        }

        // 如果设置为默认，先取消其他默认地址
        if (dto.getIsDefault() != null && dto.getIsDefault() == 1) {
            cancelOtherDefault(userId);
            address.setIsDefault(1);
        }

        baseMapper.insert(address);
    }

    @Override
    public void updateAddress(Long userId, Long addressId, AddressDTO dto) {
        Address address = baseMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ADDRESS_NOT_EXIST);
        }

        BeanUtils.copyProperties(dto, address);
        address.setId(addressId);

        // 如果设置为默认，先取消其他默认地址
        if (dto.getIsDefault() != null && dto.getIsDefault() == 1) {
            cancelOtherDefault(userId);
        }

        baseMapper.updateById(address);
    }

    @Override
    public void deleteAddress(Long userId, Long addressId) {
        Address address = baseMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ADDRESS_NOT_EXIST);
        }

        baseMapper.deleteById(addressId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long userId, Long addressId) {
        Address address = baseMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ADDRESS_NOT_EXIST);
        }

        // 取消其他默认地址
        cancelOtherDefault(userId);

        // 设置当前地址为默认
        address.setIsDefault(1);
        baseMapper.updateById(address);
    }

    @Override
    public Address getDefaultAddress(Long userId) {
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, userId);
        wrapper.eq(Address::getIsDefault, 1);

        return baseMapper.selectOne(wrapper);
    }

    /**
     * 取消其他默认地址
     */
    private void cancelOtherDefault(Long userId) {
        LambdaUpdateWrapper<Address> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Address::getUserId, userId);
        wrapper.set(Address::getIsDefault, 0);
        baseMapper.update(null, wrapper);
    }
}
