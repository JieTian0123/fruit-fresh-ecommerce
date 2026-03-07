package com.fruit.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.common.result.Result;
import com.fruit.entity.Announcement;
import com.fruit.service.AnnouncementService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 管理员公告管理控制器
 */
@Api(tags = "管理员-公告管理")
@RestController
@RequestMapping("/admin/announcement")
@RequiredArgsConstructor
public class AnnouncementManageController {

    private final AnnouncementService announcementService;

    @ApiOperation("公告列表")
    @GetMapping("/list")
    public Result<Page<Announcement>> list(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("状态") @RequestParam(required = false) Integer status,
            @ApiParam("关键词") @RequestParam(required = false) String keyword) {

        Page<Announcement> page = announcementService.listForAdmin(pageNum, pageSize, status, keyword);
        return Result.success(page);
    }

    @ApiOperation("公告详情")
    @GetMapping("/detail/{id}")
    public Result<Announcement> detail(@PathVariable Long id) {
        Announcement announcement = announcementService.getById(id);
        return Result.success(announcement);
    }

    @ApiOperation("添加公告")
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody Announcement announcement) {
        announcementService.save(announcement);
        return Result.success();
    }

    @ApiOperation("修改公告")
    @PutMapping("/update")
    public Result<Void> update(@Valid @RequestBody Announcement announcement) {
        announcementService.updateById(announcement);
        return Result.success();
    }

    @ApiOperation("发布公告")
    @PostMapping("/publish/{id}")
    public Result<Void> publish(@PathVariable Long id) {
        announcementService.publish(id);
        return Result.success();
    }

    @ApiOperation("下架公告")
    @PostMapping("/unpublish/{id}")
    public Result<Void> unpublish(@PathVariable Long id) {
        announcementService.unpublish(id);
        return Result.success();
    }

    @ApiOperation("删除公告")
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        announcementService.removeById(id);
        return Result.success();
    }
}
