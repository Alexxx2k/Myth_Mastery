package com.alexxx2k.springproject.controller;

import com.alexxx2k.springproject.domain.dto.BuyStep;
import com.alexxx2k.springproject.service.BuyStepService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/buy-steps")
public class BuyStepController {

    private final BuyStepService buyStepService;

    public BuyStepController(BuyStepService buyStepService) {
        this.buyStepService = buyStepService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String getAllBuySteps(Model model) {
        var buyStepList = buyStepService.getAllBuySteps();
        model.addAttribute("buyStepList", buyStepList);
        return "mainBuyStep";
    }

    @GetMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreateBuyStepForm(Model model) {
        model.addAttribute("buyStep", new BuyStep(null, null, null, null));
        return "createBuyStep";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String createBuyStep(@RequestParam Long stepId,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateStart,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateEnd,
                                Model model) {
        try {
            BuyStep buyStepDto = new BuyStep(null, stepId, dateStart, dateEnd);
            buyStepService.createBuyStep(buyStepDto);
            model.addAttribute("message", "Шаг покупки успешно добавлен!");
            model.addAttribute("messageType", "success");
        } catch (Exception e) {
            model.addAttribute("message", "Ошибка при добавлении шага покупки: " + e.getMessage());
            model.addAttribute("messageType", "error");
        }
        return showCreateBuyStepForm(model);
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditBuyStepForm(@PathVariable Long id, Model model) {
        try {
            BuyStep buyStep = buyStepService.getBuyStepById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Шаг покупки с ID " + id + " не найден"));
            model.addAttribute("buyStep", buyStep);
            return "editBuyStep";
        } catch (Exception e) {
            model.addAttribute("message", "Ошибка: " + e.getMessage());
            model.addAttribute("messageType", "error");
            return "redirect:/buy-steps";
        }
    }

    @PostMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateBuyStep(@PathVariable Long id,
                                @RequestParam Long stepId,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateStart,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateEnd,
                                Model model) {
        try {
            BuyStep buyStep = new BuyStep(id, stepId, dateStart, dateEnd);
            buyStepService.updateBuyStep(id, buyStep);
            model.addAttribute("message", "Шаг покупки успешно обновлен!");
            model.addAttribute("messageType", "success");
        } catch (Exception e) {
            model.addAttribute("message", "Ошибка при обновлении шага покупки: " + e.getMessage());
            model.addAttribute("messageType", "error");
        }
        return showEditBuyStepForm(id, model);
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteBuyStep(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            buyStepService.deleteBuyStep(id);
            redirectAttributes.addFlashAttribute("message", "Шаг покупки успешно удален!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute(
                    "message",
                    "Невозможно удалить шаг покупки: Сначала удалите все связанные объекты"
            );
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Ошибка при удалении шага покупки: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        return "redirect:/buy-steps";
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String getActiveBuySteps(Model model) {
        List<BuyStep> activeSteps = buyStepService.getActiveBuySteps();
        model.addAttribute("buyStepList", activeSteps);
        model.addAttribute("title", "Активные шаги покупки");
        return "mainBuyStep";
    }



    @GetMapping("/step/{stepId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String getBuyStepsByStepId(@PathVariable Long stepId, Model model) {
        List<BuyStep> steps = buyStepService.getBuyStepsByStepId(stepId);
        model.addAttribute("buyStepList", steps);
        model.addAttribute("title", "Шаги покупки для Step ID: " + stepId);
        return "mainBuyStep";
    }
}
