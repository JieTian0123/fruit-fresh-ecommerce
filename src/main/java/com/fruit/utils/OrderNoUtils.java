package com.fruit.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;

import java.util.Date;

/**
 * 订单号生成工具类
 */
public class OrderNoUtils {

    /**
     * 生成订单号
     * 格式: 年月日时分秒 + 6位随机数
     */
    public static String generateOrderNo() {
        String dateStr = DateUtil.format(new Date(), "yyyyMMddHHmmss");
        String randomStr = RandomUtil.randomNumbers(6);
        return dateStr + randomStr;
    }
}
