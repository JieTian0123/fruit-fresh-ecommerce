package com.fruit.controller.merchant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fruit.common.exception.GlobalExceptionHandler;
import com.fruit.dto.LoginDTO;
import com.fruit.dto.RegisterDTO;
import com.fruit.service.UserService;
import com.fruit.vo.LoginVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MerchantUserControllerTest {

    @InjectMocks
    private MerchantUserController controller;

    @Mock
    private UserService userService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void register_Success() throws Exception {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("merchant");
        dto.setPassword("Password123");
        dto.setPhone("13800138000");

        mockMvc.perform(post("/merchant/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService).merchantRegister(any(RegisterDTO.class));
    }

    @Test
    void login_Success() throws Exception {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("merchant");
        dto.setPassword("Password123");
        dto.setCaptchaUuid("uuid123");
        dto.setCaptchaCode("1234");

        LoginVO vo = new LoginVO();
        vo.setToken("token123");
        when(userService.merchantLogin(any(LoginDTO.class))).thenReturn(vo);

        mockMvc.perform(post("/merchant/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("token123"));
    }

    @Test
    void logout_Success() throws Exception {
        mockMvc.perform(post("/merchant/logout")
                .header("Authorization", "Bearer token123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService).logout(anyString());
    }
}