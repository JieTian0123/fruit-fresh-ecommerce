package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import com.fruit.common.exception.BusinessException;
import com.fruit.common.result.ResultCode;
import com.fruit.dto.AddressDTO;
import com.fruit.entity.Address;
import com.fruit.mapper.AddressMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
@DisplayName("地址服务测试")
class AddressServiceImplTest {

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private AddressServiceImpl addressService;

    @BeforeAll
    static void initMyBatisPlus() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), Address.class);
    }

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(addressService, "baseMapper", addressMapper);
    }

    @Test
    @DisplayName("获取地址列表")
    void getAddressList() {
        Long userId = 1L;
        List<Address> expectedList = Arrays.asList(new Address(), new Address());
        when(addressMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(expectedList);

        List<Address> result = addressService.getAddressList(userId);

        assertEquals(expectedList, result);
        verify(addressMapper).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("添加地址 - 首次添加自动设为默认")
    void addAddress_FirstAddressAutoDefault() {
        Long userId = 1L;
        AddressDTO dto = new AddressDTO();
        dto.setReceiverName("张三");

        when(addressMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        addressService.addAddress(userId, dto);

        verify(addressMapper).insert(argThat(address ->
            address.getUserId().equals(userId) &&
            address.getIsDefault() == 1 &&
            "张三".equals(address.getReceiverName())
        ));
    }

    @Test
    @DisplayName("添加地址 - 设为默认")
    void addAddress_SetAsDefault() {
        Long userId = 1L;
        AddressDTO dto = new AddressDTO();
        dto.setReceiverName("李四");
        dto.setIsDefault(1);

        when(addressMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        addressService.addAddress(userId, dto);

        verify(addressMapper).update(isNull(), any(LambdaUpdateWrapper.class));
        verify(addressMapper).insert(argThat(address ->
            address.getUserId().equals(userId) &&
            address.getIsDefault() == 1 &&
            "李四".equals(address.getReceiverName())
        ));
    }

    @Test
    @DisplayName("更新地址 - 成功")
    void updateAddress_Success() {
        Long userId = 1L;
        Long addressId = 100L;
        AddressDTO dto = new AddressDTO();
        dto.setReceiverName("王五");
        dto.setIsDefault(1);

        Address existingAddress = new Address();
        existingAddress.setId(addressId);
        existingAddress.setUserId(userId);

        when(addressMapper.selectById(addressId)).thenReturn(existingAddress);

        addressService.updateAddress(userId, addressId, dto);

        verify(addressMapper).update(isNull(), any(LambdaUpdateWrapper.class));
        verify(addressMapper).updateById(argThat(address ->
            address.getId().equals(addressId) &&
            "王五".equals(address.getReceiverName())
        ));
    }

    @Test
    @DisplayName("更新地址 - 地址不存在或不属于该用户")
    void updateAddress_NotFound() {
        Long userId = 1L;
        Long addressId = 100L;
        AddressDTO dto = new AddressDTO();

        when(addressMapper.selectById(addressId)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () ->
            addressService.updateAddress(userId, addressId, dto)
        );
        assertEquals(ResultCode.ADDRESS_NOT_EXIST.getMessage(), exception.getMessage());

        Address otherUserAddress = new Address();
        otherUserAddress.setUserId(2L);
        when(addressMapper.selectById(addressId)).thenReturn(otherUserAddress);

        exception = assertThrows(BusinessException.class, () ->
            addressService.updateAddress(userId, addressId, dto)
        );
        assertEquals(ResultCode.ADDRESS_NOT_EXIST.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("删除地址 - 成功")
    void deleteAddress_Success() {
        Long userId = 1L;
        Long addressId = 100L;

        Address existingAddress = new Address();
        existingAddress.setId(addressId);
        existingAddress.setUserId(userId);

        when(addressMapper.selectById(addressId)).thenReturn(existingAddress);

        addressService.deleteAddress(userId, addressId);

        verify(addressMapper).deleteById(addressId);
    }

    @Test
    @DisplayName("删除地址 - 地址不存在或不属于该用户")
    void deleteAddress_NotFound() {
        Long userId = 1L;
        Long addressId = 100L;

        when(addressMapper.selectById(addressId)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () ->
            addressService.deleteAddress(userId, addressId)
        );
        assertEquals(ResultCode.ADDRESS_NOT_EXIST.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("设置默认地址")
    void setDefault() {
        Long userId = 1L;
        Long addressId = 100L;

        Address existingAddress = new Address();
        existingAddress.setId(addressId);
        existingAddress.setUserId(userId);

        when(addressMapper.selectById(addressId)).thenReturn(existingAddress);

        addressService.setDefault(userId, addressId);

        verify(addressMapper).update(isNull(), any(LambdaUpdateWrapper.class));
        verify(addressMapper).updateById(argThat(address ->
            address.getId().equals(addressId) &&
            address.getIsDefault() == 1
        ));
    }

    @Test
    @DisplayName("获取默认地址")
    void getDefaultAddress() {
        Long userId = 1L;
        Address expectedAddress = new Address();

        when(addressMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(expectedAddress);

        Address result = addressService.getDefaultAddress(userId);

        assertEquals(expectedAddress, result);
        verify(addressMapper).selectOne(any(LambdaQueryWrapper.class));
    }
}
