package com.alexxx2k.springproject.controller;

import com.alexxx2k.springproject.domain.dto.Customer;
import com.alexxx2k.springproject.service.CustomerService;
import com.alexxx2k.springproject.service.CityService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customers")
@PreAuthorize("hasRole('ADMIN')")
public class CustomerController {

    private final CustomerService customerService;
    private final CityService cityService;

    public CustomerController(CustomerService customerService, CityService cityService) {
        this.customerService = customerService;
        this.cityService = cityService;
    }

    @GetMapping
    public String getAllCustomers(Model model) {
        var customerList = customerService.getAllCustomers();
        var cityList = cityService.getAllCities();
        model.addAttribute("customerList", customerList);
        model.addAttribute("cityList", cityList);
        return "mainCustomer";
    }

    // ДОБАВИЛ: Метод для создания пользователя (админ)
    @GetMapping("/create")
    public String showCreateCustomerForm(Model model) {
        var cityList = cityService.getAllCities();
        model.addAttribute("cityList", cityList);
        model.addAttribute("customer", new Customer(null, "", "", "", null, ""));
        return "createCustomer";
    }

    // ДОБАВИЛ: POST метод для создания
    @PostMapping("/create")
    public String createCustomer(@RequestParam String name,
                                 @RequestParam String email,
                                 @RequestParam String password,
                                 @RequestParam String cityName,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        try {
            // Создаем DTO для нового клиента
            Customer customerDto = new Customer(null, name, email, password, null, cityName);

            // Проверяем email
            if (customerService.existsByEmail(email)) {
                throw new IllegalArgumentException("Email '" + email + "' уже используется");
            }

            // Для создания используем Registration DTO
            var registrationDto = new com.alexxx2k.springproject.domain.dto.Registration(
                    name, email, password, password, cityName
            );

            customerService.registerCustomer(registrationDto);

            redirectAttributes.addFlashAttribute("message", "Клиент успешно создан!");
            redirectAttributes.addFlashAttribute("messageType", "success");
            return "redirect:/customers";
        } catch (Exception e) {
            var cityList = cityService.getAllCities();
            model.addAttribute("cityList", cityList);
            model.addAttribute("message", "Ошибка при создании клиента: " + e.getMessage());
            model.addAttribute("messageType", "error");
            return "createCustomer";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditCustomerForm(@PathVariable Long id, Model model) {
        try {
            Customer customer = customerService.getCustomerById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Клиент с ID " + id + " не найден"));
            var cityList = cityService.getAllCities();
            model.addAttribute("customer", customer);
            model.addAttribute("cityList", cityList);
            return "editCustomer";
        } catch (Exception e) {
            return "redirect:/customers";
        }
    }

    @PostMapping("/update/{id}")
    public String updateCustomer(@PathVariable Long id,
                                 @RequestParam String name,
                                 @RequestParam String email,
                                 @RequestParam(required = false) String password,
                                 @RequestParam String cityName,
                                 Model model) {
        try {
            Customer customer = new Customer(id, name, email, password, null, cityName);
            customerService.updateCustomer(id, customer);
            model.addAttribute("message", "Клиент успешно обновлен!");
            model.addAttribute("messageType", "success");
        } catch (Exception e) {
            model.addAttribute("message", "Ошибка при обновлении клиента: " + e.getMessage());
            model.addAttribute("messageType", "error");
        }
        return showEditCustomerForm(id, model);
    }

    @PostMapping("/delete/{id}")
    public String deleteCustomer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            customerService.deleteCustomer(id);
            redirectAttributes.addFlashAttribute("message", "Клиент успешно удален!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Ошибка при удалении клиента: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        return "redirect:/customers";
    }
}
