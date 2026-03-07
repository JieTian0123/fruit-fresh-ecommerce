package com.fruit.controller.consumer;

import com.fruit.common.result.PageResult;
import com.fruit.common.result.Result;
import com.fruit.dto.OrderCreateDTO;
import com.fruit.entity.Order;
import com.fruit.service.OrderService;
import com.fruit.utils.UserContext;
import com.fruit.vo.OrderDetailVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 消费者订单控制器
 */
@Api(tags = "消费者-订单")
@RestController
@RequestMapping("/consumer/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @ApiOperation("订单列表")
    @GetMapping("/list")
    public Result<PageResult<Order>> list(
            @ApiParam("订单状态") @RequestParam(required = false) Integer status,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {

        Long userId = UserContext.getUserId();
        PageResult<Order> pageResult = orderService.listForConsumer(userId, status, pageNum, pageSize);
        return Result.success(pageResult);
    }

    @ApiOperation("订单详情")
    @GetMapping("/detail/{orderNo}")
    public Result<OrderDetailVO> detail(@PathVariable String orderNo) {
        Long userId = UserContext.getUserId();
        OrderDetailVO vo = orderService.getOrderDetail(orderNo, userId);
        return Result.success(vo);
    }

    @ApiOperation("创建订单")
    @PostMapping("/create")
    public Result<String> create(@Valid @RequestBody OrderCreateDTO dto) {
        Long userId = UserContext.getUserId();
        String orderNo = orderService.createOrder(userId, dto);
        return Result.success(orderNo);
    }

    @ApiOperation("取消订单")
    @PutMapping("/cancel/{orderNo}")
    public Result<Void> cancel(@PathVariable String orderNo) {
        Long userId = UserContext.getUserId();
        orderService.cancelOrder(userId, orderNo);
        return Result.success();
    }

    @ApiOperation("支付订单")
    @PutMapping("/pay/{orderNo}")
    public Result<Void> pay(@PathVariable String orderNo) {
        Long userId = UserContext.getUserId();
        orderService.payOrder(userId, orderNo);
        return Result.success();
    }

    @ApiOperation("确认收货")
    @PutMapping("/confirm/{orderNo}")
    public Result<Void> confirm(@PathVariable String orderNo) {
        Long userId = UserContext.getUserId();
        orderService.confirmReceive(userId, orderNo);
        return Result.success();
    }

    @ApiOperation("申请退款")
    @PutMapping("/refund/{orderNo}")
    public Result<Void> refund(@PathVariable String orderNo) {
        Long userId = UserContext.getUserId();
        orderService.refundApply(userId, orderNo);
        return Result.success();
    }
}
