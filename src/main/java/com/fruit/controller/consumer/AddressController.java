package com.fruit.controller.consumer;

import com.fruit.common.result.Result;
import com.fruit.dto.AddressDTO;
import com.fruit.entity.Address;
import com.fruit.service.AddressService;
import com.fruit.utils.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 消费者地址控制器
 */
@Api(tags = "消费者-地址")
@RestController
@RequestMapping("/consumer/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @ApiOperation("地址列表")
    @GetMapping("/list")
    public Result<List<Address>> list() {
        Long userId = UserContext.getUserId();
        List<Address> list = addressService.getAddressList(userId);
        return Result.success(list);
    }

    @ApiOperation("添加地址")
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody AddressDTO dto) {
        Long userId = UserContext.getUserId();
        addressService.addAddress(userId, dto);
        return Result.success();
    }

    @ApiOperation("修改地址")
    @PutMapping("/update/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody AddressDTO dto) {
        Long userId = UserContext.getUserId();
        addressService.updateAddress(userId, id, dto);
        return Result.success();
    }

    @ApiOperation("删除地址")
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        addressService.deleteAddress(userId, id);
        return Result.success();
    }

    @ApiOperation("设为默认地址")
    @PutMapping("/setDefault/{id}")
    public Result<Void> setDefault(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        addressService.setDefault(userId, id);
        return Result.success();
    }

    @ApiOperation("获取默认地址")
    @GetMapping("/default")
    public Result<Address> getDefault() {
        Long userId = UserContext.getUserId();
        Address address = addressService.getDefaultAddress(userId);
        return Result.success(address);
    }
}
