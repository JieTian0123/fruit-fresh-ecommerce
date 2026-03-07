package com.fruit.controller.consumer;

import com.fruit.common.result.PageResult;
import com.fruit.common.result.Result;
import com.fruit.entity.UserPointsLog;
import com.fruit.service.PointService;
import com.fruit.utils.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 消费者积分控制器
 */
@Api(tags = "消费者-积分")
@RestController
@RequestMapping("/consumer/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @ApiOperation("获取积分记录")
    @GetMapping("/log")
    public Result<PageResult<UserPointsLog>> getPointsLog(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {

        Long userId = UserContext.getUserId();
        PageResult<UserPointsLog> result = pointService.getPointsLog(userId, pageNum, pageSize);
        return Result.success(result);
    }

    @ApiOperation("获取当前积分")
    @GetMapping("/balance")
    public Result<Integer> getPointsBalance() {
        Long userId = UserContext.getUserId();
        Integer points = pointService.getUserPoints(userId);
        return Result.success(points);
    }
}
