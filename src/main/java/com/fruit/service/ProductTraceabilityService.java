package com.fruit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruit.dto.TraceabilityDTO;
import com.fruit.entity.ProductTraceability;

import java.util.List;

/**
 * 商品溯源服务接口
 */
public interface ProductTraceabilityService extends IService<ProductTraceability> {
    List<ProductTraceability> listByProductId(Long productId);
    void addTraceability(Long merchantId, TraceabilityDTO dto);
    void deleteTraceability(Long merchantId, Long id);
}
