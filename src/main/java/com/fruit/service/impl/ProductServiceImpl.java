package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruit.common.exception.BusinessException;
import com.fruit.common.result.PageResult;
import com.fruit.common.result.ResultCode;
import com.fruit.dto.ProductDTO;
import com.fruit.entity.MerchantShop;
import com.fruit.entity.Product;
import com.fruit.enums.StatusEnum;
import com.fruit.mapper.MerchantShopMapper;
import com.fruit.mapper.ProductMapper;
import com.fruit.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 商品服务实现类
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final MerchantShopMapper merchantShopMapper;

    @Override
    public PageResult<Product> listForConsumer(Long categoryId, String keyword, String sortField, 
                                                String sortOrder, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, StatusEnum.ENABLED.getCode());

        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }

        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Product::getName, keyword)
                    .or().like(Product::getSubtitle, keyword));
        }

        // 排序
        if ("price".equals(sortField)) {
            if ("asc".equals(sortOrder)) {
                wrapper.orderByAsc(Product::getPrice);
            } else {
                wrapper.orderByDesc(Product::getPrice);
            }
        } else if ("sales".equals(sortField)) {
            wrapper.orderByDesc(Product::getSales);
        } else {
            wrapper.orderByDesc(Product::getCreateTime);
        }

        Page<Product> page = new Page<>(pageNum, pageSize);
        Page<Product> result = baseMapper.selectPage(page, wrapper);
        result.getRecords().forEach(this::calculateFreshness);

        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    public PageResult<Product> listForMerchant(Long merchantId, Integer status, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getMerchantId, merchantId);

        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }

        wrapper.orderByDesc(Product::getCreateTime);

        Page<Product> page = new Page<>(pageNum, pageSize);
        Page<Product> result = baseMapper.selectPage(page, wrapper);
        result.getRecords().forEach(this::calculateFreshness);

        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    public PageResult<Product> listForAdmin(Integer status, String keyword, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }

        if (StringUtils.isNotBlank(keyword)) {
            wrapper.like(Product::getName, keyword);
        }

        wrapper.orderByDesc(Product::getCreateTime);

        Page<Product> page = new Page<>(pageNum, pageSize);
        Page<Product> result = baseMapper.selectPage(page, wrapper);
        result.getRecords().forEach(this::calculateFreshness);

        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    public void addProduct(Long merchantId, ProductDTO dto) {
        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        product.setMerchantId(merchantId);
        product.setStatus(StatusEnum.PENDING.getCode()); // 待审核
        product.setSales(0);

        baseMapper.insert(product);
    }

    @Override
    public void updateProduct(Long merchantId, Long productId, ProductDTO dto) {
        Product product = baseMapper.selectById(productId);
        if (product == null || !product.getMerchantId().equals(merchantId)) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_EXIST);
        }

        BeanUtils.copyProperties(dto, product);
        product.setId(productId);

        baseMapper.updateById(product);
    }

    @Override
    public void onSale(Long merchantId, Long productId) {
        Product product = baseMapper.selectById(productId);
        if (product == null || !product.getMerchantId().equals(merchantId)) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_EXIST);
        }

        product.setStatus(StatusEnum.ENABLED.getCode());
        baseMapper.updateById(product);
    }

    @Override
    public void offSale(Long merchantId, Long productId) {
        Product product = baseMapper.selectById(productId);
        if (product == null || !product.getMerchantId().equals(merchantId)) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_EXIST);
        }

        product.setStatus(StatusEnum.DISABLED.getCode());
        baseMapper.updateById(product);
    }

    @Override
    public void approve(Long productId, Integer status) {
        Product product = baseMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_EXIST);
        }

        product.setStatus(status);
        baseMapper.updateById(product);
    }

    @Override
    public void deleteProduct(Long merchantId, Long productId) {
        Product product = baseMapper.selectById(productId);
        if (product == null || !product.getMerchantId().equals(merchantId)) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_EXIST);
        }

        baseMapper.deleteById(productId);
    }

    @Override
    public List<Product> getHotProducts(Integer limit) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, StatusEnum.ENABLED.getCode())
                .orderByDesc(Product::getSales)
                .last("LIMIT " + limit);
        List<Product> list = baseMapper.selectList(wrapper);
        list.forEach(this::calculateFreshness);
        return list;
    }

    @Override
    public List<Product> getNewProducts(Integer limit) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, StatusEnum.ENABLED.getCode())
                .orderByDesc(Product::getCreateTime)
                .last("LIMIT " + limit);
        List<Product> list = baseMapper.selectList(wrapper);
        list.forEach(this::calculateFreshness);
        return list;
    }

    @Override
    public List<Product> getRecommendProducts(Integer limit) {
        // 推荐商品：随机选取上架商品
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, StatusEnum.ENABLED.getCode())
                .orderByDesc(Product::getSales)
                .last("LIMIT " + limit);
        List<Product> list = baseMapper.selectList(wrapper);
        list.forEach(this::calculateFreshness);
        return list;
    }

    @Override
    public Product getProductDetail(Long id) {
        Product product = baseMapper.selectById(id);
        if (product == null) {
            return null;
        }
        calculateFreshness(product);
        // 关联查询店铺名称
        if (product.getShopId() != null) {
            MerchantShop shop = merchantShopMapper.selectById(product.getShopId());
            if (shop != null) {
                product.setMerchantName(shop.getShopName());
                product.setShopLogo(shop.getLogo());
            }
        }
        return product;
    }

    /**
     * 计算新鲜度动态价格
     */
    private void calculateFreshness(Product product) {
        if (product == null || product.getPrice() == null) {
            return;
        }
        if (product.getShelfLifeDays() == null || product.getProductionDate() == null) {
            product.setCurrentPrice(product.getPrice());
            return;
        }

        long daysPassed = ChronoUnit.DAYS.between(product.getProductionDate(), LocalDate.now());
        int remaining = product.getShelfLifeDays() - (int) daysPassed;
        if (remaining < 0) remaining = 0;

        double ratio = product.getShelfLifeDays() > 0
            ? (double) remaining / product.getShelfLifeDays()
            : 0;
        BigDecimal discount;
        String label = null;

        if (ratio > 0.7) {
            discount = BigDecimal.ONE;
        } else if (ratio > 0.5) {
            discount = new BigDecimal("0.9");
            label = "9折";
        } else if (ratio > 0.3) {
            discount = new BigDecimal("0.7");
            label = "临期特惠7折";
        } else if (remaining > 0) {
            discount = new BigDecimal("0.5");
            label = "日落促销5折";
        } else {
            discount = new BigDecimal("0.3");
            label = "即将过期3折";
        }

        product.setCurrentPrice(product.getPrice().multiply(discount).setScale(2, RoundingMode.HALF_UP));
        product.setDiscountLabel(label);
    }
}
