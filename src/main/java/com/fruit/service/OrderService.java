package com.fruit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruit.common.result.PageResult;
import com.fruit.dto.DeliverDTO;
import com.fruit.dto.OrderCreateDTO;
import com.fruit.entity.Order;
import com.fruit.vo.OrderDetailVO;

/**
 * 订单服务接口
 */
public interface OrderService extends IService<Order> {

    /**
     * 消费者订单列表
     */
    PageResult<Order> listForConsumer(Long userId, Integer status, Integer pageNum, Integer pageSize);

    /**
     * 商家订单列表
     */
    PageResult<Order> listForMerchant(Long merchantId, Integer status, Integer pageNum, Integer pageSize);

    /**
     * 管理员订单列表
     */
    PageResult<Order> listForAdmin(Integer status, String orderNo, Integer pageNum, Integer pageSize);

    /**
     * 订单详情
     */
    OrderDetailVO getOrderDetail(String orderNo, Long userId);

    /**
     * 创建订单
     */
    String createOrder(Long userId, OrderCreateDTO dto);

    /**
     * 取消订单
     */
    void cancelOrder(Long userId, String orderNo);

    /**
     * 支付订单
     */
    void payOrder(Long userId, String orderNo);

    /**
     * 商家发货
     */
    void deliverOrder(Long merchantId, DeliverDTO dto);

    /**
     * 确认收货
     */
    void confirmReceive(Long userId, String orderNo);

    /**
     * 申请退款
     */
    void refundApply(Long userId, String orderNo);
}
