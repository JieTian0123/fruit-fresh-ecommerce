package com.fruit.controller.consumer;

import com.fruit.common.result.PageResult;
import com.fruit.common.result.Result;
import com.fruit.entity.SystemNotification;
import com.fruit.service.NotificationService;
import com.fruit.utils.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 消费者通知控制器
 */
@Api(tags = "消费者-通知")
@RestController
@RequestMapping("/consumer/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @ApiOperation("获取通知列表")
    @GetMapping("/list")
    public Result<PageResult<SystemNotification>> getNotifications(
            @ApiParam("类型: order/system/promotion") @RequestParam(required = false) String type,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {

        Long userId = UserContext.getUserId();
        PageResult<SystemNotification> result = notificationService.getNotifications(userId, type, pageNum, pageSize);
        return Result.success(result);
    }

    @ApiOperation("标记通知为已读")
    @PutMapping("/read/{id}")
    public Result<Void> markAsRead(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        notificationService.markAsRead(userId, id);
        return Result.success();
    }

    @ApiOperation("标记所有通知为已读")
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead() {
        Long userId = UserContext.getUserId();
        notificationService.markAllAsRead(userId);
        return Result.success();
    }

    @ApiOperation("获取未读通知数量")
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount() {
        Long userId = UserContext.getUserId();
        Integer count = notificationService.getUnreadCount(userId);
        return Result.success(count);
    }
}
