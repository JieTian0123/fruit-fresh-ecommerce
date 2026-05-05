package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.common.exception.BusinessException;
import com.fruit.common.result.PageResult;
import com.fruit.entity.User;
import com.fruit.entity.UserPointsLog;
import com.fruit.mapper.UserMapper;
import com.fruit.mapper.UserPointsLogMapper;
import com.fruit.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointServiceImplTest {

    @InjectMocks
    private PointServiceImpl pointService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private MemberService memberService;

    @Mock
    private UserPointsLogMapper userPointsLogMapper;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(pointService, "baseMapper", userPointsLogMapper);
    }

    // addPoints tests
    @Test
    @DisplayName("增加积分 - 成功")
    void addPoints_Success() {
        Long userId = 1L;
        Integer points = 100;
        Integer sourceType = 1;
        String sourceId = "order123";
        String description = "购物赠送";

        User user = new User();
        user.setId(userId);
        user.setPoints(50);

        when(userMapper.selectById(userId)).thenReturn(user);
        when(userMapper.updateById(any(User.class))).thenReturn(1);
        when(userPointsLogMapper.insert(any(UserPointsLog.class))).thenReturn(1);

        pointService.addPoints(userId, points, sourceType, sourceId, description);

        assertEquals(150, user.getPoints());
        verify(userMapper).updateById(user);

        ArgumentCaptor<UserPointsLog> logCaptor = ArgumentCaptor.forClass(UserPointsLog.class);
        verify(userPointsLogMapper).insert(logCaptor.capture());
        UserPointsLog log = logCaptor.getValue();
        assertEquals(userId, log.getUserId());
        assertEquals(points, log.getPoints());
        assertEquals(sourceType, log.getSourceType());
        assertEquals(sourceId, log.getSourceId());
        assertEquals(description, log.getDescription());
        assertEquals(150, log.getBalanceAfter());

        verify(memberService).checkAndUpgradeLevel(userId);
    }

    @Test
    @DisplayName("增加积分 - 积分小于等于0")
    void addPoints_PointsLessThanOrEqualZero() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            pointService.addPoints(1L, 0, 1, "1", "desc");
        });
        assertEquals("积分数量必须大于0", exception.getMessage());

        exception = assertThrows(BusinessException.class, () -> {
            pointService.addPoints(1L, -10, 1, "1", "desc");
        });
        assertEquals("积分数量必须大于0", exception.getMessage());
    }

    @Test
    @DisplayName("增加积分 - 用户不存在")
    void addPoints_UserNotFound() {
        when(userMapper.selectById(1L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            pointService.addPoints(1L, 100, 1, "1", "desc");
        });
        assertEquals("用户不存在", exception.getMessage());
    }

    // deductPoints tests
    @Test
    @DisplayName("扣减积分 - 成功")
    void deductPoints_Success() {
        Long userId = 1L;
        Integer points = 50;
        Integer sourceType = 2;
        String sourceId = "exchange123";
        String description = "积分兑换";

        User user = new User();
        user.setId(userId);
        user.setPoints(100);

        when(userMapper.selectById(userId)).thenReturn(user);
        when(userMapper.updateById(any(User.class))).thenReturn(1);
        when(userPointsLogMapper.insert(any(UserPointsLog.class))).thenReturn(1);

        pointService.deductPoints(userId, points, sourceType, sourceId, description);

        assertEquals(50, user.getPoints());
        verify(userMapper).updateById(user);

        ArgumentCaptor<UserPointsLog> logCaptor = ArgumentCaptor.forClass(UserPointsLog.class);
        verify(userPointsLogMapper).insert(logCaptor.capture());
        UserPointsLog log = logCaptor.getValue();
        assertEquals(userId, log.getUserId());
        assertEquals(-points, log.getPoints());
        assertEquals(sourceType, log.getSourceType());
        assertEquals(sourceId, log.getSourceId());
        assertEquals(description, log.getDescription());
        assertEquals(50, log.getBalanceAfter());
    }

    @Test
    @DisplayName("扣减积分 - 积分小于等于0")
    void deductPoints_PointsLessThanOrEqualZero() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            pointService.deductPoints(1L, 0, 1, "1", "desc");
        });
        assertEquals("积分数量必须大于0", exception.getMessage());
    }

    @Test
    @DisplayName("扣减积分 - 用户不存在")
    void deductPoints_UserNotFound() {
        when(userMapper.selectById(1L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            pointService.deductPoints(1L, 100, 1, "1", "desc");
        });
        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    @DisplayName("扣减积分 - 积分不足")
    void deductPoints_NotEnoughPoints() {
        User user = new User();
        user.setId(1L);
        user.setPoints(50);

        when(userMapper.selectById(1L)).thenReturn(user);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            pointService.deductPoints(1L, 100, 1, "1", "desc");
        });
        assertEquals("积分不足", exception.getMessage());
    }

    // getPointsLog tests
    @Test
    @DisplayName("获取积分记录 - 成功")
    void getPointsLog_Success() {
        Long userId = 1L;
        Integer pageNum = 1;
        Integer pageSize = 10;

        UserPointsLog log1 = new UserPointsLog();
        log1.setId(1L);
        UserPointsLog log2 = new UserPointsLog();
        log2.setId(2L);
        List<UserPointsLog> records = Arrays.asList(log1, log2);

        Page<UserPointsLog> mockPage = new Page<>();
        mockPage.setCurrent(pageNum);
        mockPage.setSize(pageSize);
        mockPage.setTotal(2);
        mockPage.setRecords(records);

        when(userPointsLogMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        PageResult<UserPointsLog> result = pointService.getPointsLog(userId, pageNum, pageSize);

        assertNotNull(result);
        assertEquals(pageNum.longValue(), result.getPageNum());
        assertEquals(pageSize.longValue(), result.getPageSize());
        assertEquals(2L, result.getTotal());
        assertEquals(2, result.getList().size());
    }

    // getUserPoints tests
    @Test
    @DisplayName("获取用户积分 - 用户存在且有积分")
    void getUserPoints_UserFoundWithPoints() {
        User user = new User();
        user.setId(1L);
        user.setPoints(150);

        when(userMapper.selectById(1L)).thenReturn(user);

        Integer points = pointService.getUserPoints(1L);

        assertEquals(150, points);
    }

    @Test
    @DisplayName("获取用户积分 - 用户存在但积分为null")
    void getUserPoints_UserFoundNullPoints() {
        User user = new User();
        user.setId(1L);
        user.setPoints(null);

        when(userMapper.selectById(1L)).thenReturn(user);

        Integer points = pointService.getUserPoints(1L);

        assertEquals(0, points);
    }

    @Test
    @DisplayName("获取用户积分 - 用户不存在")
    void getUserPoints_UserNotFound() {
        when(userMapper.selectById(1L)).thenReturn(null);

        Integer points = pointService.getUserPoints(1L);

        assertEquals(0, points);
    }
}