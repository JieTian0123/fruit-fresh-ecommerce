package com.fruit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruit.dto.AddressDTO;
import com.fruit.entity.Address;

import java.util.List;

/**
 * 地址服务接口
 */
public interface AddressService extends IService<Address> {

    /**
     * 获取地址列表
     */
    List<Address> getAddressList(Long userId);

    /**
     * 添加地址
     */
    void addAddress(Long userId, AddressDTO dto);

    /**
     * 修改地址
     */
    void updateAddress(Long userId, Long addressId, AddressDTO dto);

    /**
     * 删除地址
     */
    void deleteAddress(Long userId, Long addressId);

    /**
     * 设置默认地址
     */
    void setDefault(Long userId, Long addressId);

    /**
     * 获取默认地址
     */
    Address getDefaultAddress(Long userId);
}
