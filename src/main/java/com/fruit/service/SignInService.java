package com.fruit.service;

import com.fruit.entity.UserSignIn;

import java.time.LocalDate;
import java.util.List;

/**
 * 签到服务接口
 */
public interface SignInService {

    /**
     * 签到
     */
    UserSignIn signIn(Long userId);

    /**
     * 检查今日是否已签到
     */
    boolean hasSignedToday(Long userId);

    /**
     * 获取连续签到天数
     */
    Integer getContinuousDays(Long userId);

    /**
     * 获取本月签到记录
     */
    List<UserSignIn> getMonthSignIns(Long userId, Integer year, Integer month);
}
