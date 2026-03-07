package com.fruit.controller.admin;

import com.fruit.common.result.PageResult;
import com.fruit.common.result.Result;
import com.fruit.dto.CategoryDTO;
import com.fruit.entity.Category;
import com.fruit.service.CategoryService;
import com.fruit.vo.CategoryTreeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 管理员分类管理控制器
 */
@Api(tags = "管理员-分类管理")
@RestController
@RequestMapping("/admin/category")
@RequiredArgsConstructor
public class CategoryManageController {

    private final CategoryService categoryService;

    @ApiOperation("分类列表")
    @GetMapping("/list")
    public Result<PageResult<Category>> list(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {

        PageResult<Category> pageResult = categoryService.listPage(pageNum, pageSize);
        return Result.success(pageResult);
    }

    @ApiOperation("分类树")
    @GetMapping("/tree")
    public Result<List<CategoryTreeVO>> tree() {
        List<CategoryTreeVO> tree = categoryService.getCategoryTree();
        return Result.success(tree);
    }

    @ApiOperation("分类详情")
    @GetMapping("/detail/{id}")
    public Result<Category> detail(@PathVariable Long id) {
        Category category = categoryService.getById(id);
        return Result.success(category);
    }

    @ApiOperation("添加分类")
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody CategoryDTO dto) {
        categoryService.addCategory(dto);
        return Result.success();
    }

    @ApiOperation("修改分类")
    @PutMapping("/update/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CategoryDTO dto) {
        categoryService.updateCategory(id, dto);
        return Result.success();
    }

    @ApiOperation("删除分类")
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success();
    }

    @ApiOperation("修改分类状态")
    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        categoryService.updateStatus(id, status);
        return Result.success();
    }
}
