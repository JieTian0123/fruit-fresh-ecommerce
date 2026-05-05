package com.fruit.controller.admin;

import com.fruit.common.result.Result;
import com.fruit.dto.HomeActivityDTO;
import com.fruit.service.BannerService;
import com.fruit.vo.HomeActivityVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员首页活动管理控制器
 */
@Api(tags = "管理员-首页活动管理")
@RestController
@RequestMapping("/admin/activity")
@RequiredArgsConstructor
public class HomeActivityManageController {

    private final BannerService bannerService;

    @ApiOperation("首页固定活动列表")
    @GetMapping("/list")
    public Result<List<HomeActivityVO>> list() {
        return Result.success(bannerService.getHomeActivitiesForAdmin());
    }

    @ApiOperation("更新首页固定活动")
    @PutMapping("/{code}")
    public Result<Void> update(@PathVariable String code, @RequestBody HomeActivityDTO dto) {
        bannerService.updateHomeActivity(code, dto);
        return Result.success();
    }
}
