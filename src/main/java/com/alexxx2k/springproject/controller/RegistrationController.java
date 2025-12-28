package com.alexxx2k.springproject.controller;

import com.alexxx2k.springproject.domain.dto.Registration;
import com.alexxx2k.springproject.service.CustomerService;
import com.alexxx2k.springproject.service.CityService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegistrationController {

    private final CustomerService customerService;
    private final CityService cityService;

    public RegistrationController(CustomerService customerService, CityService cityService) {
        this.customerService = customerService;
        this.cityService = cityService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        var cityList = cityService.getAllCities();
        model.addAttribute("Registration", new Registration("", "", "", "", ""));
        model.addAttribute("cityList", cityList);
        return "register";
    }

    @PostMapping("/register")
    public String registerCustomer(@ModelAttribute Registration Registration,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        try {
            customerService.registerCustomer(Registration);
            redirectAttributes.addFlashAttribute("message",
                    "Регистрация успешна! Теперь вы можете войти в систему.");
            redirectAttributes.addFlashAttribute("messageType", "success");
            return "redirect:/login";
        } catch (Exception e) {
            var cityList = cityService.getAllCities();
            model.addAttribute("cityList", cityList);
            model.addAttribute("message", "Ошибка регистрации: " + e.getMessage());
            model.addAttribute("messageType", "error");
            return "register";
        }
    }
}
