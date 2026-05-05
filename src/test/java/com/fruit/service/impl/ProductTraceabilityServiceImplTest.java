package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fruit.common.exception.BusinessException;
import com.fruit.common.result.ResultCode;
import com.fruit.dto.TraceabilityDTO;
import com.fruit.entity.Product;
import com.fruit.entity.ProductTraceability;
import com.fruit.mapper.ProductMapper;
import com.fruit.mapper.ProductTraceabilityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductTraceabilityServiceImplTest {

    @Mock
    private ProductTraceabilityMapper productTraceabilityMapper;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductTraceabilityServiceImpl productTraceabilityService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(productTraceabilityService, "baseMapper", productTraceabilityMapper);
    }

    @Test
    void listByProductId_Success() {
        Long productId = 100L;
        ProductTraceability traceability = new ProductTraceability();
        traceability.setProductId(productId);

        when(productTraceabilityMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(traceability));

        List<ProductTraceability> result = productTraceabilityService.listByProductId(productId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(productId, result.get(0).getProductId());
    }

    @Test
    void addTraceability_Success() {
        Long merchantId = 1L;
        TraceabilityDTO dto = new TraceabilityDTO();
        dto.setProductId(100L);
        dto.setNodeName("Farm");
        dto.setOperator("John");
        dto.setOccurredTime(LocalDateTime.now());

        Product product = new Product();
        product.setMerchantId(merchantId);
        when(productMapper.selectById(100L)).thenReturn(product);

        when(productTraceabilityMapper.insert(any(ProductTraceability.class))).thenReturn(1);

        assertDoesNotThrow(() -> productTraceabilityService.addTraceability(merchantId, dto));

        verify(productTraceabilityMapper).insert(any(ProductTraceability.class));
    }

    @Test
    void addTraceability_ProductNotExist_ThrowsException() {
        Long merchantId = 1L;
        TraceabilityDTO dto = new TraceabilityDTO();
        dto.setProductId(100L);

        when(productMapper.selectById(100L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> productTraceabilityService.addTraceability(merchantId, dto));
        assertEquals(ResultCode.PRODUCT_NOT_EXIST.getCode(), exception.getCode());
    }

    @Test
    void addTraceability_ProductNotBelongToMerchant_ThrowsException() {
        Long merchantId = 1L;
        TraceabilityDTO dto = new TraceabilityDTO();
        dto.setProductId(100L);

        Product product = new Product();
        product.setMerchantId(2L); // Different merchant
        when(productMapper.selectById(100L)).thenReturn(product);

        BusinessException exception = assertThrows(BusinessException.class, () -> productTraceabilityService.addTraceability(merchantId, dto));
        assertEquals(ResultCode.PRODUCT_NOT_EXIST.getCode(), exception.getCode());
    }

    @Test
    void deleteTraceability_Success() {
        Long merchantId = 1L;
        Long id = 10L;

        ProductTraceability traceability = new ProductTraceability();
        traceability.setProductId(100L);
        when(productTraceabilityMapper.selectById(id)).thenReturn(traceability);

        Product product = new Product();
        product.setMerchantId(merchantId);
        when(productMapper.selectById(100L)).thenReturn(product);

        when(productTraceabilityMapper.deleteById(id)).thenReturn(1);

        assertDoesNotThrow(() -> productTraceabilityService.deleteTraceability(merchantId, id));

        verify(productTraceabilityMapper).deleteById(id);
    }

    @Test
    void deleteTraceability_DataNotExist_ThrowsException() {
        Long merchantId = 1L;
        Long id = 10L;

        when(productTraceabilityMapper.selectById(id)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> productTraceabilityService.deleteTraceability(merchantId, id));
        assertEquals(ResultCode.DATA_NOT_EXIST.getCode(), exception.getCode());
    }

    @Test
    void deleteTraceability_ProductNotBelongToMerchant_ThrowsException() {
        Long merchantId = 1L;
        Long id = 10L;

        ProductTraceability traceability = new ProductTraceability();
        traceability.setProductId(100L);
        when(productTraceabilityMapper.selectById(id)).thenReturn(traceability);

        Product product = new Product();
        product.setMerchantId(2L); // Different merchant
        when(productMapper.selectById(100L)).thenReturn(product);

        BusinessException exception = assertThrows(BusinessException.class, () -> productTraceabilityService.deleteTraceability(merchantId, id));
        assertEquals(ResultCode.PRODUCT_NOT_EXIST.getCode(), exception.getCode());
    }
}
