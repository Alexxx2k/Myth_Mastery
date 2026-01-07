package com.alexxx2k.springproject.controller;

import com.alexxx2k.springproject.domain.dto.Mythology;
import com.alexxx2k.springproject.service.MythologyService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/mythologies")
public class MythologyController {

    private final MythologyService mythologyService;

    public MythologyController(MythologyService mythologyService) {
        this.mythologyService = mythologyService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String getAllMythologies(Model model) {
        var mythologyList = mythologyService.getAllMythologies();
        model.addAttribute("mythologyList", mythologyList);
        return "mainMythology";
    }

    @GetMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreateMythologyForm(Model model) {
        model.addAttribute("mythology", new Mythology(null, ""));
        return "createMythology";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String createMythology(@RequestParam String name, Model model) {
        try {
            Mythology mythologyDto = new Mythology(null, name);
            mythologyService.createMythology(mythologyDto);
            model.addAttribute("message", "Мифология успешно добавлена!");
            model.addAttribute("messageType", "success");
        } catch (Exception e) {
            model.addAttribute("message", "Ошибка при добавлении мифологии: " + e.getMessage());
            model.addAttribute("messageType", "error");
        }
        return showCreateMythologyForm(model);
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditMythologyForm(@PathVariable Long id, Model model) {
        try {
            Mythology mythology = mythologyService.getMythologyById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Мифология с ID " + id + " не найдена"));
            model.addAttribute("mythology", mythology);
            return "editMythology";
        } catch (Exception e) {
            model.addAttribute("message", "Ошибка: " + e.getMessage());
            model.addAttribute("messageType", "error");
            return "redirect:/mythologies";
        }
    }

    @PostMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateMythology(@PathVariable Long id, @RequestParam String name, Model model) {
        try {
            Mythology mythology = new Mythology(id, name);
            mythologyService.updateMythology(id, mythology);
            model.addAttribute("message", "Мифология успешно обновлена!");
            model.addAttribute("messageType", "success");
        } catch (Exception e) {
            model.addAttribute("message", "Ошибка при обновлении мифологии: " + e.getMessage());
            model.addAttribute("messageType", "error");
        }
        return showEditMythologyForm(id, model);
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteMythology(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            mythologyService.deleteMythology(id);
            redirectAttributes.addFlashAttribute("message", "Мифология успешно удалена!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Ошибка при удалении мифологии: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        return "redirect:/mythologies";
    }
}
