package com.alexxx2k.springproject.controller;

import com.alexxx2k.springproject.domain.dto.Auto;
import com.alexxx2k.springproject.service.AutoService;
import com.alexxx2k.springproject.service.PersonalService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/autos")
public class AutoController {

    private final AutoService autoService;
    private final PersonalService personalService;

    public AutoController(AutoService autoService, PersonalService personalService) {
        this.autoService = autoService;
        this.personalService = personalService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String getAllAutos(Model model) {
        var autoList = autoService.getAllAutos();
        var personalList = personalService.getAllPersonal();
        model.addAttribute("autoList", autoList);
        model.addAttribute("personalList", personalList);
        return "mainAuto";
    }

    @GetMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreateAutoForm(Model model) {
        var personalList = personalService.getAllPersonal();
        model.addAttribute("personalList", personalList);
        model.addAttribute("auto", new Auto(null, "", "", "", null));
        return "createAuto";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String createAuto(@RequestParam String num, @RequestParam String color,
                             @RequestParam String mark, @RequestParam Integer personalId, Model model) {
        try {
            Auto auto = new Auto(null, num, color, mark, personalId);
            autoService.createAuto(auto);
            model.addAttribute("message", "Автомобиль успешно добавлен!");
            model.addAttribute("messageType", "success");
        } catch (Exception e) {
            model.addAttribute("message", "Ошибка при добавлении автомобиля: " + e.getMessage());
            model.addAttribute("messageType", "error");
        }
        return showCreateAutoForm(model);
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditAutoForm(@PathVariable Integer id, Model model) {
        try {
            Auto auto = autoService.getAutoById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid auto Id:" + id));
            var personalList = personalService.getAllPersonal();
            model.addAttribute("auto", auto);
            model.addAttribute("personalList", personalList);
            return "editAuto";
        } catch (Exception e) {
            return "redirect:/autos";
        }
    }

    @PostMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateAuto(@PathVariable Integer id, @RequestParam String num,
                             @RequestParam String color, @RequestParam String mark,
                             @RequestParam Integer personalId, Model model) {
        try {
            Auto auto = new Auto(id, num, color, mark, personalId);
            autoService.updateAuto(id, auto);
            model.addAttribute("message", "Автомобиль успешно обновлен!");
            model.addAttribute("messageType", "success");
        } catch (Exception e) {
            model.addAttribute("message", "Ошибка при обновлении автомобиля: " + e.getMessage());
            model.addAttribute("messageType", "error");
        }
        return showEditAutoForm(id, model);
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteAuto(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            autoService.deleteAuto(id);
            redirectAttributes.addFlashAttribute("message", "Автомобиль успешно удален!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Ошибка при удалении автомобиля: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        return "redirect:/autos";
    }
}