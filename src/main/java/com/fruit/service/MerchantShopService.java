package com.fruit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruit.common.result.PageResult;
import com.fruit.dto.ShopDTO;
import com.fruit.entity.MerchantShop;

/**
 * 店铺服务接口
 */
public interface MerchantShopService extends IService<MerchantShop> {

    /**
     * 获取商家店铺信息
     */
    MerchantShop getShopByMerchantId(Long merchantId);

    /**
     * 创建店铺
     */
    void createShop(Long merchantId, ShopDTO dto);

    /**
     * 修改店铺信息
     */
    void updateShop(Long merchantId, ShopDTO dto);

    /**
     * 管理员店铺列表
     */
    PageResult<MerchantShop> listForAdmin(Integer status, Integer pageNum, Integer pageSize);

    /**
     * 审核店铺
     */
    void approveShop(Long shopId, Integer status);
}
