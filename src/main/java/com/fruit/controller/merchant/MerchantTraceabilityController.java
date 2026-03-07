package com.fruit.controller.merchant;

import com.fruit.common.result.Result;
import com.fruit.dto.TraceabilityDTO;
import com.fruit.entity.ProductTraceability;
import com.fruit.service.ProductTraceabilityService;
import com.fruit.utils.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 商家溯源管理控制器
 */
@Api(tags = "商家-商品溯源")
@RestController
@RequestMapping("/merchant/traceability")
@RequiredArgsConstructor
public class MerchantTraceabilityController {

    private final ProductTraceabilityService traceabilityService;

    @ApiOperation("溯源记录列表")
    @GetMapping("/list/{productId}")
    public Result<List<ProductTraceability>> list(@PathVariable Long productId) {
        return Result.success(traceabilityService.listByProductId(productId));
    }

    @ApiOperation("添加溯源记录")
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody TraceabilityDTO dto) {
        Long merchantId = UserContext.getUserId();
        traceabilityService.addTraceability(merchantId, dto);
        return Result.success();
    }

    @ApiOperation("删除溯源记录")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long merchantId = UserContext.getUserId();
        traceabilityService.deleteTraceability(merchantId, id);
        return Result.success();
    }
}
