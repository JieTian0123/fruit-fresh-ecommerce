package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fruit.common.exception.BusinessException;
import com.fruit.entity.UserSignIn;
import com.fruit.mapper.UserSignInMapper;
import com.fruit.service.PointService;
import com.fruit.service.VipService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("签到服务测试")
class SignInServiceImplTest {

    @Mock
    private UserSignInMapper userSignInMapper;

    @Mock
    private PointService pointService;

    @Mock
    private VipService vipService;

    @InjectMocks
    private SignInServiceImpl signInService;

    @Test
    @DisplayName("签到 - 首次签到")
    void signIn_FirstDay() {
        Long userId = 1L;
        when(userSignInMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(userSignInMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(vipService.isVip(userId)).thenReturn(false);

        UserSignIn result = signInService.signIn(userId);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(LocalDate.now(), result.getSignDate());
        assertEquals(1, result.getContinuousDays());
        assertEquals(10, result.getPointsEarned());

        verify(userSignInMapper).insert(any(UserSignIn.class));
        verify(pointService).addPoints(eq(userId), eq(10), eq(2), isNull(), eq("每日签到奖励"));
    }

    @Test
    @DisplayName("签到 - 连续签到")
    void signIn_ConsecutiveDay() {
        Long userId = 1L;
        when(userSignInMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        UserSignIn yesterdaySignIn = new UserSignIn();
        yesterdaySignIn.setContinuousDays(2);
        when(userSignInMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(yesterdaySignIn);
        when(vipService.isVip(userId)).thenReturn(false);

        UserSignIn result = signInService.signIn(userId);

        assertNotNull(result);
        assertEquals(3, result.getContinuousDays());
        assertEquals(14, result.getPointsEarned()); // 10 + (3-1)*2 = 14

        verify(userSignInMapper).insert(any(UserSignIn.class));
        verify(pointService).addPoints(eq(userId), eq(14), eq(2), isNull(), eq("每日签到奖励（连续签到3天）"));
    }

    @Test
    @DisplayName("签到 - VIP双倍积分")
    void signIn_VipDoublePoints() {
        Long userId = 1L;
        when(userSignInMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(userSignInMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(vipService.isVip(userId)).thenReturn(true);

        UserSignIn result = signInService.signIn(userId);

        assertNotNull(result);
        assertEquals(1, result.getContinuousDays());
        assertEquals(20, result.getPointsEarned()); // 10 * 2 = 20

        verify(userSignInMapper).insert(any(UserSignIn.class));
        verify(pointService).addPoints(eq(userId), eq(20), eq(2), isNull(), eq("每日签到奖励（VIP双倍）"));
    }

    @Test
    @DisplayName("签到 - 今日已签到")
    void signIn_AlreadySigned() {
        Long userId = 1L;
        when(userSignInMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> signInService.signIn(userId));
        assertEquals("今日已签到", exception.getMessage());

        verify(userSignInMapper, never()).insert(any(UserSignIn.class));
        verify(pointService, never()).addPoints(anyLong(), anyInt(), anyInt(), any(), anyString());
    }

    @Test
    @DisplayName("检查今日是否已签到")
    void hasSignedToday() {
        Long userId = 1L;
        when(userSignInMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        boolean result = signInService.hasSignedToday(userId);

        assertTrue(result);
        verify(userSignInMapper).selectCount(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("获取连续签到天数 - 今日已签到")
    void getContinuousDays_TodaySigned() {
        Long userId = 1L;
        UserSignIn todaySignIn = new UserSignIn();
        todaySignIn.setContinuousDays(5);
        when(userSignInMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(todaySignIn);

        Integer days = signInService.getContinuousDays(userId);

        assertEquals(5, days);
    }

    @Test
    @DisplayName("获取连续签到天数 - 今日未签到，昨日已签到")
    void getContinuousDays_YesterdaySigned() {
        Long userId = 1L;
        when(userSignInMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(null) // today
                .thenReturn(new UserSignIn() {{ setContinuousDays(3); }}); // yesterday

        Integer days = signInService.getContinuousDays(userId);

        assertEquals(3, days);
    }

    @Test
    @DisplayName("获取连续签到天数 - 今日未签到，昨日未签到")
    void getContinuousDays_NotSigned() {
        Long userId = 1L;
        when(userSignInMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        Integer days = signInService.getContinuousDays(userId);

        assertEquals(0, days);
    }

    @Test
    @DisplayName("获取月度签到记录")
    void getMonthSignIns() {
        Long userId = 1L;
        Integer year = 2023;
        Integer month = 10;

        List<UserSignIn> expectedList = Arrays.asList(new UserSignIn(), new UserSignIn());
        when(userSignInMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(expectedList);

        List<UserSignIn> result = signInService.getMonthSignIns(userId, year, month);

        assertEquals(expectedList, result);
        verify(userSignInMapper).selectList(any(LambdaQueryWrapper.class));
    }
}
