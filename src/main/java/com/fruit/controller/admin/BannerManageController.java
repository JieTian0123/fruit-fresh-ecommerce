package com.fruit.controller.admin;

import com.fruit.common.result.PageResult;
import com.fruit.common.result.Result;
import com.fruit.dto.BannerDTO;
import com.fruit.entity.Banner;
import com.fruit.service.BannerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 管理员轮播图管理控制器
 */
@Api(tags = "管理员-轮播图管理")
@RestController
@RequestMapping("/admin/banner")
@RequiredArgsConstructor
public class BannerManageController {

    private final BannerService bannerService;

    @ApiOperation("轮播图列表")
    @GetMapping("/list")
    public Result<PageResult<Banner>> list(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {

        PageResult<Banner> pageResult = bannerService.listForAdmin(pageNum, pageSize);
        return Result.success(pageResult);
    }

    @ApiOperation("轮播图详情")
    @GetMapping("/detail/{id}")
    public Result<Banner> detail(@PathVariable Long id) {
        Banner banner = bannerService.getById(id);
        return Result.success(banner);
    }

    @ApiOperation("添加轮播图")
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody BannerDTO dto) {
        bannerService.addBanner(dto);
        return Result.success();
    }

    @ApiOperation("修改轮播图")
    @PutMapping("/update/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody BannerDTO dto) {
        bannerService.updateBanner(id, dto);
        return Result.success();
    }

    @ApiOperation("删除轮播图")
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        bannerService.deleteBanner(id);
        return Result.success();
    }

    @ApiOperation("修改轮播图状态")
    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        bannerService.updateStatus(id, status);
        return Result.success();
    }
}
