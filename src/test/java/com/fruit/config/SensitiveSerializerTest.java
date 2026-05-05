package com.fruit.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fruit.entity.Order;
import com.fruit.vo.LoginVO;
import com.fruit.vo.OrderDetailVO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SensitiveSerializerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldMaskLoginVoPhoneAndEmail() throws Exception {
        LoginVO vo = new LoginVO();
        vo.setPhone("13812341234");
        vo.setEmail("test@example.com");

        String json = objectMapper.writeValueAsString(vo);

        assertTrue(json.contains("\"phone\":\"138****1234\""));
        assertTrue(json.contains("\"email\":\"t***@example.com\""));
    }

    @Test
    void shouldMaskOrderEntityReceiverFields() throws Exception {
        Order order = new Order();
        order.setReceiverName("张三");
        order.setReceiverPhone("13812341234");
        order.setReceiverAddress("广东省深圳市南山区科技园科苑路15号");

        String json = objectMapper.writeValueAsString(order);

        assertTrue(json.contains("\"receiverName\":\"张**\""));
        assertTrue(json.contains("\"receiverPhone\":\"138****1234\""));
        assertTrue(json.contains("\"receiverAddress\":\"广东省深圳市****\""));
    }

    @Test
    void shouldMaskOrderDetailVoReceiverFields() throws Exception {
        OrderDetailVO vo = new OrderDetailVO();
        vo.setReceiverName("李四");
        vo.setReceiverPhone("13912345678");
        vo.setReceiverAddress("浙江省杭州市西湖区文三路90号");

        String json = objectMapper.writeValueAsString(vo);

        assertTrue(json.contains("\"receiverName\":\"李**\""));
        assertTrue(json.contains("\"receiverPhone\":\"139****5678\""));
        assertTrue(json.contains("\"receiverAddress\":\"浙江省杭州市****\""));
    }
}
