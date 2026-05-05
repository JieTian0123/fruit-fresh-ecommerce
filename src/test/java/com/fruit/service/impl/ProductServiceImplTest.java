package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.common.exception.BusinessException;
import com.fruit.common.result.PageResult;
import com.fruit.common.result.ResultCode;
import com.fruit.dto.ProductDTO;
import com.fruit.entity.Category;
import com.fruit.entity.MerchantShop;
import com.fruit.entity.Product;
import com.fruit.enums.StatusEnum;
import com.fruit.mapper.CategoryMapper;
import com.fruit.mapper.MerchantShopMapper;
import com.fruit.mapper.ProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @InjectMocks
    private ProductServiceImpl productService;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private MerchantShopMapper merchantShopMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(productService, "baseMapper", productMapper);
    }

    @Test
    @DisplayName("消费者获取商品列表 - 成功")
    void listForConsumer_Success() {
        // Arrange
        Long categoryId = 1L;
        String keyword = "苹果";
        String sortField = "price";
        String sortOrder = "asc";
        Integer pageNum = 1;
        Integer pageSize = 10;

        Product product = new Product();
        product.setId(1L);
        product.setName("红富士苹果");
        product.setPrice(new BigDecimal("10.00"));
        product.setShelfLifeDays(10);
        product.setProductionDate(LocalDate.now().minusDays(2));

        Page<Product> mockPage = new Page<>(pageNum, pageSize);
        mockPage.setRecords(Collections.singletonList(product));
        mockPage.setTotal(1);

        when(productMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(mockPage);

        // Act
        PageResult<Product> result = productService.listForConsumer(categoryId, keyword, sortField, sortOrder, pageNum, pageSize);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());

        Product resultProduct = result.getList().get(0);
        assertNotNull(resultProduct.getCurrentPrice()); // Freshness calculated

        verify(productMapper, times(1)).selectPage(any(Page.class), any(Wrapper.class));
    }

    @Test
    @DisplayName("消费者获取商品列表 - 按销量排序")
    void listForConsumer_SortBySales() {
        // Arrange
        Product product = new Product();
        product.setId(1L);
        product.setPrice(new BigDecimal("10.00"));

        Page<Product> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Collections.singletonList(product));
        mockPage.setTotal(1);

        when(productMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(mockPage);

        // Act
        PageResult<Product> result = productService.listForConsumer(null, null, "sales", "desc", 1, 10);

        // Assert
        assertNotNull(result);
        verify(productMapper, times(1)).selectPage(any(Page.class), any(Wrapper.class));
    }

    @Test
    @DisplayName("消费者获取商品列表 - 默认排序")
    void listForConsumer_DefaultSort() {
        // Arrange
        Product product = new Product();
        product.setId(1L);
        product.setPrice(new BigDecimal("10.00"));

        Page<Product> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Collections.singletonList(product));
        mockPage.setTotal(1);

        when(productMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(mockPage);

        // Act
        PageResult<Product> result = productService.listForConsumer(null, null, null, null, 1, 10);

        // Assert
        assertNotNull(result);
        verify(productMapper, times(1)).selectPage(any(Page.class), any(Wrapper.class));
    }

    @Test
    @DisplayName("商家获取商品列表 - 成功")
    void listForMerchant_Success() {
        // Arrange
        Long merchantId = 100L;
        Integer status = StatusEnum.ENABLED.getCode();

        Product product = new Product();
        product.setId(1L);
        product.setMerchantId(merchantId);
        product.setPrice(new BigDecimal("10.00"));

        Page<Product> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Collections.singletonList(product));
        mockPage.setTotal(1);

        when(productMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(mockPage);

        // Act
        PageResult<Product> result = productService.listForMerchant(merchantId, status, 1, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        verify(productMapper, times(1)).selectPage(any(Page.class), any(Wrapper.class));
    }

    @Test
    @DisplayName("管理员获取商品列表 - 成功")
    void listForAdmin_Success() {
        // Arrange
        Integer status = StatusEnum.PENDING.getCode();
        String keyword = "测试";

        Product product = new Product();
        product.setId(1L);
        product.setPrice(new BigDecimal("10.00"));

        Page<Product> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Collections.singletonList(product));
        mockPage.setTotal(1);

        when(productMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(mockPage);

        // Act
        PageResult<Product> result = productService.listForAdmin(status, keyword, 1, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        verify(productMapper, times(1)).selectPage(any(Page.class), any(Wrapper.class));
    }

    @Test
    @DisplayName("管理员获取商品列表 - 补充商家和分类名称")
    void listForAdmin_EnrichesMerchantAndCategoryName() {
        Product product = new Product();
        product.setId(1L);
        product.setMerchantId(100L);
        product.setCategoryId(10L);
        product.setPrice(new BigDecimal("10.00"));

        Page<Product> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Collections.singletonList(product));
        mockPage.setTotal(1);

        MerchantShop shop = new MerchantShop();
        shop.setId(20L);
        shop.setMerchantId(100L);
        shop.setShopName("测试鲜果店");
        shop.setLogo("logo.png");

        Category category = new Category();
        category.setId(10L);
        category.setName("新鲜水果");

        when(productMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(mockPage);
        when(merchantShopMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(shop));
        when(categoryMapper.selectBatchIds(anyCollection())).thenReturn(Collections.singletonList(category));

        PageResult<Product> result = productService.listForAdmin(null, null, 1, 10);

        Product resultProduct = result.getList().get(0);
        assertEquals("测试鲜果店", resultProduct.getMerchantName());
        assertEquals("logo.png", resultProduct.getShopLogo());
        assertEquals("新鲜水果", resultProduct.getCategoryName());
        assertEquals(20L, resultProduct.getShopId());
    }

    @Test
    @DisplayName("添加商品 - 成功")
    void addProduct_Success() {
        // Arrange
        Long merchantId = 100L;
        ProductDTO dto = new ProductDTO();
        dto.setName("新商品");
        dto.setPrice(new BigDecimal("20.00"));
        dto.setStock(100);

        when(productMapper.insert(any(Product.class))).thenReturn(1);

        // Act
        productService.addProduct(merchantId, dto);

        // Assert
        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productMapper, times(1)).insert(captor.capture());

        Product savedProduct = captor.getValue();
        assertEquals(merchantId, savedProduct.getMerchantId());
        assertEquals("新商品", savedProduct.getName());
        assertEquals(StatusEnum.PENDING.getCode(), savedProduct.getStatus());
        assertEquals(0, savedProduct.getSales());
    }

    @Test
    @DisplayName("更新商品 - 成功")
    void updateProduct_Success() {
        // Arrange
        Long merchantId = 100L;
        Long productId = 1L;
        ProductDTO dto = new ProductDTO();
        dto.setName("更新商品");
        dto.setPrice(new BigDecimal("25.00"));

        Product existingProduct = new Product();
        existingProduct.setId(productId);
        existingProduct.setMerchantId(merchantId);

        when(productMapper.selectById(productId)).thenReturn(existingProduct);
        when(productMapper.updateById(any(Product.class))).thenReturn(1);

        // Act
        productService.updateProduct(merchantId, productId, dto);

        // Assert
        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productMapper, times(1)).updateById(captor.capture());

        Product updatedProduct = captor.getValue();
        assertEquals(productId, updatedProduct.getId());
        assertEquals("更新商品", updatedProduct.getName());
    }

    @Test
    @DisplayName("更新商品 - 商品不存在或非本人商品")
    void updateProduct_NotFoundOrNotOwner() {
        // Arrange
        Long merchantId = 100L;
        Long productId = 1L;
        ProductDTO dto = new ProductDTO();

        when(productMapper.selectById(productId)).thenReturn(null);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            productService.updateProduct(merchantId, productId, dto);
        });
        assertEquals(ResultCode.PRODUCT_NOT_EXIST.getCode(), exception.getCode());

        // Test not owner
        Product existingProduct = new Product();
        existingProduct.setId(productId);
        existingProduct.setMerchantId(999L); // Different merchant

        when(productMapper.selectById(productId)).thenReturn(existingProduct);

        assertThrows(BusinessException.class, () -> {
            productService.updateProduct(merchantId, productId, dto);
        });
    }

    @Test
    @DisplayName("商品上架 - 成功")
    void onSale_Success() {
        // Arrange
        Long merchantId = 100L;
        Long productId = 1L;

        Product existingProduct = new Product();
        existingProduct.setId(productId);
        existingProduct.setMerchantId(merchantId);
        existingProduct.setStatus(StatusEnum.DISABLED.getCode());

        when(productMapper.selectById(productId)).thenReturn(existingProduct);
        when(productMapper.updateById(any(Product.class))).thenReturn(1);

        // Act
        productService.onSale(merchantId, productId);

        // Assert
        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productMapper, times(1)).updateById(captor.capture());
        assertEquals(StatusEnum.ENABLED.getCode(), captor.getValue().getStatus());
    }

    @Test
    @DisplayName("商品上架 - 商品不存在")
    void onSale_NotFound() {
        // Arrange
        Long merchantId = 100L;
        Long productId = 1L;

        when(productMapper.selectById(productId)).thenReturn(null);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            productService.onSale(merchantId, productId);
        });
    }

    @Test
    @DisplayName("商品下架 - 成功")
    void offSale_Success() {
        // Arrange
        Long merchantId = 100L;
        Long productId = 1L;

        Product existingProduct = new Product();
        existingProduct.setId(productId);
        existingProduct.setMerchantId(merchantId);
        existingProduct.setStatus(StatusEnum.ENABLED.getCode());

        when(productMapper.selectById(productId)).thenReturn(existingProduct);
        when(productMapper.updateById(any(Product.class))).thenReturn(1);

        // Act
        productService.offSale(merchantId, productId);

        // Assert
        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productMapper, times(1)).updateById(captor.capture());
        assertEquals(StatusEnum.DISABLED.getCode(), captor.getValue().getStatus());
    }

    @Test
    @DisplayName("商品下架 - 商品不存在")
    void offSale_NotFound() {
        // Arrange
        Long merchantId = 100L;
        Long productId = 1L;

        when(productMapper.selectById(productId)).thenReturn(null);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            productService.offSale(merchantId, productId);
        });
    }

    @Test
    @DisplayName("审核商品 - 成功")
    void approve_Success() {
        // Arrange
        Long productId = 1L;
        Integer status = StatusEnum.ENABLED.getCode();

        Product existingProduct = new Product();
        existingProduct.setId(productId);

        when(productMapper.selectById(productId)).thenReturn(existingProduct);
        when(productMapper.updateById(any(Product.class))).thenReturn(1);

        // Act
        productService.approve(productId, status);

        // Assert
        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productMapper, times(1)).updateById(captor.capture());
        assertEquals(status, captor.getValue().getStatus());
    }

    @Test
    @DisplayName("审核商品 - 商品不存在")
    void approve_NotFound() {
        // Arrange
        Long productId = 1L;
        Integer status = StatusEnum.ENABLED.getCode();

        when(productMapper.selectById(productId)).thenReturn(null);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            productService.approve(productId, status);
        });
    }

    @Test
    @DisplayName("删除商品 - 成功")
    void deleteProduct_Success() {
        // Arrange
        Long merchantId = 100L;
        Long productId = 1L;

        Product existingProduct = new Product();
        existingProduct.setId(productId);
        existingProduct.setMerchantId(merchantId);

        when(productMapper.selectById(productId)).thenReturn(existingProduct);
        when(productMapper.deleteById(productId)).thenReturn(1);

        // Act
        productService.deleteProduct(merchantId, productId);

        // Assert
        verify(productMapper, times(1)).deleteById(productId);
    }

    @Test
    @DisplayName("删除商品 - 商品不存在")
    void deleteProduct_NotFound() {
        // Arrange
        Long merchantId = 100L;
        Long productId = 1L;

        when(productMapper.selectById(productId)).thenReturn(null);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            productService.deleteProduct(merchantId, productId);
        });
    }

    @Test
    @DisplayName("获取热门商品 - 成功")
    void getHotProducts_Success() {
        // Arrange
        Integer limit = 5;
        Product product = new Product();
        product.setId(1L);
        product.setPrice(new BigDecimal("10.00"));

        when(productMapper.selectList(any(Wrapper.class))).thenReturn(Collections.singletonList(product));

        // Act
        List<Product> result = productService.getHotProducts(limit);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productMapper, times(1)).selectList(any(Wrapper.class));
    }

    @Test
    @DisplayName("获取最新商品 - 成功")
    void getNewProducts_Success() {
        // Arrange
        Integer limit = 5;
        Product product = new Product();
        product.setId(1L);
        product.setPrice(new BigDecimal("10.00"));

        when(productMapper.selectList(any(Wrapper.class))).thenReturn(Collections.singletonList(product));

        // Act
        List<Product> result = productService.getNewProducts(limit);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productMapper, times(1)).selectList(any(Wrapper.class));
    }

    @Test
    @DisplayName("获取推荐商品 - 成功")
    void getRecommendProducts_Success() {
        // Arrange
        Integer limit = 5;
        Product product = new Product();
        product.setId(1L);
        product.setPrice(new BigDecimal("10.00"));

        when(productMapper.selectList(any(Wrapper.class))).thenReturn(Collections.singletonList(product));

        // Act
        List<Product> result = productService.getRecommendProducts(limit);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productMapper, times(1)).selectList(any(Wrapper.class));
    }

    @Test
    @DisplayName("获取商品详情 - 成功")
    void getProductDetail_Success() {
        // Arrange
        Long productId = 1L;
        Long shopId = 200L;

        Product product = new Product();
        product.setId(productId);
        product.setShopId(shopId);
        product.setPrice(new BigDecimal("10.00"));
        product.setShelfLifeDays(10);
        product.setProductionDate(LocalDate.now().minusDays(8)); // 剩余2天，打5折

        MerchantShop shop = new MerchantShop();
        shop.setId(shopId);
        shop.setShopName("测试店铺");
        shop.setLogo("logo.png");

        when(productMapper.selectById(productId)).thenReturn(product);
        when(merchantShopMapper.selectBatchIds(anyCollection())).thenReturn(Collections.singletonList(shop));

        // Act
        Product result = productService.getProductDetail(productId);

        // Assert
        assertNotNull(result);
        assertEquals("测试店铺", result.getMerchantName());
        assertEquals("logo.png", result.getShopLogo());
        assertEquals(new BigDecimal("5.00"), result.getCurrentPrice());
        assertEquals("日落促销5折", result.getDiscountLabel());
    }

    @Test
    @DisplayName("获取商品详情 - 商品不存在")
    void getProductDetail_NotFound() {
        // Arrange
        Long productId = 1L;

        when(productMapper.selectById(productId)).thenReturn(null);

        // Act
        Product result = productService.getProductDetail(productId);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("计算新鲜度 - 过期商品")
    void calculateFreshness_Expired() {
        // Arrange
        Long productId = 1L;

        Product product = new Product();
        product.setId(productId);
        product.setPrice(new BigDecimal("10.00"));
        product.setShelfLifeDays(10);
        product.setProductionDate(LocalDate.now().minusDays(11)); // 已过期

        when(productMapper.selectById(productId)).thenReturn(product);

        // Act
        Product result = productService.getProductDetail(productId);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("3.00"), result.getCurrentPrice());
        assertEquals("已过期", result.getDiscountLabel());
    }

    @Test
    @DisplayName("计算新鲜度 - 9折")
    void calculateFreshness_90Percent() {
        // Arrange
        Long productId = 1L;

        Product product = new Product();
        product.setId(productId);
        product.setPrice(new BigDecimal("10.00"));
        product.setShelfLifeDays(10);
        product.setProductionDate(LocalDate.now().minusDays(4)); // 剩余6天，比例0.6，打9折

        when(productMapper.selectById(productId)).thenReturn(product);

        // Act
        Product result = productService.getProductDetail(productId);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("9.00"), result.getCurrentPrice());
        assertEquals("9折", result.getDiscountLabel());
    }

    @Test
    @DisplayName("计算新鲜度 - 7折")
    void calculateFreshness_70Percent() {
        // Arrange
        Long productId = 1L;

        Product product = new Product();
        product.setId(productId);
        product.setPrice(new BigDecimal("10.00"));
        product.setShelfLifeDays(10);
        product.setProductionDate(LocalDate.now().minusDays(6)); // 剩余4天，比例0.4，打7折

        when(productMapper.selectById(productId)).thenReturn(product);

        // Act
        Product result = productService.getProductDetail(productId);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("7.00"), result.getCurrentPrice());
        assertEquals("临期特惠7折", result.getDiscountLabel());
    }
}
