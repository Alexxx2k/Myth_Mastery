package com.alexxx2k.springproject.controller;

import com.alexxx2k.springproject.domain.dto.Buy;
import com.alexxx2k.springproject.domain.dto.BuyProduct;
import com.alexxx2k.springproject.service.BuyProductService;
import com.alexxx2k.springproject.service.BuyService;
import com.alexxx2k.springproject.service.CustomerService;
import com.alexxx2k.springproject.service.ProductService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/buy-products")
public class BuyProductController {

    private final BuyProductService buyProductService;
    private final BuyService buyService;
    private final ProductService productService;
    private final CustomerService customerService;

    public BuyProductController(BuyProductService buyProductService,
                                BuyService buyService,
                                ProductService productService,
                                CustomerService customerService) {
        this.buyProductService = buyProductService;
        this.buyService = buyService;
        this.productService = productService;
        this.customerService = customerService;
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String showNewBuyForm(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String customerEmail = auth.getName();
            Long customerId = customerService.getCustomerIdByEmail(customerEmail);

            model.addAttribute("customerEmail", customerEmail);
            model.addAttribute("customerId", customerId);
            return "newBuyWithProducts"; // упрощенная форма
        } catch (Exception e) {
            model.addAttribute("message", "Ошибка: " + e.getMessage());
            model.addAttribute("messageType", "error");
            return "redirect:/";
        }
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String createBuyWithProducts(
            @RequestParam Long customerId,
            @RequestParam(required = false) String description,
            RedirectAttributes redirectAttributes) {

        try {
            var buy = buyService.createBuy(customerId, description, 1L);
            Long buyId = buy.id();

            redirectAttributes.addFlashAttribute("message",
                    "✅ Заказ #" + buyId + " успешно создан!");
            redirectAttributes.addFlashAttribute("messageType", "success");

            return "redirect:/buy-products/by-buy/" + buyId;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message",
                    "❌ Ошибка создания заказа: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/buy-products/new";
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String getAllBuyProducts(Model model) {
        try {
            model.addAttribute("buyProductList", buyProductService.getAllBuyProducts());
            return "mainBuyProduct";
        } catch (Exception e) {
            model.addAttribute("message", "Ошибка: " + e.getMessage());
            model.addAttribute("messageType", "error");
            return "mainBuyProduct";
        }
    }

    @GetMapping("/by-buy/{buyId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String getBuyProductsByBuy(@PathVariable Long buyId, Model model) {
        try {
            model.addAttribute("buyProductList", buyProductService.getBuyProductsByBuyId(buyId));
            model.addAttribute("buyId", buyId);
            model.addAttribute("totalPrice", buyProductService.getTotalPriceByBuyId(buyId));
            model.addAttribute("buy", buyService.getBuyById(buyId).orElse(null));
            return "buyProductByBuy";
        } catch (Exception e) {
            model.addAttribute("message", "Ошибка: " + e.getMessage());
            model.addAttribute("messageType", "error");
            return "buyProductByBuy";
        }
    }

    @GetMapping("/add/{buyId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String showAddProductForm(@PathVariable Long buyId, Model model) {
        try {
            // Проверяем статус заказа
            Buy buy = buyService.getBuyById(buyId)
                    .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));

            String status = buyService.getStepNameByBuyStepId(buy.buyStepId());

            // Если заказ оплачен или завершен - блокируем редактирование
            if (status.contains("Оплачен") || status.contains("Завершен")) {
                model.addAttribute("message", "❌ Этот заказ оплачен, редактирование невозможно");
                model.addAttribute("messageType", "error");
                return "redirect:/buy-products/by-buy/" + buyId;
            }

            model.addAttribute("buyId", buyId);
            model.addAttribute("buy", buy);
            model.addAttribute("products", productService.getAllProducts());
            return "addProductToBuy";

        } catch (Exception e) {
            model.addAttribute("message", "❌ Ошибка: " + e.getMessage());
            model.addAttribute("messageType", "error");
            return "redirect:/buy-products/by-buy/" + buyId;
        }
    }

    @PostMapping("/add/{buyId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String addProductToBuy(
            @PathVariable Long buyId,
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") Integer amount,
            RedirectAttributes redirectAttributes) {
        try {
            buyProductService.addProductToBuy(buyId, productId, amount);
            redirectAttributes.addFlashAttribute("message", "✅ Товар добавлен в заказ!");
            redirectAttributes.addFlashAttribute("messageType", "success");
            return "redirect:/buy-products/by-buy/" + buyId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "❌ Ошибка: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/buy-products/add/" + buyId;
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            BuyProduct buyProduct = buyProductService.getBuyProductById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Позиция не найдена"));
            model.addAttribute("buyProduct", buyProduct);
            return "editBuyProduct";
        } catch (Exception e) {
            model.addAttribute("message", "❌ Ошибка: " + e.getMessage());
            model.addAttribute("messageType", "error");
            return "redirect:/buy-products";
        }
    }

    @PostMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String updateProductAmount(
            @PathVariable Long id,
            @RequestParam Integer amount,
            RedirectAttributes redirectAttributes) {
        try {
            BuyProduct updated = buyProductService.updateProductAmount(id, amount);
            redirectAttributes.addFlashAttribute("message", "✅ Количество обновлено!");
            redirectAttributes.addFlashAttribute("messageType", "success");
            return "redirect:/buy-products/by-buy/" + updated.buyId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "❌ Ошибка: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/buy-products/edit/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String deleteBuyProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            BuyProduct buyProduct = buyProductService.getBuyProductById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Позиция не найдена"));

            buyProductService.removeProductFromBuy(id);
            redirectAttributes.addFlashAttribute("message", "✅ Товар удален из заказа!");
            redirectAttributes.addFlashAttribute("messageType", "success");
            return "redirect:/buy-products/by-buy/" + buyProduct.buyId();

        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("message",
                    "❌ Нельзя удалить: существуют связанные объекты");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/buy-products";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "❌ Ошибка: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/buy-products";
        }
    }

    List<BuyProductService.CartItem> parseProductIds(String productIds) {
        List<BuyProductService.CartItem> items = new ArrayList<>();

        if (productIds == null || productIds.trim().isEmpty()) {
            return items;
        }

        String[] parts = productIds.split(",");
        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) continue;

            if (part.contains(":")) {
                String[] subParts = part.split(":");
                if (subParts.length >= 2) {
                    try {
                        Long productId = Long.parseLong(subParts[0].trim());
                        Integer amount = Integer.parseInt(subParts[1].trim());
                        items.add(new BuyProductService.CartItem(productId, amount));
                    } catch (NumberFormatException e) {
                    }
                }
            } else {
                try {
                    Long productId = Long.parseLong(part.trim());
                    items.add(new BuyProductService.CartItem(productId, 1));
                } catch (NumberFormatException e) {
                }
            }
        }

        return items;
    }
}
