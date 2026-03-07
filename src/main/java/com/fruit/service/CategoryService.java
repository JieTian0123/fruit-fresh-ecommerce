package com.fruit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruit.common.result.PageResult;
import com.fruit.dto.CategoryDTO;
import com.fruit.entity.Category;
import com.fruit.vo.CategoryTreeVO;

import java.util.List;

/**
 * 分类服务接口
 */
public interface CategoryService extends IService<Category> {

    /**
     * 获取分类树
     */
    List<CategoryTreeVO> getCategoryTree();

    /**
     * 获取分类列表（分页）
     */
    PageResult<Category> listPage(Integer pageNum, Integer pageSize);

    /**
     * 添加分类
     */
    void addCategory(CategoryDTO dto);

    /**
     * 修改分类
     */
    void updateCategory(Long id, CategoryDTO dto);

    /**
     * 删除分类
     */
    void deleteCategory(Long id);

    /**
     * 启用/禁用分类
     */
    void updateStatus(Long id, Integer status);
}
