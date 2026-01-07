package com.alexxx2k.springproject.controller;

import com.alexxx2k.springproject.domain.dto.Customer;
import com.alexxx2k.springproject.domain.dto.City;
import com.alexxx2k.springproject.service.CustomerService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    @MockitoBean
    private CityService cityService;

    private Customer testCustomer;
    private City testCity;

    @BeforeEach
    void setUp() {
        testCity = new City(1L, "Москва", 5L);
        testCustomer = new Customer(1L, "Иван Иванов", "ivan@test.com", null, 1L, "Москва");
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getAllCustomers_ShouldReturnView() throws Exception {
        when(customerService.getAllCustomers()).thenReturn(List.of(testCustomer));
        when(cityService.getAllCities()).thenReturn(List.of(testCity));

        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(view().name("mainCustomer"))
                .andExpect(model().attributeExists("customerList"))
                .andExpect(model().attributeExists("cityList"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void showCreateCustomerForm_ShouldReturnView() throws Exception {
        when(cityService.getAllCities()).thenReturn(List.of(testCity));

        mockMvc.perform(get("/customers/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("createCustomer"))
                .andExpect(model().attributeExists("customer"))
                .andExpect(model().attributeExists("cityList"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createCustomer_ShouldCreateCustomer() throws Exception {
        when(cityService.getAllCities()).thenReturn(List.of(testCity));
        when(customerService.existsByEmail("ivan@test.com")).thenReturn(false);

        mockMvc.perform(post("/customers/create")
                        .with(csrf())
                        .param("name", "Иван Иванов")
                        .param("email", "ivan@test.com")
                        .param("password", "password123")
                        .param("cityName", "Москва"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers"))
                .andExpect(flash().attribute("messageType", "success"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void showEditCustomerForm_ShouldReturnView() throws Exception {
        when(customerService.getCustomerById(1L)).thenReturn(Optional.of(testCustomer));
        when(cityService.getAllCities()).thenReturn(List.of(testCity));

        mockMvc.perform(get("/customers/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("editCustomer"))
                .andExpect(model().attributeExists("customer"))
                .andExpect(model().attributeExists("cityList"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void showEditCustomerForm_ShouldRedirectWhenCustomerNotFound() throws Exception {
        when(customerService.getCustomerById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/customers/edit/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteCustomer_ShouldDeleteCustomer() throws Exception {
        doNothing().when(customerService).deleteCustomer(1L);

        mockMvc.perform(post("/customers/delete/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers"))
                .andExpect(flash().attribute("messageType", "success"));
    }

    @Test
    void getAllCustomers_ShouldBeUnauthorizedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/customers"))
                .andExpect(status().isUnauthorized());
    }
}
