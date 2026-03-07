package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruit.common.exception.BusinessException;
import com.fruit.common.result.ResultCode;
import com.fruit.entity.Cart;
import com.fruit.entity.Product;
import com.fruit.mapper.CartMapper;
import com.fruit.mapper.ProductMapper;
import com.fruit.service.CartService;
import com.fruit.vo.CartVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 购物车服务实现类
 */
@Service
@RequiredArgsConstructor
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    private final ProductMapper productMapper;

    @Override
    public List<CartVO> getCartList(Long userId) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        wrapper.orderByDesc(Cart::getCreateTime);

        List<Cart> carts = baseMapper.selectList(wrapper);

        List<CartVO> voList = new ArrayList<>();
        for (Cart cart : carts) {
            CartVO vo = new CartVO();
            BeanUtils.copyProperties(cart, vo);

            Product product = productMapper.selectById(cart.getProductId());
            if (product != null) {
                vo.setProductName(product.getName());
                vo.setProductImage(product.getMainImage());
                vo.setPrice(product.getPrice());
                vo.setStock(product.getStock());
            }
            voList.add(vo);
        }

        return voList;
    }

    @Override
    public void addCart(Long userId, Long productId, Integer quantity) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_EXIST);
        }

        // 检查是否已存在
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        wrapper.eq(Cart::getProductId, productId);
        Cart existCart = baseMapper.selectOne(wrapper);

        if (existCart != null) {
            existCart.setQuantity(existCart.getQuantity() + quantity);
            baseMapper.updateById(existCart);
        } else {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setMerchantId(product.getMerchantId()); // 从商品获取商家ID
            cart.setQuantity(quantity);
            cart.setSelected(1);
            baseMapper.insert(cart);
        }
    }

    @Override
    public void updateQuantity(Long userId, Long cartId, Integer quantity) {
        Cart cart = baseMapper.selectById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.CART_NOT_EXIST);
        }

        cart.setQuantity(quantity);
        baseMapper.updateById(cart);
    }

    @Override
    public void deleteCart(Long userId, Long cartId) {
        Cart cart = baseMapper.selectById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.CART_NOT_EXIST);
        }

        baseMapper.deleteById(cartId);
    }

    @Override
    public void clearCart(Long userId) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        baseMapper.delete(wrapper);
    }

    @Override
    public void updateSelected(Long userId, Long cartId, Integer selected) {
        Cart cart = baseMapper.selectById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.CART_NOT_EXIST);
        }

        cart.setSelected(selected);
        baseMapper.updateById(cart);
    }

    @Override
    public void selectAll(Long userId, Integer selected) {
        LambdaUpdateWrapper<Cart> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        wrapper.set(Cart::getSelected, selected);
        baseMapper.update(null, wrapper);
    }
}
