package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.common.exception.BusinessException;
import com.fruit.common.result.PageResult;
import com.fruit.common.result.ResultCode;
import com.fruit.dto.CategoryDTO;
import com.fruit.entity.Category;
import com.fruit.enums.StatusEnum;
import com.fruit.mapper.CategoryMapper;
import com.fruit.mapper.ProductMapper;
import com.fruit.vo.CategoryTreeVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(categoryService, "baseMapper", categoryMapper);
    }

    @Test
    void getCategoryTree_Success() {
        Category parent = new Category();
        parent.setId(1L);
        parent.setParentId(0L);
        parent.setName("Parent");

        Category child = new Category();
        child.setId(2L);
        child.setParentId(1L);
        child.setName("Child");

        List<Category> categories = Arrays.asList(parent, child);

        when(categoryMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(categories);

        List<CategoryTreeVO> result = categoryService.getCategoryTree();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("Parent", result.get(0).getName());

        List<CategoryTreeVO> children = result.get(0).getChildren();
        assertNotNull(children);
        assertEquals(1, children.size());
        assertEquals(2L, children.get(0).getId());
        assertEquals("Child", children.get(0).getName());

        verify(categoryMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void listPage_Success() {
        Category category = new Category();
        category.setId(1L);
        Page<Category> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(category));
        page.setTotal(1);

        when(categoryMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        when(productMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(12L);

        PageResult<Category> result = categoryService.listPage(1, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals(12L, result.getList().get(0).getProductCount());
        verify(categoryMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        verify(productMapper, times(1)).selectCount(any(LambdaQueryWrapper.class));
    }

    @Test
    void listEnabled_Success() {
        Category category = new Category();
        category.setId(1L);
        category.setStatus(StatusEnum.ENABLED.getCode());
        when(categoryMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(category));

        List<Category> result = categoryService.listEnabled();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(StatusEnum.ENABLED.getCode(), result.get(0).getStatus());
        verify(categoryMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void addCategory_Success() {
        CategoryDTO dto = new CategoryDTO();
        dto.setName("Test Category");

        when(categoryMapper.insert(any(Category.class))).thenReturn(1);

        categoryService.addCategory(dto);

        verify(categoryMapper, times(1)).insert(argThat(category ->
            "Test Category".equals(category.getName()) &&
            StatusEnum.ENABLED.getCode().equals(category.getStatus())
        ));
    }

    @Test
    void addCategory_UsesSubmittedStatusAndSort() {
        CategoryDTO dto = new CategoryDTO();
        dto.setName("Hidden First Category");
        dto.setSort(-10);
        dto.setStatus(StatusEnum.DISABLED.getCode());

        when(categoryMapper.insert(any(Category.class))).thenReturn(1);

        categoryService.addCategory(dto);

        verify(categoryMapper).insert(argThat(category ->
                "Hidden First Category".equals(category.getName()) &&
                        Integer.valueOf(-10).equals(category.getSort()) &&
                        StatusEnum.DISABLED.getCode().equals(category.getStatus())
        ));
    }

    @Test
    void updateCategory_Success() {
        Long id = 1L;
        CategoryDTO dto = new CategoryDTO();
        dto.setName("Updated Category");

        Category existingCategory = new Category();
        existingCategory.setId(id);
        existingCategory.setName("Old Category");

        when(categoryMapper.selectById(id)).thenReturn(existingCategory);
        when(categoryMapper.updateById(any(Category.class))).thenReturn(1);

        categoryService.updateCategory(id, dto);

        verify(categoryMapper, times(1)).selectById(id);
        verify(categoryMapper, times(1)).updateById(argThat(category ->
            id.equals(category.getId()) &&
            "Updated Category".equals(category.getName())
        ));
    }

    @Test
    void updateCategory_NotExist_ThrowsException() {
        Long id = 1L;
        CategoryDTO dto = new CategoryDTO();

        when(categoryMapper.selectById(id)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> categoryService.updateCategory(id, dto));
        assertEquals(ResultCode.CATEGORY_NOT_EXIST.getCode(), exception.getCode());
        verify(categoryMapper, times(1)).selectById(id);
        verify(categoryMapper, never()).updateById(any(Category.class));
    }

    @Test
    void deleteCategory_Success() {
        Long id = 1L;

        when(categoryMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(categoryMapper.deleteById(any(Serializable.class))).thenReturn(1);

        categoryService.deleteCategory(id);

        verify(categoryMapper, times(1)).selectCount(any(LambdaQueryWrapper.class));
        verify(categoryMapper, times(1)).deleteById(any(Serializable.class));
    }

    @Test
    void deleteCategory_HasChildren_ThrowsException() {
        Long id = 1L;

        when(categoryMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> categoryService.deleteCategory(id));
        assertEquals(ResultCode.CATEGORY_HAS_CHILDREN.getCode(), exception.getCode());
        verify(categoryMapper, times(1)).selectCount(any(LambdaQueryWrapper.class));
        verify(categoryMapper, never()).deleteById(any(Serializable.class));
    }

    @Test
    void updateStatus_Success() {
        Long id = 1L;
        Integer status = StatusEnum.DISABLED.getCode();

        Category existingCategory = new Category();
        existingCategory.setId(id);
        existingCategory.setStatus(StatusEnum.ENABLED.getCode());

        when(categoryMapper.selectById(id)).thenReturn(existingCategory);
        when(categoryMapper.updateById(any(Category.class))).thenReturn(1);

        categoryService.updateStatus(id, status);

        verify(categoryMapper, times(1)).selectById(id);
        verify(categoryMapper, times(1)).updateById(argThat(category ->
            id.equals(category.getId()) &&
            status.equals(category.getStatus())
        ));
    }

    @Test
    void updateStatus_NotExist_ThrowsException() {
        Long id = 1L;
        Integer status = StatusEnum.DISABLED.getCode();

        when(categoryMapper.selectById(id)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> categoryService.updateStatus(id, status));
        assertEquals(ResultCode.CATEGORY_NOT_EXIST.getCode(), exception.getCode());
        verify(categoryMapper, times(1)).selectById(id);
        verify(categoryMapper, never()).updateById(any(Category.class));
    }
}
