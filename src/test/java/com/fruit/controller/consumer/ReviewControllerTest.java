package com.fruit.controller.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fruit.common.exception.GlobalExceptionHandler;
import com.fruit.common.result.PageResult;
import com.fruit.dto.ReviewDTO;
import com.fruit.entity.User;
import com.fruit.service.ProductReviewService;
import com.fruit.utils.UserContext;
import com.fruit.vo.ReviewVO;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    @InjectMocks
    private ReviewController reviewController;

    @Mock
    private ProductReviewService productReviewService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reviewController)
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
    void add() throws Exception {
        ReviewDTO dto = new ReviewDTO();
        dto.setOrderNo("ORDER123");
        dto.setProductId(1L);
        dto.setRating(5);
        dto.setContent("Great!");

        mockMvc.perform(post("/consumer/review/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(productReviewService).addReview(eq(1L), any(ReviewDTO.class));
    }

    @Test
    void productReviews() throws Exception {
        PageResult<ReviewVO> pageResult = new PageResult<>();
        pageResult.setList(Arrays.asList(new ReviewVO()));
        pageResult.setTotal(1L);

        when(productReviewService.getProductReviews(1L, 1, 10)).thenReturn(pageResult);

        mockMvc.perform(get("/consumer/review/product/1")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1));

        verify(productReviewService).getProductReviews(1L, 1, 10);
    }

    @Test
    void checkReviewed() throws Exception {
        when(productReviewService.hasReviewed(1L, "ORDER123", 1L)).thenReturn(true);

        mockMvc.perform(get("/consumer/review/check")
                        .param("orderNo", "ORDER123")
                        .param("productId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        verify(productReviewService).hasReviewed(1L, "ORDER123", 1L);
    }
}