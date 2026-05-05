package com.fruit.controller.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fruit.common.exception.GlobalExceptionHandler;
import com.fruit.entity.MemberLevel;
import com.fruit.entity.User;
import com.fruit.entity.UserSignIn;
import com.fruit.service.MemberService;
import com.fruit.service.SignInService;
import com.fruit.utils.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SignInControllerTest {

    @InjectMocks
    private SignInController signInController;

    @Mock
    private SignInService signInService;

    @Mock
    private MemberService memberService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(signInController)
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
    void signIn() throws Exception {
        UserSignIn signIn = new UserSignIn();
        signIn.setId(1L);
        when(signInService.signIn(1L)).thenReturn(signIn);

        mockMvc.perform(post("/consumer/sign-in"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(signInService).signIn(1L);
    }

    @Test
    void getSignInStatus() throws Exception {
        when(signInService.hasSignedToday(1L)).thenReturn(true);
        when(signInService.getContinuousDays(1L)).thenReturn(5);

        mockMvc.perform(get("/consumer/sign-in/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.signedToday").value(true))
                .andExpect(jsonPath("$.data.continuousDays").value(5));

        verify(signInService).hasSignedToday(1L);
        verify(signInService).getContinuousDays(1L);
    }

    @Test
    void getMonthSignIns() throws Exception {
        when(signInService.getMonthSignIns(eq(1L), anyInt(), anyInt())).thenReturn(Arrays.asList(new UserSignIn()));

        mockMvc.perform(get("/consumer/sign-in/month")
                        .param("year", "2023")
                        .param("month", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());

        verify(signInService).getMonthSignIns(1L, 2023, 10);
    }

    @Test
    void getMemberLevels() throws Exception {
        when(memberService.getAllLevels()).thenReturn(Arrays.asList(new MemberLevel()));

        mockMvc.perform(get("/consumer/sign-in/levels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());

        verify(memberService).getAllLevels();
    }

    @Test
    void getCurrentLevel() throws Exception {
        MemberLevel level = new MemberLevel();
        level.setId(1L);
        when(memberService.getUserLevel(1L)).thenReturn(level);

        mockMvc.perform(get("/consumer/sign-in/level"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(memberService).getUserLevel(1L);
    }
}