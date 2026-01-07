package com.alexxx2k.springproject.controller;

import com.alexxx2k.springproject.domain.dto.Buy;
import com.alexxx2k.springproject.domain.dto.Step;
import com.alexxx2k.springproject.service.BuyService;
import com.alexxx2k.springproject.service.CustomerService;
import com.alexxx2k.springproject.service.StepService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/buys")
public class BuyController {

    private final BuyService buyService;
    private final CustomerService customerService;
    private final StepService stepService;

    public BuyController(BuyService buyService,
                         CustomerService customerService,
                         StepService stepService) {  // ← ДОБАВИТЬ параметр
        this.buyService = buyService;
        this.customerService = customerService;
        this.stepService = stepService;     // ← ДОБАВИТЬ
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String getAllBuys(Model model) {
        List<Buy> buys = buyService.getAllBuys();
        model.addAttribute("buyList", buys);

        // Для каждого заказа получить название статуса
        List<BuyStatusInfo> buyStatusList = new ArrayList<>();
        for (Buy buy : buys) {
            String statusName = buyService.getStepNameByBuyStepId(buy.buyStepId());
            buyStatusList.add(new BuyStatusInfo(buy, statusName));
        }

        model.addAttribute("buyStatusList", buyStatusList);
        return "mainBuy";
    }

    @GetMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreateBuyForm(Model model) {
        model.addAttribute("buy", Buy.forCreation(null, ""));
        return "createBuy";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String createBuy(
            @RequestParam Long customerId,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Long buyStepId,
            Model model) {
        try {
            buyService.createBuy(customerId, description, buyStepId);
            model.addAttribute("message", "Заказ успешно создан!");
            model.addAttribute("messageType", "success");
        } catch (Exception e) {
            model.addAttribute("message", "Ошибка при создании заказа: " + e.getMessage());
            model.addAttribute("messageType", "error");
        }
        return showCreateBuyForm(model);
    }

    // ========== ИЗМЕНИТЬ метод showEditBuyForm ==========
    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditBuyForm(@PathVariable Long id, Model model) {
        try {
            Buy buy = buyService.getBuyById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Заказ с ID " + id + " не найден"));
            model.addAttribute("buy", buy);

            // 1. Получить текущее название статуса
            String currentStatusName = buyService.getStepNameByBuyStepId(buy.buyStepId());
            model.addAttribute("currentStatusName", currentStatusName);

            // 2. Получить все шаги для выпадающего списка
            var allSteps = stepService.getAllSteps();
            model.addAttribute("allSteps", allSteps);

            return "editBuy";
        } catch (Exception e) {
            model.addAttribute("message", "Ошибка: " + e.getMessage());
            model.addAttribute("messageType", "error");
            return "redirect:/buys";
        }
    }

    // ========== ИЗМЕНИТЬ метод updateBuy ==========
    @PostMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateBuy(
            @PathVariable Long id,
            @RequestParam Long customerId,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Long selectedStepId,  // ← ИЗМЕНИТЬ тип и имя
            Model model) {
        try {
            Long finalBuyStepId = null;

            if (selectedStepId != null) {
                // Получить или создать buyStepId для выбранного stepId
                finalBuyStepId = buyService.getOrCreateBuyStepIdForStep(selectedStepId);
            }

            buyService.updateBuy(id, customerId, description, finalBuyStepId);
            model.addAttribute("message", "Заказ успешно обновлен!");
            model.addAttribute("messageType", "success");
        } catch (Exception e) {
            model.addAttribute("message", "Ошибка при обновлении заказа: " + e.getMessage());
            model.addAttribute("messageType", "error");
        }
        return showEditBuyForm(id, model);
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteBuy(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            buyService.deleteBuy(id);
            redirectAttributes.addFlashAttribute("message", "Заказ успешно удален!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message",
                    "Ошибка при удалении заказа: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        return "redirect:/buys";
    }

    // ========== ИЗМЕНИТЬ метод getMyOrders для пользователя ==========
    @GetMapping("/my-orders")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String getMyOrders(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String customerEmail = auth.getName();
            Long customerId = customerService.getCustomerIdByEmail(customerEmail);

            List<Buy> allBuys = buyService.getAllBuys();
            List<BuyStatusInfo> myBuys = new ArrayList<>();

            for (Buy buy : allBuys) {
                if (buy.customerId().equals(customerId)) {
                    String statusName = buyService.getStepNameByBuyStepId(buy.buyStepId());
                    myBuys.add(new BuyStatusInfo(buy, statusName));
                }
            }

            model.addAttribute("buyList", myBuys);
            model.addAttribute("customerEmail", customerEmail);
            return "myOrders";

        } catch (Exception e) {
            model.addAttribute("message", "Ошибка: " + e.getMessage());
            model.addAttribute("messageType", "error");
            return "redirect:/";
        }
    }

    @PostMapping("/pay/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String payForOrder(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            // Получаем текущего пользователя
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String customerEmail = auth.getName();
            Long customerId = customerService.getCustomerIdByEmail(customerEmail);

            // Получаем заказ
            Buy buy = buyService.getBuyById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));

            // Проверяем, что заказ принадлежит пользователю
            if (!buy.customerId().equals(customerId)) {
                throw new IllegalArgumentException("Это не ваш заказ");
            }

            // Проверяем, что заказ еще не оплачен
            String currentStatus = buyService.getStepNameByBuyStepId(buy.buyStepId());
            if (currentStatus.contains("Оплачен") || currentStatus.contains("Завершен")) {
                throw new IllegalArgumentException("Заказ уже оплачен или завершен");
            }

            // 1. Найти шаг "Оплачен" в таблице step
            Optional<Step> paidStepOpt = stepService.getAllSteps().stream()
                    .filter(step -> step.name().equals("Оплачен"))
                    .findFirst();

            if (paidStepOpt.isEmpty()) {
                throw new IllegalArgumentException("Статус 'Оплачен' не найден в системе. Обратитесь к администратору.");
            }

            Long stepId = paidStepOpt.get().id();

            // 2. Найти или создать buyStep с этим stepId
            Long paidBuyStepId = buyService.getOrCreateBuyStepIdForStep(stepId);

            // 3. Обновляем статус заказа
            buyService.updateBuy(id, buy.customerId(), buy.description(), paidBuyStepId);

            redirectAttributes.addFlashAttribute("message", "✅ Заказ успешно оплачен!");
            redirectAttributes.addFlashAttribute("messageType", "success");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "❌ Ошибка: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }

        return "redirect:/buys/my-orders";
    }

    // ========== ВСПОМОГАТЕЛЬНЫЙ КЛАСС (добавить в конце файла) ==========
    public record BuyStatusInfo(Buy buy, String statusName) {}
}
