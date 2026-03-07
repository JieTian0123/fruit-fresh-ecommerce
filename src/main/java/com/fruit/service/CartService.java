package com.fruit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruit.entity.Cart;
import com.fruit.vo.CartVO;

import java.util.List;

/**
 * 购物车服务接口
 */
public interface CartService extends IService<Cart> {

    /**
     * 获取购物车列表
     */
    List<CartVO> getCartList(Long userId);

    /**
     * 添加购物车
     */
    void addCart(Long userId, Long productId, Integer quantity);

    /**
     * 修改购物车数量
     */
    void updateQuantity(Long userId, Long cartId, Integer quantity);

    /**
     * 删除购物车
     */
    void deleteCart(Long userId, Long cartId);

    /**
     * 清空购物车
     */
    void clearCart(Long userId);

    /**
     * 选中/取消选中购物车商品
     */
    void updateSelected(Long userId, Long cartId, Integer selected);

    /**
     * 全选/取消全选
     */
    void selectAll(Long userId, Integer selected);
}
