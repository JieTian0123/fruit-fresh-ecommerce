package com.fruit.controller.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fruit.common.exception.GlobalExceptionHandler;
import com.fruit.dto.AddressDTO;
import com.fruit.entity.Address;
import com.fruit.entity.User;
import com.fruit.service.AddressService;
import com.fruit.utils.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AddressControllerTest {

    @InjectMocks
    private AddressController addressController;

    @Mock
    private AddressService addressService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(addressController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        User user = new User();
        user.setId(1L);
        user.setUserType(0);
        UserContext.setUser(user);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void list() throws Exception {
        Address address = new Address();
        address.setId(1L);
        address.setReceiverName("John");
        List<Address> list = Arrays.asList(address);

        when(addressService.getAddressList(1L)).thenReturn(list);

        mockMvc.perform(get("/consumer/address/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].receiverName").value("John"));

        verify(addressService).getAddressList(1L);
    }

    @Test
    void add() throws Exception {
        AddressDTO dto = new AddressDTO();
        dto.setReceiverName("John");
        dto.setReceiverPhone("13800138000");
        dto.setProvince("Guangdong");
        dto.setCity("Shenzhen");
        dto.setDistrict("Nanshan");
        dto.setDetailAddress("Tech Park");

        mockMvc.perform(post("/consumer/address/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(addressService).addAddress(eq(1L), any(AddressDTO.class));
    }

    @Test
    void update() throws Exception {
        AddressDTO dto = new AddressDTO();
        dto.setReceiverName("John");
        dto.setReceiverPhone("13800138000");
        dto.setProvince("Guangdong");
        dto.setCity("Shenzhen");
        dto.setDistrict("Nanshan");
        dto.setDetailAddress("Tech Park");

        mockMvc.perform(put("/consumer/address/update/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(addressService).updateAddress(eq(1L), eq(1L), any(AddressDTO.class));
    }

    @Test
    void deleteAddress() throws Exception {
        mockMvc.perform(delete("/consumer/address/delete/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(addressService).deleteAddress(1L, 1L);
    }

    @Test
    void setDefault() throws Exception {
        mockMvc.perform(put("/consumer/address/setDefault/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(addressService).setDefault(1L, 1L);
    }

    @Test
    void getDefault() throws Exception {
        Address address = new Address();
        address.setId(1L);
        address.setReceiverName("John");

        when(addressService.getDefaultAddress(1L)).thenReturn(address);

        mockMvc.perform(get("/consumer/address/default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.receiverName").value("John"));

        verify(addressService).getDefaultAddress(1L);
    }
}
