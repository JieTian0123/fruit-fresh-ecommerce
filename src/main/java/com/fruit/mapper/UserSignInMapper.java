package com.fruit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fruit.entity.UserSignIn;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户签到Mapper
 */
@Mapper
public interface UserSignInMapper extends BaseMapper<UserSignIn> {
}
