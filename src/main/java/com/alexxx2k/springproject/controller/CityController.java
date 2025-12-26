package com.alexxx2k.springproject.controller;

import com.alexxx2k.springproject.domain.dto.City;
import com.alexxx2k.springproject.service.CityService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cities")
public class CityController {

    private final CityService cityService;

    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String getAllCities(Model model) {
        var cityList = cityService.getAllCities();
        model.addAttribute("cityList", cityList);
        return "mainCity";
    }

    @GetMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreateCityForm(Model model) {
        model.addAttribute("city", new City(null, "", null));
        return "createCity";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String createCity(@RequestParam String name,
                             @RequestParam Long deliveryTime,
                             Model model) {
        try {
            City cityDto = new City(null, name, deliveryTime);
            cityService.createCity(cityDto);
            model.addAttribute("message", "Город успешно добавлен!");
            model.addAttribute("messageType", "success");
        } catch (Exception e) {
            model.addAttribute("message", "Ошибка при добавлении города: " + e.getMessage());
            model.addAttribute("messageType", "error");
        }
        return showCreateCityForm(model);
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditCityForm(@PathVariable Long id, Model model) {
        try {
            City city = cityService.getCityById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Город с ID " + id + " не найден"));
            model.addAttribute("city", city);
            return "editCity";
        } catch (Exception e) {
            model.addAttribute("message", "Ошибка: " + e.getMessage());
            model.addAttribute("messageType", "error");
            return "redirect:/cities";
        }
    }

    @PostMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateCity(@PathVariable Long id,
                             @RequestParam String name,
                             @RequestParam Long deliveryTime,
                             Model model) {
        try {
            City city = new City(id, name, deliveryTime);
            cityService.updateCity(id, city);
            model.addAttribute("message", "Город успешно обновлен!");
            model.addAttribute("messageType", "success");
        } catch (Exception e) {
            model.addAttribute("message", "Ошибка при обновлении города: " + e.getMessage());
            model.addAttribute("messageType", "error");
        }
        return showEditCityForm(id, model);
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteCity(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            cityService.deleteCity(id);
            redirectAttributes.addFlashAttribute("message", "Город успешно удален!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        }
        catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute(
                    "message",
                    "Невозможно удалить город: Сначала удалите все связанные объекты"
            );
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        return "redirect:/cities";
    }
}
