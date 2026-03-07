package com.fruit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fruit.entity.Address;
import org.apache.ibatis.annotations.Mapper;

/**
 * 地址Mapper
 */
@Mapper
public interface AddressMapper extends BaseMapper<Address> {
}
