package com.alexxx2k.springproject.controller;

import com.alexxx2k.springproject.domain.dto.Route;
import com.alexxx2k.springproject.service.RouteService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String getAllRoutes(Model model) {
        var routeList = routeService.getAllRoutes();
        model.addAttribute("routeList", routeList);
        return "mainRoute";
    }

    @GetMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreateRouteForm(Model model) {
        model.addAttribute("route", new Route(null, ""));
        return "createRoute";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String createRoute(@RequestParam String name, Model model) {
        try {
            Route route = new Route(null, name);
            routeService.createRoute(route);
            model.addAttribute("message", "Маршрут успешно добавлен!");
            model.addAttribute("messageType", "success");
        } catch (Exception e) {
            model.addAttribute("message", "Ошибка при добавлении маршрута: " + e.getMessage());
            model.addAttribute("messageType", "error");
        }
        return "createRoute";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditRouteForm(@PathVariable Integer id, Model model) {
        try {
            Route route = routeService.getRouteById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid route Id:" + id));
            model.addAttribute("route", route);
            return "editRoute";
        } catch (Exception e) {
            return "redirect:/routes";
        }
    }

    @PostMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateRoute(@PathVariable Integer id, @RequestParam String name, Model model) {
        try {
            Route route = new Route(id, name);
            routeService.updateRoute(id, route);
            model.addAttribute("message", "Маршрут успешно обновлен!");
            model.addAttribute("messageType", "success");
        } catch (Exception e) {
            model.addAttribute("message", "Ошибка при обновлении маршрута: " + e.getMessage());
            model.addAttribute("messageType", "error");
        }
        return showEditRouteForm(id, model);
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteRoute(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            routeService.deleteRoute(id);
            redirectAttributes.addFlashAttribute("message", "Маршрут успешно удален!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Ошибка при удалении маршрута: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        return "redirect:/routes";
    }
}