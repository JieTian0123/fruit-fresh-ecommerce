package com.fruit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fruit.entity.UserCoupon;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户优惠券Mapper
 */
@Mapper
public interface UserCouponMapper extends BaseMapper<UserCoupon> {
}
