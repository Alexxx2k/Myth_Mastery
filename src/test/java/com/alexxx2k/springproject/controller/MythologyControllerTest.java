package com.alexxx2k.springproject.controller;

import com.alexxx2k.springproject.domain.dto.Mythology;
import com.alexxx2k.springproject.service.MythologyService;
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

@WebMvcTest(MythologyController.class)
class MythologyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MythologyService mythologyService;

    private Mythology testMythology;

    @BeforeEach
    void setUp() {
        testMythology = new Mythology(1L, "Греческая");
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getAllMythologies_ShouldReturnView() throws Exception {
        when(mythologyService.getAllMythologies()).thenReturn(List.of(testMythology));

        mockMvc.perform(get("/mythologies"))
                .andExpect(status().isOk())
                .andExpect(view().name("mainMythology"))
                .andExpect(model().attributeExists("mythologyList"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void showCreateMythologyForm_ShouldReturnView() throws Exception {
        mockMvc.perform(get("/mythologies/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("createMythology"))
                .andExpect(model().attributeExists("mythology"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createMythology_ShouldCreateMythology() throws Exception {
        when(mythologyService.createMythology(any(Mythology.class))).thenReturn(testMythology);

        mockMvc.perform(post("/mythologies")
                        .with(csrf())
                        .param("name", "Греческая"))
                .andExpect(status().isOk())
                .andExpect(view().name("createMythology"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createMythology_ShouldHandleException() throws Exception {
        when(mythologyService.createMythology(any(Mythology.class)))
                .thenThrow(new IllegalArgumentException("Мифология с названием 'Греческая' уже существует"));

        mockMvc.perform(post("/mythologies")
                        .with(csrf())
                        .param("name", "Греческая"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("messageType", "error"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void showEditMythologyForm_ShouldReturnView() throws Exception {
        when(mythologyService.getMythologyById(1L)).thenReturn(Optional.of(testMythology));

        mockMvc.perform(get("/mythologies/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("editMythology"))
                .andExpect(model().attributeExists("mythology"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void showEditMythologyForm_ShouldRedirectWhenMythologyNotFound() throws Exception {
        when(mythologyService.getMythologyById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/mythologies/edit/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mythologies"));
    }

//    @Test
//    @WithMockUser(roles = {"ADMIN"})
//    void updateMythology_ShouldUpdateMythology() throws Exception {
//        when(mythologyService.updateMythology(eq(1L), any(Mythology.class))).thenReturn(testMythology);
//
//        mockMvc.perform(post("/mythologies/update/1")
//                        .with(csrf())
//                        .param("name", "Греческая"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("editMythology"));
//    }
//
//    @Test
//    @WithMockUser(roles = {"ADMIN"})
//    void updateMythology_ShouldHandleException() throws Exception {
//        when(mythologyService.updateMythology(eq(1L), any(Mythology.class)))
//                .thenThrow(new IllegalArgumentException("Мифология с названием 'Греческая' уже существует"));
//
//        mockMvc.perform(post("/mythologies/update/1")
//                        .with(csrf())
//                        .param("name", "Греческая"))
//                .andExpect(status().isOk())
//                .andExpect(model().attribute("messageType", "error"));
//    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteMythology_ShouldDeleteMythology() throws Exception {
        doNothing().when(mythologyService).deleteMythology(1L);

        mockMvc.perform(post("/mythologies/delete/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mythologies"));
    }

//    @Test
//    @WithMockUser(roles = {"USER"})
//    void createMythology_ShouldBeForbiddenForUserRole() throws Exception {
//        mockMvc.perform(get("/mythologies/create"))
//                .andExpect(status().isForbidden());
//    }

    @Test
    void getAllMythologies_ShouldBeUnauthorizedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/mythologies"))
                .andExpect(status().isUnauthorized());
    }
}
