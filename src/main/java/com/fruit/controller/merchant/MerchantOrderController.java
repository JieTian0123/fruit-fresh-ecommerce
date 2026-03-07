package com.fruit.controller.merchant;

import com.fruit.common.result.PageResult;
import com.fruit.common.result.Result;
import com.fruit.dto.DeliverDTO;
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
 * 商家订单控制器
 */
@Api(tags = "商家-订单")
@RestController
@RequestMapping("/merchant/order")
@RequiredArgsConstructor
public class MerchantOrderController {

    private final OrderService orderService;

    @ApiOperation("订单列表")
    @GetMapping("/list")
    public Result<PageResult<Order>> list(
            @ApiParam("订单状态") @RequestParam(required = false) Integer status,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {

        Long merchantId = UserContext.getUserId();
        PageResult<Order> pageResult = orderService.listForMerchant(merchantId, status, pageNum, pageSize);
        return Result.success(pageResult);
    }

    @ApiOperation("订单详情")
    @GetMapping("/detail/{orderNo}")
    public Result<OrderDetailVO> detail(@PathVariable String orderNo) {
        OrderDetailVO vo = orderService.getOrderDetail(orderNo, null);
        return Result.success(vo);
    }

    @ApiOperation("订单发货")
    @PostMapping("/deliver")
    public Result<Void> deliver(@Valid @RequestBody DeliverDTO dto) {
        Long merchantId = UserContext.getUserId();
        orderService.deliverOrder(merchantId, dto);
        return Result.success();
    }
}
