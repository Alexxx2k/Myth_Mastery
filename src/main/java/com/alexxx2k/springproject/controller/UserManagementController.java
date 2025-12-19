package com.alexxx2k.springproject.controller;

import com.alexxx2k.springproject.domain.dto.User;
import com.alexxx2k.springproject.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserManagementController {

    private final UserService userService;

    public UserManagementController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping
    public String usersManagement(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "mainUsers";
    }

    @GetMapping("/create")
    public String createUserForm(Model model) {
        model.addAttribute("user", new User(null, "", "", "USER", true, null));
        model.addAttribute("roles", List.of("ADMIN", "OPERATOR", "USER"));
        return "createUser";
    }

    @PostMapping("/create")
    public String createUser(@RequestParam String username, @RequestParam String password,
                             @RequestParam String role, @RequestParam(defaultValue = "true") Boolean enabled,
                             Model model, RedirectAttributes redirectAttributes) {
        try {
            User user = new User(null, username.trim(), null, role, enabled, null);
            userService.createUser(user, password);
            redirectAttributes.addFlashAttribute("message", "Пользователь успешно создан!");
            redirectAttributes.addFlashAttribute("messageType", "success");
            return "redirect:/users";
        } catch (Exception e) {
            model.addAttribute("message", "Ошибка при создании пользователя: " + e.getMessage());
            model.addAttribute("messageType", "error");
            model.addAttribute("user", new User(null, username, null, role, enabled, null));
            model.addAttribute("roles", List.of("ADMIN", "OPERATOR", "USER"));
            return "createUser";
        }
    }

    @GetMapping("/edit/{id}")
    public String editUserForm(@PathVariable Integer id, Model model) {
        try {
            User user = userService.getUserById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
            model.addAttribute("user", user);
            model.addAttribute("roles", List.of("ADMIN", "OPERATOR", "USER"));
            return "editUser";
        } catch (Exception e) {
            return "redirect:/users";
        }
    }

    @PostMapping("/update/{id}")
    public String updateUser(@PathVariable Integer id, @RequestParam String username,
                             @RequestParam String role, @RequestParam(defaultValue = "true") Boolean enabled,
                             @RequestParam(required = false) String password, Model model, RedirectAttributes redirectAttributes) {
        try {
            User user = new User(id, username.trim(), null, role, enabled, null);
            userService.updateUser(id, user, password);
            redirectAttributes.addFlashAttribute("message", "Пользователь успешно обновлен!");
            redirectAttributes.addFlashAttribute("messageType", "success");
            return "redirect:/users";
        } catch (Exception e) {
            model.addAttribute("message", "Ошибка при обновлении пользователя: " + e.getMessage());
            model.addAttribute("messageType", "error");
            model.addAttribute("user", new User(id, username, null, role, enabled, null));
            model.addAttribute("roles", List.of("ADMIN", "OPERATOR", "USER"));
            return "editUser";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("message", "Пользователь успешно удален!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Ошибка при удалении пользователя: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        return "redirect:/users";
    }
}