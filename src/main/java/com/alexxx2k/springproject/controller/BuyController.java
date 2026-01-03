package com.alexxx2k.springproject.controller;

import com.alexxx2k.springproject.domain.dto.Buy;
import com.alexxx2k.springproject.service.BuyService;
import com.alexxx2k.springproject.service.CustomerService;
import com.alexxx2k.springproject.service.ProductService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/buys")
public class BuyController {

    private final BuyService buyService;
    private final ProductService productService;
    private final CustomerService customerService;

    public BuyController(BuyService buyService,
                         ProductService productService,
                         CustomerService customerService) {
        this.buyService = buyService;
        this.productService = productService;
        this.customerService = customerService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
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

    // ========== НОВЫЕ МЕТОДЫ ДЛЯ ПОЛЬЗОВАТЕЛЕЙ ==========

    // 1. Форма создания заказа с товарами
    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String showNewBuyFormForUser(Model model) {
        try {
            // Получаем текущего пользователя
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String customerEmail = auth.getName();

            // Загружаем все товары для выбора
            var products = productService.getAllProducts();

            model.addAttribute("products", products);
            model.addAttribute("customerEmail", customerEmail);
            return "newBuyWithProducts";

        } catch (Exception e) {
            model.addAttribute("message", "Ошибка: " + e.getMessage());
            model.addAttribute("messageType", "error");
            return "redirect:/";
        }
    }

    // 2. Создание заказа с выбранными товарами
    @PostMapping("/create-user-order")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String createUserBuy(
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String productIds, // строка типа "1,3,5"
            RedirectAttributes redirectAttributes) {

        try {
            // Получаем текущего пользователя
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String customerEmail = auth.getName();
            Long customerId = customerService.getCustomerIdByEmail(customerEmail);

            // Формируем описание с товарами
            String finalDescription = buildDescriptionWithProducts(description, productIds);

            // Создаем заказ
            buyService.createBuy(customerId, finalDescription, null);

            // Считаем количество товаров
            int productCount = 0;
            if (productIds != null && !productIds.trim().isEmpty()) {
                productCount = productIds.split(",").length;
            }

            redirectAttributes.addFlashAttribute("message",
                    "✅ Заказ успешно создан! " +
                            (productCount > 0 ? "Выбрано товаров: " + productCount : ""));
            redirectAttributes.addFlashAttribute("messageType", "success");

            return "redirect:/buys/my-orders";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message",
                    "❌ Ошибка при создании заказа: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/buys/new";
        }
    }

    // 3. Показать заказы текущего пользователя
    @GetMapping("/my-orders")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String getMyOrders(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String customerEmail = auth.getName();
            Long customerId = customerService.getCustomerIdByEmail(customerEmail);

            // Фильтруем заказы по customerId
            List<Buy> allBuys = buyService.getAllBuys();
            List<Buy> myBuys = allBuys.stream()
                    .filter(buy -> buy.customerId().equals(customerId))
                    .collect(Collectors.toList());

            model.addAttribute("buyList", myBuys);
            model.addAttribute("customerEmail", customerEmail);
            return "myOrders";

        } catch (Exception e) {
            model.addAttribute("message", "Ошибка: " + e.getMessage());
            model.addAttribute("messageType", "error");
            return "redirect:/buys";
        }
    }

    // Вспомогательный метод: формируем описание с товарами
    private String buildDescriptionWithProducts(String originalDescription, String productIds) {
        StringBuilder description = new StringBuilder();

        if (originalDescription != null && !originalDescription.trim().isEmpty()) {
            description.append(originalDescription.trim());
        }

        if (productIds != null && !productIds.trim().isEmpty()) {
            if (description.length() > 0) {
                description.append("\n\n");
            }
            description.append("=== ВЫБРАННЫЕ ТОВАРЫ ===\n");
            description.append("ID товаров: ").append(productIds);

            // Можно добавить названия товаров, если нужно
            try {
                List<Long> ids = Arrays.stream(productIds.split(","))
                        .map(String::trim)
                        .map(Long::parseLong)
                        .collect(Collectors.toList());

                description.append("\n\nСписок товаров:");
                for (Long id : ids) {
                    var productOpt = productService.getProductById(id);
                    if (productOpt.isPresent()) {
                        var product = productOpt.get();
                        description.append("\n- ").append(product.name())
                                .append(" (ID: ").append(id).append(")");
                    }
                }
            } catch (Exception e) {
                // Если не получилось получить названия - просто IDs
            }
        }

        return description.toString();
    }
}
