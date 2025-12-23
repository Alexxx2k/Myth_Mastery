package com.alexxx2k.springproject.controller;

import com.alexxx2k.springproject.domain.dto.Category;
import com.alexxx2k.springproject.service.CategoryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String getAllCategories(Model model) {
        var categoryList = categoryService.getAllCategories();
        model.addAttribute("categoryList", categoryList);
        return "mainCategory"; // или другое имя шаблона
    }

    @GetMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreateCategoryForm(Model model) {
        model.addAttribute("category", Category.forCreation("", "", ""));
        return "createCategory";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String createCategory(
            @RequestParam String name,
            @RequestParam String hazard,
            @RequestParam String rarity,
            Model model) {
        try {
            Category categoryDto = Category.forCreation(name, hazard, rarity);
            categoryService.createCategory(categoryDto);
            model.addAttribute("message", "Категория успешно добавлена!");
            model.addAttribute("messageType", "success");
        } catch (Exception e) {
            model.addAttribute("message", "Ошибка при добавлении категории: " + e.getMessage());
            model.addAttribute("messageType", "error");
        }
        return showCreateCategoryForm(model);
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditCategoryForm(@PathVariable Long id, Model model) {
        try {
            Category category = categoryService.getCategoryById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Категория с ID " + id + " не найдена"));
            model.addAttribute("category", category);
            return "editCategory";
        } catch (Exception e) {
            model.addAttribute("message", "Ошибка: " + e.getMessage());
            model.addAttribute("messageType", "error");
            return "redirect:/categories";
        }
    }

    @PostMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateCategory(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String hazard,
            @RequestParam String rarity,
            Model model) {
        try {
            Category category = new Category(id, name, hazard, rarity);
            categoryService.updateCategory(id, category);
            model.addAttribute("message", "Категория успешно обновлена!");
            model.addAttribute("messageType", "success");
        } catch (Exception e) {
            model.addAttribute("message", "Ошибка при обновлении категории: " + e.getMessage());
            model.addAttribute("messageType", "error");
        }
        return showEditCategoryForm(id, model);
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("message", "Категория успешно удалена!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Ошибка при удалении категории: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        return "redirect:/categories";
    }
}