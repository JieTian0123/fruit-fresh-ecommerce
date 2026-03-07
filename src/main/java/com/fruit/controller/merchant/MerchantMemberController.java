package com.fruit.controller.merchant;

import com.fruit.common.result.PageResult;
import com.fruit.common.result.Result;
import com.fruit.entity.MemberLevel;
import com.fruit.entity.User;
import com.fruit.mapper.MemberLevelMapper;
import com.fruit.mapper.OrderMapper;
import com.fruit.mapper.UserMapper;
import com.fruit.utils.UserContext;
import com.fruit.vo.MemberCustomerVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商家端-会员客户管理控制器
 */
@Api(tags = "商家-会员客户")
@RestController
@RequestMapping("/merchant/member-customers")
@RequiredArgsConstructor
public class MerchantMemberController {

    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final MemberLevelMapper memberLevelMapper;

    @ApiOperation("获取会员客户列表")
    @GetMapping("/list")
    public Result<PageResult<MemberCustomerVO>> listMemberCustomers(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {

        Long merchantId = UserContext.getUserId();

        // 获取总数
        Long total = orderMapper.countDistinctCustomers(merchantId);
        if (total == 0) {
            return Result.success(PageResult.of((long) pageNum, (long) pageSize, 0L, new ArrayList<>()));
        }

        // 分页查询客户统计
        int offset = (pageNum - 1) * pageSize;
        List<Map<String, Object>> customerStats = orderMapper.selectMemberCustomers(merchantId, offset, pageSize);

        if (customerStats.isEmpty()) {
            return Result.success(PageResult.of((long) pageNum, (long) pageSize, total, new ArrayList<>()));
        }

        // 批量查询用户信息
        List<Long> userIds = new ArrayList<>();
        for (Map<String, Object> stat : customerStats) {
            Object userIdObj = stat.get("user_id");
            if (userIdObj != null) {
                userIds.add(Long.valueOf(userIdObj.toString()));
            }
        }

        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Long, User> userMap = new HashMap<>();
        for (User user : users) {
            userMap.put(user.getId(), user);
        }

        // 批量查询会员等级
        List<MemberLevel> levels = memberLevelMapper.selectList(null);
        Map<Long, String> levelNameMap = new HashMap<>();
        for (MemberLevel level : levels) {
            levelNameMap.put(level.getId(), level.getLevelName());
        }

        // 组装VO
        List<MemberCustomerVO> voList = new ArrayList<>();
        for (Map<String, Object> stat : customerStats) {
            Long userId = Long.valueOf(stat.get("user_id").toString());
            User user = userMap.get(userId);
            if (user == null) {
                continue;
            }

            MemberCustomerVO vo = new MemberCustomerVO();
            vo.setUserId(userId);
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
            vo.setPhone(user.getPhone());

            // 会员等级名称
            if (user.getMemberLevelId() != null) {
                String levelName = levelNameMap.get(user.getMemberLevelId());
                vo.setMemberLevelName(levelName != null ? levelName : "普通用户");
            } else {
                vo.setMemberLevelName("普通用户");
            }

            // 平台VIP状态
            vo.setIsVip(user.getIsVip() != null ? user.getIsVip() : 0);

            // 统计信息
            Object orderCountObj = stat.get("order_count");
            vo.setOrderCount(orderCountObj != null ? Integer.valueOf(orderCountObj.toString()) : 0);

            Object totalSpendObj = stat.get("total_spend");
            vo.setTotalSpend(totalSpendObj != null ? new BigDecimal(totalSpendObj.toString()) : BigDecimal.ZERO);

            Object lastOrderTimeObj = stat.get("last_order_time");
            if (lastOrderTimeObj instanceof LocalDateTime) {
                vo.setLastOrderTime((LocalDateTime) lastOrderTimeObj);
            }

            voList.add(vo);
        }

        return Result.success(PageResult.of((long) pageNum, (long) pageSize, total, voList));
    }
}
