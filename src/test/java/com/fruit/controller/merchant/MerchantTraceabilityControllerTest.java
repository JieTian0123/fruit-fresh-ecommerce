package com.fruit.controller.merchant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fruit.common.exception.GlobalExceptionHandler;
import com.fruit.dto.TraceabilityDTO;
import com.fruit.entity.ProductTraceability;
import com.fruit.entity.User;
import com.fruit.service.ProductTraceabilityService;
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

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MerchantTraceabilityControllerTest {

    @InjectMocks
    private MerchantTraceabilityController controller;

    @Mock
    private ProductTraceabilityService traceabilityService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        User user = new User();
        user.setId(1L);
        user.setUserType(1);
        UserContext.setUser(user);

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void list_Success() throws Exception {
        ProductTraceability pt = new ProductTraceability();
        pt.setId(10L);
        when(traceabilityService.listByProductId(100L)).thenReturn(Collections.singletonList(pt));

        mockMvc.perform(get("/merchant/traceability/list/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].id").value(10));
    }

    @Test
    void add_Success() throws Exception {
        TraceabilityDTO dto = new TraceabilityDTO();
        dto.setProductId(100L);
        dto.setNodeType(1);
        dto.setNodeName("Farm");
        dto.setDescription("Harvested");
        dto.setOccurredTime(java.time.LocalDateTime.now());

        mockMvc.perform(post("/merchant/traceability/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(traceabilityService).addTraceability(eq(1L), any(TraceabilityDTO.class));
    }

    @Test
    void delete_Success() throws Exception {
        mockMvc.perform(delete("/merchant/traceability/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(traceabilityService).deleteTraceability(1L, 10L);
    }
}