package com.fruit.controller.consumer;

import com.fruit.common.result.Result;
import com.fruit.entity.Category;
import com.fruit.service.CategoryService;
import com.fruit.vo.CategoryTreeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 消费者分类控制器
 */
@Api(tags = "消费者-分类")
@RestController
@RequestMapping("/consumer/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @ApiOperation("获取分类树")
    @GetMapping("/tree")
    public Result<List<CategoryTreeVO>> tree() {
        List<CategoryTreeVO> tree = categoryService.getCategoryTree();
        return Result.success(tree);
    }

    @ApiOperation("获取分类列表")
    @GetMapping("/list")
    public Result<List<Category>> list() {
        List<Category> list = categoryService.listEnabled();
        return Result.success(list);
    }
}
