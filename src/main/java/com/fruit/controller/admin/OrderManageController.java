package com.fruit.controller.admin;

import com.fruit.common.result.PageResult;
import com.fruit.common.result.Result;
import com.fruit.entity.Order;
import com.fruit.service.OrderService;
import com.fruit.vo.OrderDetailVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员订单管理控制器
 */
@Api(tags = "管理员-订单管理")
@RestController
@RequestMapping("/admin/order")
@RequiredArgsConstructor
public class OrderManageController {

    private final OrderService orderService;

    @ApiOperation("订单列表")
    @GetMapping("/list")
    public Result<PageResult<Order>> list(
            @ApiParam("订单状态") @RequestParam(required = false) Integer status,
            @ApiParam("订单编号") @RequestParam(required = false) String orderNo,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {

        PageResult<Order> pageResult = orderService.listForAdmin(status, orderNo, pageNum, pageSize);
        return Result.success(pageResult);
    }

    @ApiOperation("订单详情")
    @GetMapping("/detail/{orderNo}")
    public Result<OrderDetailVO> detail(@PathVariable String orderNo) {
        OrderDetailVO vo = orderService.getOrderDetail(orderNo, null);
        return Result.success(vo);
    }
}
