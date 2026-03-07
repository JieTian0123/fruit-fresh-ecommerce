package com.fruit.controller.admin;

import com.fruit.common.result.Result;
import com.fruit.service.StatsService;
import com.fruit.vo.PeriodStatsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 管理员统计控制器
 */
@Api(tags = "管理员-统计")
@RestController
@RequestMapping("/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final StatsService statsService;

    @ApiOperation("概览统计")
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        Map<String, Object> data = statsService.getAdminOverview();
        return Result.success(data);
    }

    @ApiOperation("增长率统计")
    @GetMapping("/growth")
    public Result<Map<String, Object>> growth() {
        Map<String, Object> data = statsService.getAdminGrowth();
        return Result.success(data);
    }

    @ApiOperation("时段统计")
    @GetMapping("/period")
    public Result<PeriodStatsVO> periodStats(@RequestParam(defaultValue = "week") String period) {
        PeriodStatsVO data = statsService.getAdminPeriodStats(period);
        return Result.success(data);
    }
}
