package com.fruit.controller.consumer;

import com.fruit.common.result.Result;
import com.fruit.entity.Banner;
import com.fruit.entity.Product;
import com.fruit.service.BannerService;
import com.fruit.service.ProductService;
import com.fruit.vo.HomeActivityVO;
import com.fruit.vo.HomeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 消费者首页控制器
 */
@Api(tags = "消费者-首页")
@RestController
@RequestMapping("/home")
@RequiredArgsConstructor
public class HomeController {

    private final BannerService bannerService;
    private final ProductService productService;

    @ApiOperation("首页数据")
    @GetMapping("/index")
    public Result<HomeVO> index() {
        HomeVO vo = new HomeVO();

        // 轮播图
        List<Banner> banners = bannerService.getHomeBanners();
        vo.setBanners(banners);

        // 热销商品
        List<Product> hotProducts = productService.listForConsumer(null, null, "sales", "desc", 1, 8).getList();
        vo.setHotProducts(hotProducts);

        // 新品上市
        List<Product> newProducts = productService.listForConsumer(null, null, "createTime", "desc", 1, 8).getList();
        vo.setNewProducts(newProducts);

        return Result.success(vo);
    }

    @ApiOperation("首页固定活动")
    @GetMapping("/activities")
    public Result<List<HomeActivityVO>> activities() {
        return Result.success(bannerService.getEnabledHomeActivities());
    }
}
