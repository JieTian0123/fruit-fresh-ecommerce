package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fruit.common.exception.BusinessException;
import com.fruit.entity.UserSignIn;
import com.fruit.mapper.UserSignInMapper;
import com.fruit.service.PointService;
import com.fruit.service.SignInService;
import com.fruit.service.VipService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 签到服务实现类
 */
@Service
@RequiredArgsConstructor
public class SignInServiceImpl implements SignInService {

    private final UserSignInMapper userSignInMapper;
    private final PointService pointService;
    private final VipService vipService;

    /**
     * 基础签到积分
     */
    private static final int BASE_SIGN_IN_POINTS = 10;

    /**
     * 连续签到额外奖励(每增加1天多奖励2积分，最多额外10积分)
     */
    private static final int EXTRA_POINTS_PER_DAY = 2;
    private static final int MAX_EXTRA_POINTS = 10;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserSignIn signIn(Long userId) {
        LocalDate today = LocalDate.now();

        // 检查今日是否已签到
        if (hasSignedToday(userId)) {
            throw new BusinessException("今日已签到");
        }

        // 查询昨天的签到记录以计算连续天数
        LocalDate yesterday = today.minusDays(1);
        LambdaQueryWrapper<UserSignIn> yesterdayWrapper = new LambdaQueryWrapper<>();
        yesterdayWrapper.eq(UserSignIn::getUserId, userId);
        yesterdayWrapper.eq(UserSignIn::getSignDate, yesterday);
        UserSignIn yesterdaySignIn = userSignInMapper.selectOne(yesterdayWrapper);

        int continuousDays = 1;
        if (yesterdaySignIn != null) {
            continuousDays = yesterdaySignIn.getContinuousDays() + 1;
        }

        // 计算签到积分: 基础10分 + 连续签到额外奖励
        int extraPoints = Math.min((continuousDays - 1) * EXTRA_POINTS_PER_DAY, MAX_EXTRA_POINTS);
        int pointsEarned = BASE_SIGN_IN_POINTS + extraPoints;

        // VIP用户签到积分双倍
        boolean isVipUser = false;
        try {
            isVipUser = vipService.isVip(userId);
        } catch (Exception e) {
            // VIP查询失败不影响签到
        }
        if (isVipUser) {
            pointsEarned *= 2;
        }

        // 创建签到记录
        UserSignIn signIn = new UserSignIn();
        signIn.setUserId(userId);
        signIn.setSignDate(today);
        signIn.setContinuousDays(continuousDays);
        signIn.setPointsEarned(pointsEarned);
        userSignInMapper.insert(signIn);

        // 添加积分
        String desc = "每日签到奖励" +
                (continuousDays > 1 ? "（连续签到" + continuousDays + "天）" : "");
        if (isVipUser) {
            desc += "（VIP双倍）";
        }
        pointService.addPoints(userId, pointsEarned, 2, null, desc);

        return signIn;
    }

    @Override
    public boolean hasSignedToday(Long userId) {
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<UserSignIn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSignIn::getUserId, userId);
        wrapper.eq(UserSignIn::getSignDate, today);
        return userSignInMapper.selectCount(wrapper) > 0;
    }

    @Override
    public Integer getContinuousDays(Long userId) {
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<UserSignIn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSignIn::getUserId, userId);
        wrapper.eq(UserSignIn::getSignDate, today);
        UserSignIn todaySignIn = userSignInMapper.selectOne(wrapper);

        if (todaySignIn != null) {
            return todaySignIn.getContinuousDays();
        }

        // 如果今天没签到，检查昨天的记录
        LocalDate yesterday = today.minusDays(1);
        LambdaQueryWrapper<UserSignIn> yesterdayWrapper = new LambdaQueryWrapper<>();
        yesterdayWrapper.eq(UserSignIn::getUserId, userId);
        yesterdayWrapper.eq(UserSignIn::getSignDate, yesterday);
        UserSignIn yesterdaySignIn = userSignInMapper.selectOne(yesterdayWrapper);

        return yesterdaySignIn != null ? yesterdaySignIn.getContinuousDays() : 0;
    }

    @Override
    public List<UserSignIn> getMonthSignIns(Long userId, Integer year, Integer month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        LambdaQueryWrapper<UserSignIn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSignIn::getUserId, userId);
        wrapper.ge(UserSignIn::getSignDate, startDate);
        wrapper.le(UserSignIn::getSignDate, endDate);
        wrapper.orderByAsc(UserSignIn::getSignDate);

        return userSignInMapper.selectList(wrapper);
    }
}
