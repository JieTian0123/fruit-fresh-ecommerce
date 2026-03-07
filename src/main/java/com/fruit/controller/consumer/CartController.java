package com.fruit.controller.consumer;

import com.fruit.common.result.Result;
import com.fruit.dto.CartDTO;
import com.fruit.service.CartService;
import com.fruit.utils.UserContext;
import com.fruit.vo.CartVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 消费者购物车控制器
 */
@Api(tags = "消费者-购物车")
@RestController
@RequestMapping("/consumer/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @ApiOperation("购物车列表")
    @GetMapping("/list")
    public Result<List<CartVO>> list() {
        Long userId = UserContext.getUserId();
        List<CartVO> voList = cartService.getCartList(userId);
        return Result.success(voList);
    }

    @ApiOperation("添加购物车")
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody CartDTO dto) {
        Long userId = UserContext.getUserId();
        cartService.addCart(userId, dto.getProductId(), dto.getQuantity());
        return Result.success();
    }

    @ApiOperation("修改购物车数量")
    @PutMapping("/update/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestParam Integer quantity) {
        Long userId = UserContext.getUserId();
        cartService.updateQuantity(userId, id, quantity);
        return Result.success();
    }

    @ApiOperation("删除购物车")
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        cartService.deleteCart(userId, id);
        return Result.success();
    }

    @ApiOperation("清空购物车")
    @DeleteMapping("/clear")
    public Result<Void> clear() {
        Long userId = UserContext.getUserId();
        cartService.clearCart(userId);
        return Result.success();
    }

    @ApiOperation("选中/取消选中商品")
    @PutMapping("/select/{id}")
    public Result<Void> select(@PathVariable Long id, @RequestParam Integer selected) {
        Long userId = UserContext.getUserId();
        cartService.updateSelected(userId, id, selected);
        return Result.success();
    }

    @ApiOperation("全选/取消全选")
    @PutMapping("/selectAll")
    public Result<Void> selectAll(@RequestParam Integer selected) {
        Long userId = UserContext.getUserId();
        cartService.selectAll(userId, selected);
        return Result.success();
    }
}
