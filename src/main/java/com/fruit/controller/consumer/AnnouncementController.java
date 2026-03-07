package com.fruit.controller.consumer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.common.result.Result;
import com.fruit.entity.Announcement;
import com.fruit.service.AnnouncementService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 消费者公告控制器
 */
@Api(tags = "消费者-公告")
@RestController
@RequestMapping("/announcement")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @ApiOperation("公告列表")
    @GetMapping("/list")
    public Result<Page<Announcement>> list(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("公告类型 1-系统公告 2-活动公告 3-知识科普") @RequestParam(required = false) Integer type) {

        Page<Announcement> page = announcementService.listAnnouncements(pageNum, pageSize, type);
        return Result.success(page);
    }

    @ApiOperation("公告详情")
    @GetMapping("/{id}")
    public Result<Announcement> detail(@PathVariable Long id) {
        Announcement announcement = announcementService.getDetail(id);
        // 增加浏览次数
        announcementService.incrementViewCount(id);
        return Result.success(announcement);
    }
}
