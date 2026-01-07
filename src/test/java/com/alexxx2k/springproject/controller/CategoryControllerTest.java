package com.alexxx2k.springproject.controller;

import com.alexxx2k.springproject.domain.dto.Category;
import com.alexxx2k.springproject.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new Category(1L, "Test Category", "Medium", "Rare");
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getAllCategories_ShouldReturnView() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(List.of(testCategory));

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(view().name("mainCategory"))
                .andExpect(model().attributeExists("categoryList"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void showCreateCategoryForm_ShouldReturnView() throws Exception {
        mockMvc.perform(get("/categories/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("createCategory"))
                .andExpect(model().attributeExists("category"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createCategory_ShouldCreateCategory() throws Exception {
        when(categoryService.createCategory(any(Category.class))).thenReturn(testCategory);

        mockMvc.perform(post("/categories")
                        .with(csrf())
                        .param("name", "Test Category")
                        .param("hazard", "Medium")
                        .param("rarity", "Rare"))
                .andExpect(status().isOk())
                .andExpect(view().name("createCategory"))
                .andExpect(model().attribute("messageType", "success"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createCategory_ShouldHandleException() throws Exception {
        when(categoryService.createCategory(any(Category.class)))
                .thenThrow(new IllegalArgumentException("Категория с названием 'Test Category' уже существует"));

        mockMvc.perform(post("/categories")
                        .with(csrf())
                        .param("name", "Test Category")
                        .param("hazard", "Medium")
                        .param("rarity", "Rare"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("messageType", "error"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void showEditCategoryForm_ShouldReturnView() throws Exception {
        when(categoryService.getCategoryById(1L)).thenReturn(Optional.of(testCategory));

        mockMvc.perform(get("/categories/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("editCategory"))
                .andExpect(model().attributeExists("category"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void showEditCategoryForm_ShouldRedirectWhenCategoryNotFound() throws Exception {
        when(categoryService.getCategoryById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/categories/edit/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/categories"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteCategory_ShouldDeleteCategory() throws Exception {
        doNothing().when(categoryService).deleteCategory(1L);

        mockMvc.perform(post("/categories/delete/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/categories"))
                .andExpect(flash().attribute("messageType", "success"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteCategory_ShouldHandleDataIntegrityViolation() throws Exception {
        doThrow(new DataIntegrityViolationException("Constraint violation"))
                .when(categoryService).deleteCategory(1L);

        mockMvc.perform(post("/categories/delete/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/categories"))
                .andExpect(flash().attribute("messageType", "error"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteCategory_ShouldHandleGeneralException() throws Exception {
        doThrow(new RuntimeException("Some error"))
                .when(categoryService).deleteCategory(1L);

        mockMvc.perform(post("/categories/delete/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/categories"))
                .andExpect(flash().attribute("messageType", "error"));
    }

    @Test
    void getAllCategories_ShouldBeUnauthorizedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/categories"))
                .andExpect(status().isUnauthorized());
    }
}
