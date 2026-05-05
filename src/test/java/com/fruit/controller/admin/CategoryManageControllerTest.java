package com.fruit.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fruit.common.exception.GlobalExceptionHandler;
import com.fruit.common.result.PageResult;
import com.fruit.dto.CategoryDTO;
import com.fruit.entity.Category;
import com.fruit.service.CategoryService;
import com.fruit.utils.UserContext;
import com.fruit.vo.CategoryTreeVO;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CategoryManageControllerTest {

    @InjectMocks
    private CategoryManageController controller;

    @Mock
    private CategoryService categoryService;

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

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void list_Success() throws Exception {
        PageResult<Category> pageResult = new PageResult<>();
        Category category = new Category();
        category.setId(1L);
        category.setName("Test Category");
        pageResult.setList(Collections.singletonList(category));
        pageResult.setTotal(1L);

        when(categoryService.listPage(1, 10)).thenReturn(pageResult);

        mockMvc.perform(get("/admin/category/list")
                .param("pageNum", "1")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].id").value(1))
                .andExpect(jsonPath("$.data.list[0].name").value("Test Category"));

        verify(categoryService).listPage(1, 10);
    }

    @Test
    void tree_Success() throws Exception {
        CategoryTreeVO treeVO = new CategoryTreeVO();
        treeVO.setId(1L);
        treeVO.setName("Root Category");
        List<CategoryTreeVO> tree = Collections.singletonList(treeVO);

        when(categoryService.getCategoryTree()).thenReturn(tree);

        mockMvc.perform(get("/admin/category/tree")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Root Category"));

        verify(categoryService).getCategoryTree();
    }

    @Test
    void detail_Success() throws Exception {
        Category category = new Category();
        category.setId(1L);
        category.setName("Test Category");

        when(categoryService.getById(1L)).thenReturn(category);

        mockMvc.perform(get("/admin/category/detail/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Test Category"));

        verify(categoryService).getById(1L);
    }

    @Test
    void add_Success() throws Exception {
        CategoryDTO dto = new CategoryDTO();
        dto.setName("New Category");

        mockMvc.perform(post("/admin/category/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(categoryService).addCategory(any(CategoryDTO.class));
    }

    @Test
    void update_Success() throws Exception {
        CategoryDTO dto = new CategoryDTO();
        dto.setName("Updated Category");

        mockMvc.perform(put("/admin/category/update/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(categoryService).updateCategory(eq(1L), any(CategoryDTO.class));
    }

    @Test
    void delete_Success() throws Exception {
        mockMvc.perform(delete("/admin/category/delete/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(categoryService).deleteCategory(1L);
    }

    @Test
    void updateStatus_Success() throws Exception {
        mockMvc.perform(put("/admin/category/status/1")
                .param("status", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(categoryService).updateStatus(1L, 1);
    }
}
