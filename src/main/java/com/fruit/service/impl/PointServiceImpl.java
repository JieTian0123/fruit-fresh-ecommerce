package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruit.common.exception.BusinessException;
import com.fruit.common.result.PageResult;
import com.fruit.entity.User;
import com.fruit.entity.UserPointsLog;
import com.fruit.mapper.UserMapper;
import com.fruit.mapper.UserPointsLogMapper;
import com.fruit.service.MemberService;
import com.fruit.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 积分服务实现类
 */
@Service
@RequiredArgsConstructor
public class PointServiceImpl extends ServiceImpl<UserPointsLogMapper, UserPointsLog> implements PointService {

    private final UserMapper userMapper;

    @Lazy
    private final MemberService memberService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPoints(Long userId, Integer points, Integer sourceType, String sourceId, String description) {
        if (points <= 0) {
            throw new BusinessException("积分数量必须大于0");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 更新用户积分
        Integer currentPoints = user.getPoints() != null ? user.getPoints() : 0;
        Integer newPoints = currentPoints + points;
        user.setPoints(newPoints);
        userMapper.updateById(user);

        // 记录积分变动
        UserPointsLog log = new UserPointsLog();
        log.setUserId(userId);
        log.setPoints(points);
        log.setSourceType(sourceType);
        log.setSourceId(sourceId);
        log.setDescription(description);
        log.setBalanceAfter(newPoints);
        baseMapper.insert(log);

        // 检查是否需要升级会员等级
        memberService.checkAndUpgradeLevel(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductPoints(Long userId, Integer points, Integer sourceType, String sourceId, String description) {
        if (points <= 0) {
            throw new BusinessException("积分数量必须大于0");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        Integer currentPoints = user.getPoints() != null ? user.getPoints() : 0;
        if (currentPoints < points) {
            throw new BusinessException("积分不足");
        }

        // 更新用户积分
        Integer newPoints = currentPoints - points;
        user.setPoints(newPoints);
        userMapper.updateById(user);

        // 记录积分变动(负数)
        UserPointsLog log = new UserPointsLog();
        log.setUserId(userId);
        log.setPoints(-points);
        log.setSourceType(sourceType);
        log.setSourceId(sourceId);
        log.setDescription(description);
        log.setBalanceAfter(newPoints);
        baseMapper.insert(log);
    }

    @Override
    public PageResult<UserPointsLog> getPointsLog(Long userId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<UserPointsLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPointsLog::getUserId, userId);
        wrapper.orderByDesc(UserPointsLog::getCreateTime);

        Page<UserPointsLog> page = new Page<>(pageNum, pageSize);
        Page<UserPointsLog> result = baseMapper.selectPage(page, wrapper);

        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    public Integer getUserPoints(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return 0;
        }
        return user.getPoints() != null ? user.getPoints() : 0;
    }
}
