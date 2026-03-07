package com.fruit.controller.merchant;

import com.fruit.common.result.Result;
import com.fruit.service.StatsService;
import com.fruit.utils.UserContext;
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
 * 商家统计控制器
 */
@Api(tags = "商家-统计")
@RestController
@RequestMapping("/merchant/stats")
@RequiredArgsConstructor
public class MerchantStatsController {

    private final StatsService statsService;

    @ApiOperation("商家概览统计")
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        Long merchantId = UserContext.getUserId();
        Map<String, Object> data = statsService.getMerchantOverview(merchantId);
        return Result.success(data);
    }

    @ApiOperation("商家时段统计")
    @GetMapping("/period")
    public Result<PeriodStatsVO> periodStats(@RequestParam(defaultValue = "week") String period) {
        Long merchantId = UserContext.getUserId();
        PeriodStatsVO data = statsService.getMerchantPeriodStats(merchantId, period);
        return Result.success(data);
    }
}
