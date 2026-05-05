package com.fruit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fruit.entity.ShopFollow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 关注店铺Mapper
 */
@Mapper
public interface ShopFollowMapper extends BaseMapper<ShopFollow> {

    /**
     * 查找已软删除的关注记录（绕过@TableLogic自动过滤）
     */
    @Select("SELECT * FROM shop_follow WHERE user_id = #{userId} AND shop_id = #{shopId} AND deleted = 1 LIMIT 1")
    ShopFollow selectDeletedByUserAndShop(@Param("userId") Long userId, @Param("shopId") Long shopId);

    /**
     * 恢复已软删除的记录（绕过@TableLogic自动过滤）
     */
    @Update("UPDATE shop_follow SET deleted = 0 WHERE id = #{id}")
    int restoreById(@Param("id") Long id);
}
