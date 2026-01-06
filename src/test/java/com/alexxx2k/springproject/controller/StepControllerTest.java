package com.alexxx2k.springproject.controller;

import com.alexxx2k.springproject.domain.dto.Step;
import com.alexxx2k.springproject.service.StepService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StepControllerTest {

    @Mock
    private StepService stepService;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private StepController stepController;

    private List<Step> stepList;

    @BeforeEach
    void setUp() {
        stepList = Arrays.asList(
                new Step(1L, "Подготовка", "Описание подготовки"),
                new Step(2L, "Основной этап", "Описание основного этапа")
        );
    }

    @Test
    void getAllSteps_ShouldReturnMainStepPage() {
        when(stepService.getAllSteps()).thenReturn(stepList);
        when(model.addAttribute("stepList", stepList)).thenReturn(model);
        
        String viewName = stepController.getAllSteps(model);
        
        assertEquals("mainStep", viewName);
        verify(stepService, times(1)).getAllSteps();
        verify(model, times(1)).addAttribute("stepList", stepList);
    }

    @Test
    void showCreateStepForm_ShouldReturnCreateStepPage() {
        when(model.addAttribute(eq("step"), any(Step.class))).thenReturn(model);


        String viewName = stepController.showCreateStepForm(model);


        assertEquals("createStep", viewName);
        verify(model, times(1)).addAttribute(eq("step"), any(Step.class));
    }

    @Test
    void createStep_WithValidData_ShouldCreateAndShowSuccessMessage() {
        Step createdStep = new Step(3L, "Новый этап", "Описание");
        when(stepService.createStep(any(Step.class))).thenReturn(createdStep);
        when(model.addAttribute(eq("step"), any(Step.class))).thenReturn(model);
        when(model.addAttribute("message", "Шаг успешно добавлен!")).thenReturn(model);
        when(model.addAttribute("messageType", "success")).thenReturn(model);

        String viewName = stepController.createStep("Новый этап", "Описание", model);

        assertEquals("createStep", viewName);
        verify(stepService, times(1)).createStep(any(Step.class));
        verify(model, times(1)).addAttribute("message", "Шаг успешно добавлен!");
        verify(model, times(1)).addAttribute("messageType", "success");
    }

    @Test
    void createStep_WithServiceException_ShouldShowErrorMessage() {
        when(stepService.createStep(any(Step.class)))
                .thenThrow(new IllegalArgumentException("Ошибка создания"));
        when(model.addAttribute(eq("step"), any(Step.class))).thenReturn(model);
        when(model.addAttribute(eq("message"), anyString())).thenReturn(model);
        when(model.addAttribute("messageType", "error")).thenReturn(model);

        String viewName = stepController.createStep("Название", "Описание", model);

        assertEquals("createStep", viewName);
        verify(stepService, times(1)).createStep(any(Step.class));
        verify(model, times(1)).addAttribute(eq("message"), contains("Ошибка при добавлении шага"));
        verify(model, times(1)).addAttribute("messageType", "error");
    }

    @Test
    void showEditStepForm_WithExistingId_ShouldReturnEditStepPage() {
        Step step = new Step(1L, "Подготовка", "Описание");
        when(stepService.getStepById(1L)).thenReturn(Optional.of(step));
        when(model.addAttribute("step", step)).thenReturn(model);

        String viewName = stepController.showEditStepForm(1L, model);

        assertEquals("editStep", viewName);
        verify(stepService, times(1)).getStepById(1L);
        verify(model, times(1)).addAttribute("step", step);
    }

    @Test
    void showEditStepForm_WithNonExistingId_ShouldRedirect() {
        when(stepService.getStepById(99L))
                .thenThrow(new IllegalArgumentException("Шаг с ID 99 не найден"));

        String viewName = stepController.showEditStepForm(99L, model);

        assertEquals("redirect:/steps", viewName);
        verify(model, times(1)).addAttribute("message", "Ошибка: Шаг с ID 99 не найден");
        verify(model, times(1)).addAttribute("messageType", "error");
    }


    @Test
    void deleteStep_Successfully_ShouldRedirectWithSuccessMessage() {
        doNothing().when(stepService).deleteStep(1L);
        when(redirectAttributes.addFlashAttribute("message", "Шаг успешно удален!")).thenReturn(redirectAttributes);
        when(redirectAttributes.addFlashAttribute("messageType", "success")).thenReturn(redirectAttributes);

        String viewName = stepController.deleteStep(1L, redirectAttributes);

        assertEquals("redirect:/steps", viewName);
        verify(stepService, times(1)).deleteStep(1L);
        verify(redirectAttributes, times(1)).addFlashAttribute("message", "Шаг успешно удален!");
        verify(redirectAttributes, times(1)).addFlashAttribute("messageType", "success");
    }

    @Test
    void deleteStep_WithDataIntegrityViolation_ShouldRedirectWithErrorMessage() {
        doThrow(new DataIntegrityViolationException("foreign key constraint"))
                .when(stepService).deleteStep(1L);
        when(redirectAttributes.addFlashAttribute(eq("message"), anyString())).thenReturn(redirectAttributes);
        when(redirectAttributes.addFlashAttribute("messageType", "error")).thenReturn(redirectAttributes);

        String viewName = stepController.deleteStep(1L, redirectAttributes);

        assertEquals("redirect:/steps", viewName);
        verify(stepService, times(1)).deleteStep(1L);
        verify(redirectAttributes, times(1)).addFlashAttribute("message",
                "Невозможно удалить шаг: сначала удалите все связанные объекты");
        verify(redirectAttributes, times(1)).addFlashAttribute("messageType", "error");
    }

    @Test
    void checkStepExists_WhenStepExists_ShouldReturnTrue() {
        when(stepService.existsByName("Подготовка")).thenReturn(true);

        boolean result = stepController.checkStepExists("Подготовка");

        assertTrue(result);
        verify(stepService, times(1)).existsByName("Подготовка");
    }

    @Test
    void checkStepExists_WhenStepNotExists_ShouldReturnFalse() {
        when(stepService.existsByName("Несуществующий")).thenReturn(false);

        boolean result = stepController.checkStepExists("Несуществующий");

        assertFalse(result);
        verify(stepService, times(1)).existsByName("Несуществующий");
    }
}
