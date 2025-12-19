package com.alexxx2k.springproject.controller;

import com.alexxx2k.springproject.domain.dto.Personal;
import com.alexxx2k.springproject.service.PersonalService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/personal")
public class PersonalController {

    private final PersonalService personalService;

    public PersonalController(PersonalService personalService) {
        this.personalService = personalService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String getAllPersonal(Model model) {
        var personalList = personalService.getAllPersonal();
        model.addAttribute("personalList", personalList);
        return "mainPersonal";
    }

    @GetMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreatePersonalForm(Model model) {
        model.addAttribute("personal", new Personal(null, "", "", ""));
        return "createPersonal";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String createPersonal(@RequestParam String firstName, @RequestParam String lastName,
                                 @RequestParam(required = false) String fatherName, Model model) {
        try {
            Personal personal = new Personal(null, firstName, lastName, fatherName);
            personalService.createPersonal(personal);
            model.addAttribute("message", "Сотрудник успешно добавлен!");
            model.addAttribute("messageType", "success");
        } catch (Exception e) {
            model.addAttribute("message", "Ошибка при добавлении сотрудника: " + e.getMessage());
            model.addAttribute("messageType", "error");
        }
        return "createPersonal";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditPersonalForm(@PathVariable Integer id, Model model) {
        try {
            Personal personal = personalService.getPersonalById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid personal Id:" + id));
            model.addAttribute("personal", personal);
            return "editPersonal";
        } catch (Exception e) {
            return "redirect:/personal";
        }
    }

    @PostMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updatePersonal(@PathVariable Integer id, @RequestParam String firstName,
                                 @RequestParam String lastName, @RequestParam(required = false) String fatherName, Model model) {
        try {
            Personal personal = new Personal(id, firstName, lastName, fatherName);
            personalService.updatePersonal(id, personal);
            model.addAttribute("message", "Сотрудник успешно обновлен!");
            model.addAttribute("messageType", "success");
        } catch (Exception e) {
            model.addAttribute("message", "Ошибка при обновлении сотрудника: " + e.getMessage());
            model.addAttribute("messageType", "error");
        }
        return showEditPersonalForm(id, model);
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deletePersonal(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            personalService.deletePersonal(id);
            redirectAttributes.addFlashAttribute("message", "Сотрудник успешно удален!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Ошибка при удалении сотрудника: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        return "redirect:/personal";
    }
}