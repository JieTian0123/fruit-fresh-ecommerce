package com.fruit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fruit.entity.MemberLevel;
import com.fruit.entity.User;
import com.fruit.mapper.MemberLevelMapper;
import com.fruit.mapper.UserMapper;
import com.fruit.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 会员服务实现类
 */
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberLevelMapper memberLevelMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkAndUpgradeLevel(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }

        Integer currentPoints = user.getPoints() != null ? user.getPoints() : 0;

        // 查询所有启用的会员等级，按所需积分降序排列
        LambdaQueryWrapper<MemberLevel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberLevel::getStatus, 1);
        wrapper.orderByDesc(MemberLevel::getRequiredPoints);
        List<MemberLevel> levels = memberLevelMapper.selectList(wrapper);

        // 从最高等级开始匹配
        for (MemberLevel level : levels) {
            if (currentPoints >= level.getRequiredPoints()) {
                // 如果当前等级不同，则升级
                if (user.getMemberLevelId() == null || !user.getMemberLevelId().equals(level.getId())) {
                    user.setMemberLevelId(level.getId());
                    userMapper.updateById(user);
                }
                break;
            }
        }
    }

    @Override
    public List<MemberLevel> getAllLevels() {
        LambdaQueryWrapper<MemberLevel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberLevel::getStatus, 1);
        wrapper.orderByAsc(MemberLevel::getSort);
        return memberLevelMapper.selectList(wrapper);
    }

    @Override
    public MemberLevel getUserLevel(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getMemberLevelId() == null) {
            // 返回默认普通会员等级
            LambdaQueryWrapper<MemberLevel> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MemberLevel::getLevelCode, "NORMAL");
            return memberLevelMapper.selectOne(wrapper);
        }
        return memberLevelMapper.selectById(user.getMemberLevelId());
    }
}
