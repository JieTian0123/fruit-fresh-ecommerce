package com.fruit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fruit.entity.VipOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * VIP订单Mapper
 */
@Mapper
public interface VipOrderMapper extends BaseMapper<VipOrder> {

    /**
     * 统计用户已完成的订单数(status=3, 已确认收货, 未删除)
     */
    @Select("SELECT COUNT(*) FROM `order` WHERE user_id = #{userId} AND status = 3 AND deleted = 0")
    int countCompletedOrders(@Param("userId") Long userId);
}
