package com.fruit.service;

import com.fruit.entity.MemberLevel;

import java.util.List;

/**
 * 会员服务接口
 */
public interface MemberService {

    /**
     * 检查并升级会员等级
     */
    void checkAndUpgradeLevel(Long userId);

    /**
     * 获取所有会员等级列表
     */
    List<MemberLevel> getAllLevels();

    /**
     * 获取用户会员等级信息
     */
    MemberLevel getUserLevel(Long userId);
}
