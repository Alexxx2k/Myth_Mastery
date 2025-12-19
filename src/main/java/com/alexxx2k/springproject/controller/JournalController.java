package com.alexxx2k.springproject.controller;

import com.alexxx2k.springproject.domain.dto.FastestTrip;
import com.alexxx2k.springproject.service.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Controller
@RequestMapping("/journal")
public class JournalController {

    private final JournalService journalService;
    private final AutoService autoService;
    private final RouteService routeService;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("[yyyy-MM-dd'T'HH:mm]");

    public JournalController(JournalService journalService, AutoService autoService, RouteService routeService) {
        this.journalService = journalService;
        this.autoService = autoService;
        this.routeService = routeService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String getJournal(Model model) {
        var journalList = journalService.getAllJournals();
        var autoList = autoService.getAllAutos();
        var routeList = routeService.getAllRoutes();
        model.addAttribute("journalList", journalList);
        model.addAttribute("autoList", autoList);
        model.addAttribute("routeList", routeList);
        return "mainJournal";
    }

    @PostMapping("/start")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String startTrip(@RequestParam Integer autoId, @RequestParam Integer routeId,
                            @RequestParam(required = false) String customTimeOut, Model model) {
        try {
            LocalDateTime timeOut = null;
            if (customTimeOut != null && !customTimeOut.isEmpty()) {
                timeOut = LocalDateTime.parse(customTimeOut, formatter);
            }
            journalService.startTrip(autoId, routeId, timeOut);
            model.addAttribute("message", "Рейс успешно начат!");
            model.addAttribute("messageType", "success");
        } catch (Exception e) {
            model.addAttribute("message", "Ошибка при начале рейса: " + e.getMessage());
            model.addAttribute("messageType", "error");
        }
        return getJournal(model);
    }

    @PostMapping("/end")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String endTrip(@RequestParam Integer journalId,
                          @RequestParam(required = false) String customTimeIn, Model model) {
        try {
            LocalDateTime timeIn = null;
            if (customTimeIn != null && !customTimeIn.isEmpty()) {
                timeIn = LocalDateTime.parse(customTimeIn, formatter);
            }
            journalService.endTrip(journalId, timeIn);
            model.addAttribute("message", "Рейс успешно завершен!");
            model.addAttribute("messageType", "success");
        } catch (Exception e) {
            model.addAttribute("message", "Ошибка при завершении рейса: " + e.getMessage());
            model.addAttribute("messageType", "error");
        }
        return getJournal(model);
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String showEditJournalForm(@PathVariable Integer id, Model model) {
        try {
            var journal = journalService.getJournalById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid journal Id:" + id));
            var autoList = autoService.getAllAutos();
            var routeList = routeService.getAllRoutes();
            model.addAttribute("journal", journal);
            model.addAttribute("autoList", autoList);
            model.addAttribute("routeList", routeList);
            return "editJournal";
        } catch (Exception e) {
            return "redirect:/journal";
        }
    }

    @PostMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String updateJournal(@PathVariable Integer id, @RequestParam(required = false) String timeOut,
                                @RequestParam(required = false) String timeIn, @RequestParam(required = false) Integer autoId,
                                @RequestParam(required = false) Integer routeId, Model model) {
        try {
            LocalDateTime parsedTimeOut = null;
            LocalDateTime parsedTimeIn = null;
            if (timeOut != null && !timeOut.isEmpty()) {
                parsedTimeOut = LocalDateTime.parse(timeOut, formatter);
            }
            if (timeIn != null && !timeIn.isEmpty()) {
                parsedTimeIn = LocalDateTime.parse(timeIn, formatter);
            }
            journalService.updateJournal(id, parsedTimeOut, parsedTimeIn, autoId, routeId);
            model.addAttribute("message", "Запись журнала успешно обновлена!");
            model.addAttribute("messageType", "success");
        } catch (Exception e) {
            model.addAttribute("message", "Ошибка при обновлении записи: " + e.getMessage());
            model.addAttribute("messageType", "error");
        }
        return showEditJournalForm(id, model);
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String deleteJournal(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            journalService.deleteJournal(id);
            redirectAttributes.addFlashAttribute("message", "Запись журнала успешно удалена!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Ошибка при удалении записи: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        return "redirect:/journal";
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String showStatistics(Model model) {
        var routeList = routeService.getAllRoutes();
        var activeTripsStats = journalService.getActiveTripsForAllRoutes();
        model.addAttribute("routeList", routeList);
        model.addAttribute("activeTripsStats", activeTripsStats);
        return "statistics";
    }

    @PostMapping("/statistics/fastest")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String getFastestTrip(@RequestParam Integer routeId, Model model) {
        try {
            Optional<FastestTrip> fastestTrip = journalService.getFastestTripByRoute(routeId);
            var routeList = routeService.getAllRoutes();
            var activeTripsStats = journalService.getActiveTripsForAllRoutes();

            model.addAttribute("routeList", routeList);
            model.addAttribute("activeTripsStats", activeTripsStats);
            model.addAttribute("selectedRouteId", routeId);
            model.addAttribute("fastestTrip", fastestTrip.orElse(null));

            if (fastestTrip.isPresent()) {
                String formattedDuration = journalService.formatDuration(fastestTrip.get().tripDuration());
                model.addAttribute("formattedDuration", formattedDuration);
            }

            if (fastestTrip.isEmpty()) {
                model.addAttribute("message", "Для выбранного маршрута нет завершенных рейсов");
                model.addAttribute("messageType", "info");
            }
        } catch (Exception e) {
            model.addAttribute("message", "Ошибка при получении статистики: " + e.getMessage());
            model.addAttribute("messageType", "error");
        }
        return "statistics";
    }
}