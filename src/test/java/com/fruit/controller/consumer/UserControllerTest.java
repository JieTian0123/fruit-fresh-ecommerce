package com.fruit.controller.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fruit.common.exception.GlobalExceptionHandler;
import com.fruit.dto.LoginDTO;
import com.fruit.dto.PasswordDTO;
import com.fruit.dto.RegisterDTO;
import com.fruit.dto.UserUpdateDTO;
import com.fruit.entity.User;
import com.fruit.service.UserService;
import com.fruit.utils.UserContext;
import com.fruit.vo.LoginVO;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
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
    void register() throws Exception {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("testuser");
        dto.setPassword("Test1234");

        mockMvc.perform(post("/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService).register(any(RegisterDTO.class));
    }

    @Test
    void login() throws Exception {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser");
        dto.setPassword("Test1234");
        dto.setCaptchaCode("1234");
        dto.setCaptchaUuid("uuid");

        LoginVO vo = new LoginVO();
        vo.setToken("token123");
        vo.setUsername("testuser");

        when(userService.login(any(LoginDTO.class))).thenReturn(vo);

        mockMvc.perform(post("/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("token123"));

        verify(userService).login(any(LoginDTO.class));
    }

    @Test
    void logout() throws Exception {
        mockMvc.perform(post("/user/logout")
                .header("Authorization", "token123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService).logout("token123");
    }

    @Test
    void info() throws Exception {
        LoginVO vo = new LoginVO();
        vo.setUsername("testuser");

        when(userService.getUserInfo(1L)).thenReturn(vo);

        mockMvc.perform(get("/user/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("testuser"));

        verify(userService).getUserInfo(1L);
    }

    @Test
    void updateProfile() throws Exception {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setNickname("newNick");

        mockMvc.perform(put("/user/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService).updateProfile(eq(1L), any(UserUpdateDTO.class));
    }

    @Test
    void changePassword() throws Exception {
        PasswordDTO dto = new PasswordDTO();
        dto.setOldPassword("Old12345");
        dto.setNewPassword("New12345");

        mockMvc.perform(put("/user/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService).changePassword(eq(1L), any(PasswordDTO.class));
    }
}
