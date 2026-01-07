package com.alexxx2k.springproject.controller;

import com.alexxx2k.springproject.domain.dto.BuyStep;
import com.alexxx2k.springproject.service.BuyStepService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BuyStepController.class)
class BuyStepControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BuyStepService buyStepService;

    private BuyStep testBuyStep;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        testDate = LocalDate.of(2024, 1, 15);
        testBuyStep = new BuyStep(1L, 100L, testDate, testDate.plusDays(7));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getAllBuySteps_ShouldReturnView() throws Exception {
        when(buyStepService.getAllBuySteps()).thenReturn(List.of(testBuyStep));

        mockMvc.perform(get("/buy-steps"))
                .andExpect(status().isOk())
                .andExpect(view().name("mainBuyStep"))
                .andExpect(model().attributeExists("buyStepList"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void showCreateBuyStepForm_ShouldReturnView() throws Exception {
        mockMvc.perform(get("/buy-steps/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("createBuyStep"))
                .andExpect(model().attributeExists("buyStep"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createBuyStep_ShouldCreateSuccessfully() throws Exception {
        when(buyStepService.createBuyStep(any(BuyStep.class))).thenReturn(testBuyStep);

        mockMvc.perform(post("/buy-steps")
                        .with(csrf())
                        .param("stepId", "100")
                        .param("dateStart", "2024-01-15")
                        .param("dateEnd", "2024-01-22"))
                .andExpect(status().isOk())
                .andExpect(view().name("createBuyStep"))
                .andExpect(model().attribute("messageType", "success"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createBuyStep_ShouldHandleException() throws Exception {
        when(buyStepService.createBuyStep(any(BuyStep.class)))
                .thenThrow(new IllegalArgumentException("Дата окончания не может быть раньше даты начала"));

        mockMvc.perform(post("/buy-steps")
                        .with(csrf())
                        .param("stepId", "100")
                        .param("dateStart", "2024-01-22")
                        .param("dateEnd", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("messageType", "error"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void showEditBuyStepForm_ShouldReturnView() throws Exception {
        when(buyStepService.getBuyStepById(1L)).thenReturn(Optional.of(testBuyStep));

        mockMvc.perform(get("/buy-steps/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("editBuyStep"))
                .andExpect(model().attributeExists("buyStep"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void showEditBuyStepForm_ShouldRedirectWhenNotFound() throws Exception {
        when(buyStepService.getBuyStepById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/buy-steps/edit/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/buy-steps"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteBuyStep_ShouldDeleteSuccessfully() throws Exception {
        doNothing().when(buyStepService).deleteBuyStep(1L);

        mockMvc.perform(post("/buy-steps/delete/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/buy-steps"))
                .andExpect(flash().attribute("messageType", "success"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteBuyStep_ShouldHandleDataIntegrityViolation() throws Exception {
        doThrow(new DataIntegrityViolationException("Constraint violation"))
                .when(buyStepService).deleteBuyStep(1L);

        mockMvc.perform(post("/buy-steps/delete/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("messageType", "error"));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getActiveBuySteps_ShouldReturnActiveSteps() throws Exception {
        when(buyStepService.getActiveBuySteps()).thenReturn(List.of(testBuyStep));

        mockMvc.perform(get("/buy-steps/active"))
                .andExpect(status().isOk())
                .andExpect(view().name("mainBuyStep"))
                .andExpect(model().attributeExists("buyStepList"))
                .andExpect(model().attributeExists("title"));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getBuyStepsByStepId_ShouldReturnStepsForStepId() throws Exception {
        when(buyStepService.getBuyStepsByStepId(100L)).thenReturn(List.of(testBuyStep));

        mockMvc.perform(get("/buy-steps/step/100"))
                .andExpect(status().isOk())
                .andExpect(view().name("mainBuyStep"))
                .andExpect(model().attributeExists("buyStepList"))
                .andExpect(model().attribute("title", "Шаги покупки для Step ID: 100"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createBuyStep_WithoutOptionalDates_ShouldWork() throws Exception {
        when(buyStepService.createBuyStep(any(BuyStep.class))).thenReturn(
                new BuyStep(1L, 100L, null, null));

        mockMvc.perform(post("/buy-steps")
                        .with(csrf())
                        .param("stepId", "100"))
                .andExpect(status().isOk())
                .andExpect(view().name("createBuyStep"));
    }
}
