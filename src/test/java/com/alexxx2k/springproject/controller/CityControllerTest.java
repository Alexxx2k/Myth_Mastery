package com.alexxx2k.springproject.controller;

import com.alexxx2k.springproject.domain.dto.City;
import com.alexxx2k.springproject.service.CityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CityController.class)
class CityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CityService cityService;

    private City testCity;

    @BeforeEach
    void setUp() {
        testCity = new City(1L, "Москва", 5L);
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getAllCities_ShouldReturnView() throws Exception {
        when(cityService.getAllCities()).thenReturn(List.of(testCity));

        mockMvc.perform(get("/cities"))
                .andExpect(status().isOk())
                .andExpect(view().name("mainCity"))
                .andExpect(model().attributeExists("cityList"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void showCreateCityForm_ShouldReturnView() throws Exception {
        mockMvc.perform(get("/cities/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("createCity"))
                .andExpect(model().attributeExists("city"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createCity_ShouldCreateCity() throws Exception {
        when(cityService.createCity(any(City.class))).thenReturn(testCity);

        mockMvc.perform(post("/cities")
                        .with(csrf())
                        .param("name", "Москва"))
                .andExpect(status().isOk())
                .andExpect(view().name("createCity"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createCity_ShouldHandleException() throws Exception {
        when(cityService.createCity(any(City.class)))
                .thenThrow(new IllegalArgumentException("Город уже существует"));

        mockMvc.perform(post("/cities")
                        .with(csrf())
                        .param("name", "Москва"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("messageType", "error"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void showEditCityForm_ShouldReturnView() throws Exception {
        when(cityService.getCityById(1L)).thenReturn(Optional.of(testCity));

        mockMvc.perform(get("/cities/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("editCity"))
                .andExpect(model().attributeExists("city"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void showEditCityForm_ShouldRedirectWhenCityNotFound() throws Exception {
        when(cityService.getCityById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/cities/edit/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cities"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteCity_ShouldDeleteCity() throws Exception {
        doNothing().when(cityService).deleteCity(1L);

        mockMvc.perform(post("/cities/delete/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cities"));
    }
}
