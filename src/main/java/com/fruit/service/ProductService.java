package com.fruit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruit.common.result.PageResult;
import com.fruit.dto.ProductDTO;
import com.fruit.entity.Product;

import java.util.List;

/**
 * 商品服务接口
 */
public interface ProductService extends IService<Product> {

    /**
     * 消费者商品列表
     */
    PageResult<Product> listForConsumer(Long categoryId, String keyword, String sortField, String sortOrder, Integer pageNum, Integer pageSize);

    /**
     * 商家商品列表
     */
    PageResult<Product> listForMerchant(Long merchantId, Integer status, Integer pageNum, Integer pageSize);

    /**
     * 管理员商品列表
     */
    PageResult<Product> listForAdmin(Integer status, String keyword, Integer pageNum, Integer pageSize);

    /**
     * 商家添加商品
     */
    void addProduct(Long merchantId, ProductDTO dto);

    /**
     * 商家修改商品
     */
    void updateProduct(Long merchantId, Long productId, ProductDTO dto);

    /**
     * 商品上架
     */
    void onSale(Long merchantId, Long productId);

    /**
     * 商品下架
     */
    void offSale(Long merchantId, Long productId);

    /**
     * 管理员审核商品
     */
    void approve(Long productId, Integer status);

    /**
     * 删除商品
     */
    void deleteProduct(Long merchantId, Long productId);

    /**
     * 获取热销商品
     */
    List<Product> getHotProducts(Integer limit);

    /**
     * 获取新品商品
     */
    List<Product> getNewProducts(Integer limit);

    /**
     * 获取推荐商品
     */
    List<Product> getRecommendProducts(Integer limit);

    /**
     * 获取商品详情（含新鲜度计算）
     */
    Product getProductDetail(Long id);
}
