package com.fruit.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态码
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAILED(500, "操作失败"),
    VALIDATE_FAILED(400, "参数校验失败"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有相关权限"),
    NOT_FOUND(404, "资源不存在"),

    // 用户相关
    USER_NOT_EXIST(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "密码错误"),
    OLD_PASSWORD_ERROR(1007, "原密码错误"),
    USER_DISABLED(1003, "用户已被禁用"),
    USER_PENDING(1004, "用户待审核"),
    USERNAME_EXIST(1005, "用户名已存在"),
    PHONE_EXIST(1006, "手机号已注册"),

    // 商品相关
    PRODUCT_NOT_EXIST(2001, "商品不存在"),
    PRODUCT_OFF_SHELF(2002, "商品已下架"),
    PRODUCT_STOCK_NOT_ENOUGH(2003, "商品库存不足"),
    STOCK_NOT_ENOUGH(2004, "库存不足"),
    TRACEABILITY_NOT_EXIST(2005, "溯源记录不存在"),

    // 订单相关
    ORDER_NOT_EXIST(3001, "订单不存在"),
    ORDER_STATUS_ERROR(3002, "订单状态错误"),
    ORDER_CANNOT_CANCEL(3003, "订单无法取消"),
    ORDER_CANNOT_PAY(3004, "订单无法支付"),
    ORDER_CANNOT_DELIVER(3005, "订单无法发货"),
    ORDER_CANNOT_CONFIRM(3006, "订单无法确认收货"),
    ORDER_CANNOT_REFUND(3007, "订单无法退款"),

    // 购物车相关
    CART_EMPTY(4001, "购物车为空"),
    CART_ITEM_NOT_EXIST(4002, "购物车商品不存在"),
    CART_NOT_EXIST(4003, "购物车不存在"),

    // 地址相关
    ADDRESS_NOT_EXIST(5001, "地址不存在"),

    // 分类相关
    CATEGORY_NOT_EXIST(6001, "分类不存在"),
    CATEGORY_HAS_CHILDREN(6002, "分类下有子分类，无法删除"),

    // 店铺相关
    SHOP_NOT_EXIST(7001, "店铺不存在"),
    SHOP_ALREADY_EXIST(7002, "店铺已存在"),

    // 轮播图相关
    BANNER_NOT_EXIST(8001, "轮播图不存在"),

    // 评价相关
    REVIEW_NOT_EXIST(10001, "评价不存在"),
    REVIEW_ALREADY_EXIST(10002, "已经评价过了"),
    REVIEW_ORDER_NOT_COMPLETED(10003, "订单未完成，无法评价"),
    REVIEW_PRODUCT_NOT_IN_ORDER(10004, "商品不在订单中"),

    // 积分相关
    POINTS_NOT_ENOUGH(11001, "积分不足"),
    ALREADY_SIGNED_TODAY(11002, "今日已签到"),

    // 通用
    DATA_NOT_EXIST(9001, "数据不存在");

    private final Integer code;
    private final String message;
}
