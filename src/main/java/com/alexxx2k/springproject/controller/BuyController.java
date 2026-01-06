package com.alexxx2k.springproject.controller;

import com.alexxx2k.springproject.domain.dto.Buy;
import com.alexxx2k.springproject.service.BuyService;
import com.alexxx2k.springproject.service.CustomerService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/buys")
public class BuyController {

    private final BuyService buyService;
    private final CustomerService customerService;

    public BuyController(BuyService buyService,
                         CustomerService customerService) {
        this.buyService = buyService;
        this.customerService = customerService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String getAllBuys(Model model) {
        List<Buy> buys = buyService.getAllBuys();
        model.addAttribute("buyList", buys);
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

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditBuyForm(@PathVariable Long id, Model model) {
        try {
            Buy buy = buyService.getBuyById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Заказ с ID " + id + " не найден"));
            model.addAttribute("buy", buy);
            return "editBuy";
        } catch (Exception e) {
            model.addAttribute("message", "Ошибка: " + e.getMessage());
            model.addAttribute("messageType", "error");
            return "redirect:/buys";
        }
    }

    @PostMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateBuy(
            @PathVariable Long id,
            @RequestParam Long customerId,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Long buyStepId,
            Model model) {
        try {
            buyService.updateBuy(id, customerId, description, buyStepId);
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

    @GetMapping("/my-orders")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String getMyOrders(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String customerEmail = auth.getName();
            Long customerId = customerService.getCustomerIdByEmail(customerEmail);

            List<Buy> allBuys = buyService.getAllBuys();
            List<Buy> myBuys = new ArrayList<>();
            for (Buy buy : allBuys) {
                if (buy.customerId().equals(customerId)) {
                    myBuys.add(buy);
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
}
