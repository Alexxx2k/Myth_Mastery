package com.alexxx2k.springproject.service;

import com.alexxx2k.springproject.domain.dto.Mythology;
import com.alexxx2k.springproject.domain.entities.MythologyEntity;
import com.alexxx2k.springproject.repository.MythologyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MythologyServiceTest {

    @Mock
    private MythologyRepository mythologyRepository;

    @InjectMocks
    private MythologyService mythologyService;

    private MythologyEntity testEntity;
    private Mythology testMythology;

    @BeforeEach
    void setUp() {
        testEntity = new MythologyEntity(1L, "Греческая");
        testMythology = new Mythology(1L, "Греческая");
    }

    @Test
    void getAllMythologies_ShouldReturnMythologies() {
        when(mythologyRepository.findAll()).thenReturn(List.of(testEntity));

        List<Mythology> mythologies = mythologyService.getAllMythologies();

        assertEquals(1, mythologies.size());
        assertEquals("Греческая", mythologies.get(0).name());
        verify(mythologyRepository, times(1)).findAll();
    }

    @Test
    void getMythologyById_ShouldReturnMythology() {
        when(mythologyRepository.findById(1L)).thenReturn(Optional.of(testEntity));

        Optional<Mythology> result = mythologyService.getMythologyById(1L);

        assertTrue(result.isPresent());
        assertEquals("Греческая", result.get().name());
    }

    @Test
    void getMythologyById_ShouldReturnEmptyWhenNotFound() {
        when(mythologyRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Mythology> result = mythologyService.getMythologyById(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void createMythology_ShouldCreateMythology() {
        when(mythologyRepository.existsByName("Греческая")).thenReturn(false);
        when(mythologyRepository.save(any(MythologyEntity.class))).thenReturn(testEntity);

        Mythology result = mythologyService.createMythology(new Mythology(null, "Греческая"));

        assertNotNull(result);
        assertEquals("Греческая", result.name());
        assertEquals(1L, result.id());
        verify(mythologyRepository, times(1)).save(any(MythologyEntity.class));
    }

    @Test
    void createMythology_ShouldThrowWhenMythologyExists() {
        when(mythologyRepository.existsByName("Греческая")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            mythologyService.createMythology(testMythology);
        });

        assertEquals("Мифология с названием 'Греческая' уже существует", exception.getMessage());
    }

    @Test
    void updateMythology_ShouldUpdateMythology() {
        MythologyEntity updatedEntity = new MythologyEntity(1L, "Обновленная греческая");

        when(mythologyRepository.findById(1L)).thenReturn(Optional.of(testEntity));
        when(mythologyRepository.findByName("Обновленная греческая")).thenReturn(Optional.empty());
        when(mythologyRepository.save(any(MythologyEntity.class))).thenReturn(updatedEntity);

        Mythology updatedMythology = new Mythology(1L, "Обновленная греческая");
        Mythology result = mythologyService.updateMythology(1L, updatedMythology);

        assertNotNull(result);
        assertEquals("Обновленная греческая", result.name());
        verify(mythologyRepository, times(1)).save(any(MythologyEntity.class));
    }

    @Test
    void updateMythology_ShouldThrowWhenMythologyWithSameNameExists() {
        MythologyEntity anotherEntity = new MythologyEntity(2L, "Греческая");

        when(mythologyRepository.findById(1L)).thenReturn(Optional.of(testEntity));
        when(mythologyRepository.findByName("Греческая")).thenReturn(Optional.of(anotherEntity));

        Mythology updatedMythology = new Mythology(1L, "Греческая");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            mythologyService.updateMythology(1L, updatedMythology);
        });

        assertEquals("Мифология с названием 'Греческая' уже существует", exception.getMessage());
    }

    @Test
    void updateMythology_ShouldUpdateWhenSameEntity() {
        when(mythologyRepository.findById(1L)).thenReturn(Optional.of(testEntity));
        when(mythologyRepository.findByName("Греческая")).thenReturn(Optional.of(testEntity));
        when(mythologyRepository.save(any(MythologyEntity.class))).thenReturn(testEntity);

        Mythology result = mythologyService.updateMythology(1L, testMythology);

        assertNotNull(result);
        assertEquals("Греческая", result.name());
        verify(mythologyRepository, times(1)).save(any(MythologyEntity.class));
    }

    @Test
    void updateMythology_ShouldThrowWhenNotFound() {
        when(mythologyRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            mythologyService.updateMythology(1L, testMythology);
        });

        assertEquals("Мифология с ID 1 не найдена", exception.getMessage());
    }

    @Test
    void deleteMythology_ShouldDeleteMythology() {
        when(mythologyRepository.existsById(1L)).thenReturn(true);
        doNothing().when(mythologyRepository).deleteById(1L);

        mythologyService.deleteMythology(1L);

        verify(mythologyRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteMythology_ShouldThrowWhenNotFound() {
        when(mythologyRepository.existsById(1L)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            mythologyService.deleteMythology(1L);
        });

        assertEquals("Мифология с ID 1 не найдена", exception.getMessage());
    }

    @Test
    void existsByName_ShouldReturnTrue() {
        when(mythologyRepository.existsByName("Греческая")).thenReturn(true);

        boolean exists = mythologyService.existsByName("Греческая");

        assertTrue(exists);
    }

    @Test
    void existsByName_ShouldReturnFalse() {
        when(mythologyRepository.existsByName("Греческая")).thenReturn(false);

        boolean exists = mythologyService.existsByName("Греческая");

        assertFalse(exists);
    }
}
