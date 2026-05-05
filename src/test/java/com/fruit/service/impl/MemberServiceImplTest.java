package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fruit.entity.MemberLevel;
import com.fruit.entity.User;
import com.fruit.mapper.MemberLevelMapper;
import com.fruit.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("会员服务测试")
class MemberServiceImplTest {

    @Mock
    private MemberLevelMapper memberLevelMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private MemberServiceImpl memberService;

    @Test
    @DisplayName("检查并升级等级 - 需要升级")
    void checkAndUpgradeLevel_UpgradeNeeded() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setPoints(1500);
        user.setMemberLevelId(1L); // Current level ID

        when(userMapper.selectById(userId)).thenReturn(user);

        MemberLevel level1 = new MemberLevel();
        level1.setId(1L);
        level1.setRequiredPoints(0);

        MemberLevel level2 = new MemberLevel();
        level2.setId(2L);
        level2.setRequiredPoints(1000);

        MemberLevel level3 = new MemberLevel();
        level3.setId(3L);
        level3.setRequiredPoints(2000);

        // Mocking the list returned in descending order of required points
        when(memberLevelMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(level3, level2, level1));

        memberService.checkAndUpgradeLevel(userId);

        assertEquals(2L, user.getMemberLevelId());
        verify(userMapper).updateById(user);
    }

    @Test
    @DisplayName("检查并升级等级 - 不需要升级")
    void checkAndUpgradeLevel_NoUpgrade() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setPoints(1500);
        user.setMemberLevelId(2L); // Already at the correct level

        when(userMapper.selectById(userId)).thenReturn(user);

        MemberLevel level1 = new MemberLevel();
        level1.setId(1L);
        level1.setRequiredPoints(0);

        MemberLevel level2 = new MemberLevel();
        level2.setId(2L);
        level2.setRequiredPoints(1000);

        MemberLevel level3 = new MemberLevel();
        level3.setId(3L);
        level3.setRequiredPoints(2000);

        when(memberLevelMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(level3, level2, level1));

        memberService.checkAndUpgradeLevel(userId);

        assertEquals(2L, user.getMemberLevelId());
        verify(userMapper, never()).updateById(any(User.class));
    }

    @Test
    @DisplayName("检查并升级等级 - 用户不存在")
    void checkAndUpgradeLevel_UserNull() {
        Long userId = 1L;
        when(userMapper.selectById(userId)).thenReturn(null);

        memberService.checkAndUpgradeLevel(userId);

        verify(memberLevelMapper, never()).selectList(any(LambdaQueryWrapper.class));
        verify(userMapper, never()).updateById(any(User.class));
    }

    @Test
    @DisplayName("获取所有会员等级")
    void getAllLevels() {
        List<MemberLevel> expectedLevels = Arrays.asList(new MemberLevel(), new MemberLevel());
        when(memberLevelMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(expectedLevels);

        List<MemberLevel> result = memberService.getAllLevels();

        assertEquals(expectedLevels, result);
        verify(memberLevelMapper).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("获取用户等级 - 用户有等级")
    void getUserLevel_WithLevel() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setMemberLevelId(2L);

        when(userMapper.selectById(userId)).thenReturn(user);

        MemberLevel expectedLevel = new MemberLevel();
        expectedLevel.setId(2L);
        when(memberLevelMapper.selectById(2L)).thenReturn(expectedLevel);

        MemberLevel result = memberService.getUserLevel(userId);

        assertEquals(expectedLevel, result);
    }

    @Test
    @DisplayName("获取用户等级 - 用户无等级")
    void getUserLevel_WithoutLevel() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setMemberLevelId(null);

        when(userMapper.selectById(userId)).thenReturn(user);

        MemberLevel defaultLevel = new MemberLevel();
        defaultLevel.setLevelCode("NORMAL");
        when(memberLevelMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(defaultLevel);

        MemberLevel result = memberService.getUserLevel(userId);

        assertEquals(defaultLevel, result);
        verify(memberLevelMapper).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("获取用户等级 - 用户不存在")
    void getUserLevel_UserNull() {
        Long userId = 1L;
        when(userMapper.selectById(userId)).thenReturn(null);

        MemberLevel defaultLevel = new MemberLevel();
        defaultLevel.setLevelCode("NORMAL");
        when(memberLevelMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(defaultLevel);

        MemberLevel result = memberService.getUserLevel(userId);

        assertEquals(defaultLevel, result);
        verify(memberLevelMapper).selectOne(any(LambdaQueryWrapper.class));
    }
}
