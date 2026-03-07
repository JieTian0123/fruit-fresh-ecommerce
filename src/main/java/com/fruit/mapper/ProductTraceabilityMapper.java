package com.fruit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fruit.entity.ProductTraceability;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品溯源Mapper
 */
@Mapper
public interface ProductTraceabilityMapper extends BaseMapper<ProductTraceability> {
}
