package com.fruit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.entity.VipOrder;
import com.fruit.entity.VipPlan;

import java.util.List;
import java.util.Map;

/**
 * VIP会员服务接口
 */
public interface VipService {

    /**
     * 获取VIP状态信息
     * 返回: isVip, expireTime, completedOrders, upgradeThreshold
     */
    Map<String, Object> getVipStatus(Long userId);

    /**
     * 判断用户是否为VIP
     */
    boolean isVip(Long userId);

    /**
     * 获取可用的VIP套餐列表
     */
    List<VipPlan> getAvailablePlans();

    /**
     * 购买VIP（模拟支付，立即生效）
     */
    VipOrder purchaseVip(Long userId, Long planId);

    /**
     * 检查并通过订单数自动升级VIP
     * 在确认收货时调用
     */
    void checkAndUpgradeByOrders(Long userId);

    /**
     * 每周VIP优惠券发放
     */
    void distributeWeeklyVipCoupons();

    // ============ 管理员接口 ============

    /**
     * 管理员获取所有VIP套餐(含禁用)
     */
    Page<VipPlan> listAllPlans(Integer pageNum, Integer pageSize);

    /**
     * 管理员新增VIP套餐
     */
    void addPlan(VipPlan plan);

    /**
     * 管理员更新VIP套餐
     */
    void updatePlan(VipPlan plan);

    /**
     * 管理员删除VIP套餐
     */
    void deletePlan(Long id);

    /**
     * 管理员更新套餐状态
     */
    void updatePlanStatus(Long id, Integer status);

    /**
     * 管理员获取VIP用户列表
     */
    Page<Map<String, Object>> listVipUsers(Integer pageNum, Integer pageSize);

    /**
     * 管理员获取VIP订单列表
     */
    Page<VipOrder> listVipOrders(Integer pageNum, Integer pageSize);
}
