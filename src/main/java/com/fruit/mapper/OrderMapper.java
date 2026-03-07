package com.fruit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fruit.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 订单Mapper
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 查询商家的会员客户统计信息
     * 返回: user_id, order_count, total_spend, last_order_time
     */
    @Select("SELECT o.user_id, COUNT(*) AS order_count, " +
            "SUM(o.pay_amount) AS total_spend, MAX(o.create_time) AS last_order_time " +
            "FROM `order` o " +
            "WHERE o.merchant_id = #{merchantId} AND o.deleted = 0 AND o.status >= 1 " +
            "GROUP BY o.user_id " +
            "ORDER BY total_spend DESC " +
            "LIMIT #{offset}, #{size}")
    List<Map<String, Object>> selectMemberCustomers(@Param("merchantId") Long merchantId,
                                                     @Param("offset") Integer offset,
                                                     @Param("size") Integer size);

    /**
     * 查询商家有多少个不同的客户
     */
    @Select("SELECT COUNT(DISTINCT o.user_id) FROM `order` o " +
            "WHERE o.merchant_id = #{merchantId} AND o.deleted = 0 AND o.status >= 1")
    Long countDistinctCustomers(@Param("merchantId") Long merchantId);
}
