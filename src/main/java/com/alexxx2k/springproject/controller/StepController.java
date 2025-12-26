package com.alexxx2k.springproject.controller;

import com.alexxx2k.springproject.domain.dto.Step;
import com.alexxx2k.springproject.service.StepService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/steps")
public class StepController {

    private final StepService stepService;

    public StepController(StepService stepService) {
        this.stepService = stepService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String getAllSteps(Model model) {
        var stepList = stepService.getAllSteps();
        model.addAttribute("stepList", stepList);
        return "mainStep";
    }

    @GetMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreateStepForm(Model model) {
        model.addAttribute("step", new Step(null, "", ""));
        return "createStep";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String createStep(@RequestParam String name,
                             @RequestParam String description,
                             Model model) {
        try {
            Step stepDto = new Step(null, name, description);
            stepService.createStep(stepDto);
            model.addAttribute("message", "Шаг успешно добавлен!");
            model.addAttribute("messageType", "success");
        } catch (Exception e) {
            model.addAttribute("message", "Ошибка при добавлении шага: " + e.getMessage());
            model.addAttribute("messageType", "error");
        }
        return showCreateStepForm(model);
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditStepForm(@PathVariable Long id, Model model) {
        try {
            Step step = stepService.getStepById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Шаг с ID " + id + " не найден"));
            model.addAttribute("step", step);
            return "editStep";
        } catch (Exception e) {
            model.addAttribute("message", "Ошибка: " + e.getMessage());
            model.addAttribute("messageType", "error");
            return "redirect:/steps";
        }
    }

    @PostMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateStep(@PathVariable Long id,
                             @RequestParam String name,
                             @RequestParam String description,
                             Model model) {
        try {
            Step step = new Step(id, name, description);
            stepService.updateStep(id, step);
            model.addAttribute("message", "Шаг успешно обновлен!");
            model.addAttribute("messageType", "success");
        } catch (Exception e) {
            model.addAttribute("message", "Ошибка при обновлении шага: " + e.getMessage());
            model.addAttribute("messageType", "error");
        }
        return showEditStepForm(id, model);
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteStep(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            stepService.deleteStep(id);
            redirectAttributes.addFlashAttribute("message", "Шаг успешно удален!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute(
                    "message",
                    "Невозможно удалить шаг: сначала удалите все связанные объекты"
            );
            redirectAttributes.addFlashAttribute("messageType", "error");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("message", "Ошибка: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Ошибка при удалении шага: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        return "redirect:/steps";
    }

    @GetMapping("/exists")
    @ResponseBody
    public boolean checkStepExists(@RequestParam String name) {
        return stepService.existsByName(name);
    }
}
