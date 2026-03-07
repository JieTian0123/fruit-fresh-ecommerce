package com.fruit.controller.consumer;

import com.fruit.common.result.Result;
import com.fruit.entity.MemberLevel;
import com.fruit.entity.UserSignIn;
import com.fruit.service.MemberService;
import com.fruit.service.SignInService;
import com.fruit.utils.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消费者签到控制器
 */
@Api(tags = "消费者-签到")
@RestController
@RequestMapping("/consumer/sign-in")
@RequiredArgsConstructor
public class SignInController {

    private final SignInService signInService;
    private final MemberService memberService;

    @ApiOperation("签到")
    @PostMapping
    public Result<UserSignIn> signIn() {
        Long userId = UserContext.getUserId();
        UserSignIn signIn = signInService.signIn(userId);
        return Result.success(signIn);
    }

    @ApiOperation("获取签到状态")
    @GetMapping("/status")
    public Result<Map<String, Object>> getSignInStatus() {
        Long userId = UserContext.getUserId();

        Map<String, Object> status = new HashMap<>();
        status.put("signedToday", signInService.hasSignedToday(userId));
        status.put("continuousDays", signInService.getContinuousDays(userId));

        return Result.success(status);
    }

    @ApiOperation("获取本月签到记录")
    @GetMapping("/month")
    public Result<List<UserSignIn>> getMonthSignIns(
            @ApiParam("年份") @RequestParam(required = false) Integer year,
            @ApiParam("月份") @RequestParam(required = false) Integer month) {

        Long userId = UserContext.getUserId();
        if (year == null) year = LocalDate.now().getYear();
        if (month == null) month = LocalDate.now().getMonthValue();

        List<UserSignIn> signIns = signInService.getMonthSignIns(userId, year, month);
        return Result.success(signIns);
    }

    @ApiOperation("获取会员等级列表")
    @GetMapping("/levels")
    public Result<List<MemberLevel>> getMemberLevels() {
        List<MemberLevel> levels = memberService.getAllLevels();
        return Result.success(levels);
    }

    @ApiOperation("获取当前会员等级")
    @GetMapping("/level")
    public Result<MemberLevel> getCurrentLevel() {
        Long userId = UserContext.getUserId();
        MemberLevel level = memberService.getUserLevel(userId);
        return Result.success(level);
    }
}
