package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruit.common.exception.BusinessException;
import com.fruit.common.result.PageResult;
import com.fruit.common.result.ResultCode;
import com.fruit.dto.CategoryDTO;
import com.fruit.entity.Category;
import com.fruit.entity.Product;
import com.fruit.enums.StatusEnum;
import com.fruit.mapper.CategoryMapper;
import com.fruit.mapper.ProductMapper;
import com.fruit.service.CategoryService;
import com.fruit.vo.CategoryTreeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类服务实现类
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final ProductMapper productMapper;

    @Override
    public List<CategoryTreeVO> getCategoryTree() {
        // 查询所有启用的分类
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getStatus, StatusEnum.ENABLED.getCode());
        wrapper.orderByAsc(Category::getSort);

        List<Category> categories = baseMapper.selectList(wrapper);

        // 构建树形结构
        return buildTree(categories, 0L);
    }

    private List<CategoryTreeVO> buildTree(List<Category> categories, Long parentId) {
        List<CategoryTreeVO> tree = new ArrayList<>();

        for (Category category : categories) {
            if (category.getParentId().equals(parentId)) {
                CategoryTreeVO vo = new CategoryTreeVO();
                BeanUtils.copyProperties(category, vo);
                vo.setChildren(buildTree(categories, category.getId()));
                tree.add(vo);
            }
        }

        return tree;
    }

    @Override
    public PageResult<Category> listPage(Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSort);
        wrapper.orderByDesc(Category::getCreateTime);

        Page<Category> page = new Page<>(pageNum, pageSize);
        Page<Category> result = baseMapper.selectPage(page, wrapper);
        fillProductCount(result.getRecords());

        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    public List<Category> listEnabled() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getStatus, StatusEnum.ENABLED.getCode());
        wrapper.orderByAsc(Category::getSort);
        wrapper.orderByDesc(Category::getCreateTime);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public void addCategory(CategoryDTO dto) {
        Category category = new Category();
        BeanUtils.copyProperties(dto, category);
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        if (category.getSort() == null) {
            category.setSort(0);
        }
        category.setStatus(dto.getStatus() == null ? StatusEnum.ENABLED.getCode() : dto.getStatus());

        baseMapper.insert(category);
    }

    @Override
    public void updateCategory(Long id, CategoryDTO dto) {
        Category category = baseMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_EXIST);
        }

        BeanUtils.copyProperties(dto, category);
        category.setId(id);

        baseMapper.updateById(category);
    }

    @Override
    public void deleteCategory(Long id) {
        // 检查是否有子分类
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getParentId, id);
        Long count = baseMapper.selectCount(wrapper);

        if (count > 0) {
            throw new BusinessException(ResultCode.CATEGORY_HAS_CHILDREN);
        }

        baseMapper.deleteById(id);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Category category = baseMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_EXIST);
        }

        category.setStatus(status);
        baseMapper.updateById(category);
    }

    private void fillProductCount(List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return;
        }
        for (Category category : categories) {
            LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Product::getCategoryId, category.getId());
            category.setProductCount(productMapper.selectCount(wrapper));
        }
    }
}
