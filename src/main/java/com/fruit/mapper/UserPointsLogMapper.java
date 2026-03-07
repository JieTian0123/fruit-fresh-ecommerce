package com.fruit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fruit.entity.UserPointsLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户积分记录Mapper
 */
@Mapper
public interface UserPointsLogMapper extends BaseMapper<UserPointsLog> {
}
