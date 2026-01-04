package com.alexxx2k.springproject.controller;

import com.alexxx2k.springproject.domain.dto.Product;
import com.alexxx2k.springproject.service.CategoryService;
import com.alexxx2k.springproject.service.MythologyService;
import com.alexxx2k.springproject.service.ProductService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final MythologyService mythologyService;

    public ProductController(ProductService productService,
                             CategoryService categoryService,
                             MythologyService mythologyService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.mythologyService = mythologyService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String getAllProducts(Model model) {
        try {
            List<Product> productList = productService.getAllProducts();
            model.addAttribute("productList", productList);
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("mythologies", mythologyService.getAllMythologies());
            return "mainProduct";
        } catch (Exception e) {
            model.addAttribute("message", "Ошибка при получении списка продуктов: " + e.getMessage());
            model.addAttribute("messageType", "error");
            return "mainProduct";
        }
    }

    @GetMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreateProductForm(Model model) {
        model.addAttribute("product", new Product(
                null,
                null,
                "",
                null,
                "",
                "",
                BigDecimal.ZERO,
                "",
                ""
        ));

        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("mythologies", mythologyService.getAllMythologies());

        List<Map<String, String>> existingImages = productService.getExistingImages();
        model.addAttribute("existingImages", existingImages);
        model.addAttribute("hasExistingImages", !existingImages.isEmpty());

        return "createProduct";
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createProduct(
            @RequestParam Long categoryId,
            @RequestParam Long mythologyId,
            @RequestParam String name,
            @RequestParam BigDecimal price,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) MultipartFile imageFile,
            @RequestParam(required = false) String selectedImageUrl,
            RedirectAttributes redirectAttributes) {

        try {
            validateProductInput(name, price);

            System.out.println("DEBUG: Selected image URL from form: " + selectedImageUrl);

            Product productDto = new Product(
                    null,
                    categoryId,
                    null,
                    mythologyId,
                    null,
                    name.trim(),
                    price != null ? price : BigDecimal.ZERO,
                    description != null ? description : "",
                    selectedImageUrl != null ? selectedImageUrl : ""
            );

            productService.createProduct(productDto, imageFile, selectedImageUrl);

            redirectAttributes.addFlashAttribute("message", "Продукт успешно добавлен!");
            redirectAttributes.addFlashAttribute("messageType", "success");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Ошибка при добавлении продукта: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/products/create";
        }

        return "redirect:/products";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditProductForm(@PathVariable Long id, Model model) {
        try {
            Product product = productService.getProductById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Продукт с ID " + id + " не найден"));

            model.addAttribute("product", product);
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("mythologies", mythologyService.getAllMythologies());

            List<Map<String, String>> existingImages = productService.getExistingImages();
            model.addAttribute("existingImages", existingImages);
            model.addAttribute("hasExistingImages", !existingImages.isEmpty());

            if (product.pic() != null && !product.pic().isEmpty()) {
                Map<String, String> currentImageInfo = productService.getImageInfo(product.pic());
                model.addAttribute("currentImageInfo", currentImageInfo);
            }

            return "editProduct";

        } catch (Exception e) {
            model.addAttribute("message", "Ошибка: " + e.getMessage());
            model.addAttribute("messageType", "error");
            return "redirect:/products";
        }
    }

    @PostMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateProduct(
            @PathVariable Long id,
            @RequestParam Long categoryId,
            @RequestParam Long mythologyId,
            @RequestParam String name,
            @RequestParam BigDecimal price,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) MultipartFile imageFile,
            @RequestParam(required = false) String imageOption,
            @RequestParam(required = false) String selectedImageUrl,
            RedirectAttributes redirectAttributes) {

        try {
            validateProductInput(name, price);

            boolean keepCurrentImage = "keep".equals(imageOption);

            Product product = new Product(
                    id,
                    categoryId,
                    null,
                    mythologyId,
                    null,
                    name.trim(),
                    price != null ? price : BigDecimal.ZERO,
                    description != null ? description : "",
                    selectedImageUrl != null ? selectedImageUrl : ""
            );

            productService.updateProduct(id, product, imageFile, selectedImageUrl, keepCurrentImage);

            redirectAttributes.addFlashAttribute("message", "Продукт успешно обновлен!");
            redirectAttributes.addFlashAttribute("messageType", "success");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Ошибка при обновлении продукта: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }

        return "redirect:/products/edit/" + id;
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("message", "Продукт успешно удален!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Ошибка при удалении продукта: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        return "redirect:/products";
    }

    @GetMapping("/by-category/{categoryId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String getProductsByCategory(@PathVariable Long categoryId, Model model) {
        try {
            List<Product> productList = productService.getProductsByCategory(categoryId);
            model.addAttribute("productList", productList);
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("mythologies", mythologyService.getAllMythologies());
            return "mainProduct";
        } catch (Exception e) {
            model.addAttribute("message", "Ошибка при фильтрации продуктов: " + e.getMessage());
            model.addAttribute("messageType", "error");
            return "mainProduct";
        }
    }

    @GetMapping("/by-mythology/{mythologyId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String getProductsByMythology(@PathVariable Long mythologyId, Model model) {
        try {
            List<Product> productList = productService.getProductsByMythology(mythologyId);
            model.addAttribute("productList", productList);
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("mythologies", mythologyService.getAllMythologies());
            return "mainProduct";
        } catch (Exception e) {
            model.addAttribute("message", "Ошибка при фильтрации продуктов: " + e.getMessage());
            model.addAttribute("messageType", "error");
            return "mainProduct";
        }
    }

    @GetMapping("/search-images")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @ResponseBody
    public List<Map<String, String>> searchImages(@RequestParam(required = false) String search) {
        return productService.searchImages(search);
    }

    @GetMapping("/image-info")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @ResponseBody
    public Map<String, String> getImageInfo(@RequestParam String imageUrl) {
        return productService.getImageInfo(imageUrl);
    }

    private void validateProductInput(String name, BigDecimal price) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Название продукта не может быть пустым");
        }
        if (price != null && price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Цена не может быть отрицательной");
        }
    }
}