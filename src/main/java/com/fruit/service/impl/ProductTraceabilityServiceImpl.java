package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruit.common.exception.BusinessException;
import com.fruit.common.result.ResultCode;
import com.fruit.dto.TraceabilityDTO;
import com.fruit.entity.Product;
import com.fruit.entity.ProductTraceability;
import com.fruit.mapper.ProductMapper;
import com.fruit.mapper.ProductTraceabilityMapper;
import com.fruit.service.ProductTraceabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品溯源服务实现类
 */
@Service
@RequiredArgsConstructor
public class ProductTraceabilityServiceImpl
        extends ServiceImpl<ProductTraceabilityMapper, ProductTraceability>
        implements ProductTraceabilityService {

    private final ProductMapper productMapper;

    @Override
    public List<ProductTraceability> listByProductId(Long productId) {
        LambdaQueryWrapper<ProductTraceability> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductTraceability::getProductId, productId)
               .orderByAsc(ProductTraceability::getOccurredTime);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public void addTraceability(Long merchantId, TraceabilityDTO dto) {
        Product product = productMapper.selectById(dto.getProductId());
        if (product == null || !product.getMerchantId().equals(merchantId)) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_EXIST);
        }
        ProductTraceability entity = new ProductTraceability();
        BeanUtils.copyProperties(dto, entity);
        baseMapper.insert(entity);
    }

    @Override
    public void deleteTraceability(Long merchantId, Long id) {
        ProductTraceability traceability = baseMapper.selectById(id);
        if (traceability == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        Product product = productMapper.selectById(traceability.getProductId());
        if (product == null || !product.getMerchantId().equals(merchantId)) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_EXIST);
        }
        baseMapper.deleteById(id);
    }
}
