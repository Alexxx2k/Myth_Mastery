package com.alexxx2k.springproject.service;

import com.alexxx2k.springproject.domain.dto.Step;
import com.alexxx2k.springproject.domain.entities.StepEntity;
import com.alexxx2k.springproject.repository.StepRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StepServiceTest {

    @Mock
    private StepRepository stepRepository;

    @InjectMocks
    private StepService stepService;

    private StepEntity stepEntity1;
    private StepEntity stepEntity2;

    @BeforeEach
    void setUp() {
        stepEntity1 = new StepEntity(1L, "Подготовка", "Описание подготовки");
        stepEntity2 = new StepEntity(2L, "Основной этап", "Описание основного этапа");
    }

    @Test
    void getAllSteps_ShouldReturnAllSteps() {
        List<StepEntity> entities = Arrays.asList(stepEntity1, stepEntity2);
        when(stepRepository.findAll()).thenReturn(entities);

        List<Step> result = stepService.getAllSteps();

        assertEquals(2, result.size());
        assertEquals("Подготовка", result.get(0).name());
        assertEquals("Основной этап", result.get(1).name());
        verify(stepRepository, times(1)).findAll();
    }

    @Test
    void getStepById_WithExistingId_ShouldReturnStep() {
        when(stepRepository.findById(1L)).thenReturn(Optional.of(stepEntity1));

        Optional<Step> result = stepService.getStepById(1L);

        assertTrue(result.isPresent());
        assertEquals("Подготовка", result.get().name());
        verify(stepRepository, times(1)).findById(1L);
    }

    @Test
    void getStepById_WithNonExistingId_ShouldReturnEmpty() {
        when(stepRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Step> result = stepService.getStepById(99L);

        assertFalse(result.isPresent());
        verify(stepRepository, times(1)).findById(99L);
    }

    @Test
    void createStep_WithUniqueName_ShouldCreateSuccessfully() {
        Step newStep = new Step(null, "Новый этап", "Описание нового этапа");
        StepEntity savedEntity = new StepEntity(3L, "Новый этап", "Описание нового этапа");

        when(stepRepository.existsByName("Новый этап")).thenReturn(false);
        when(stepRepository.save(any(StepEntity.class))).thenReturn(savedEntity);

        Step result = stepService.createStep(newStep);

        assertNotNull(result);
        assertEquals(3L, result.id());
        assertEquals("Новый этап", result.name());
        verify(stepRepository, times(1)).existsByName("Новый этап");
        verify(stepRepository, times(1)).save(any(StepEntity.class));
    }

    @Test
    void createStep_WithDuplicateName_ShouldThrowException() {
        Step newStep = new Step(null, "Подготовка", "Описание");
        when(stepRepository.existsByName("Подготовка")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> stepService.createStep(newStep));

        assertEquals("Шаг с названием 'Подготовка' уже существует", exception.getMessage());
        verify(stepRepository, times(1)).existsByName("Подготовка");
        verify(stepRepository, never()).save(any(StepEntity.class));
    }

    @Test
    void updateStep_WithExistingIdAndUniqueName_ShouldUpdateSuccessfully() {
        Step updatedStep = new Step(1L, "Обновленное название", "Обновленное описание");
        StepEntity existingEntity = new StepEntity(1L, "Старое название", "Старое описание");
        StepEntity savedEntity = new StepEntity(1L, "Обновленное название", "Обновленное описание");

        when(stepRepository.findById(1L)).thenReturn(Optional.of(existingEntity));
        when(stepRepository.findByName("Обновленное название")).thenReturn(Optional.empty());
        when(stepRepository.save(any(StepEntity.class))).thenReturn(savedEntity);

        Step result = stepService.updateStep(1L, updatedStep);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Обновленное название", result.name());
        assertEquals("Обновленное описание", result.description());
        verify(stepRepository, times(1)).findById(1L);
        verify(stepRepository, times(1)).findByName("Обновленное название");
        verify(stepRepository, times(1)).save(existingEntity);
    }

    @Test
    void updateStep_WithNonExistingId_ShouldThrowException() {
        Step updatedStep = new Step(99L, "Обновленное название", "Описание");
        when(stepRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> stepService.updateStep(99L, updatedStep));

        assertEquals("Шаг с ID 99 не найден", exception.getMessage());
        verify(stepRepository, times(1)).findById(99L);
        verify(stepRepository, never()).save(any(StepEntity.class));
    }

    @Test
    void updateStep_WithDuplicateNameFromOtherStep_ShouldThrowException() {
        Step updatedStep = new Step(1L, "Основной этап", "Описание"); // Дублирует stepEntity2
        StepEntity existingEntity = stepEntity1; // ID = 1
        StepEntity duplicateEntity = stepEntity2; // ID = 2, имя "Основной этап"

        when(stepRepository.findById(1L)).thenReturn(Optional.of(existingEntity));
        when(stepRepository.findByName("Основной этап")).thenReturn(Optional.of(duplicateEntity));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> stepService.updateStep(1L, updatedStep));

        assertEquals("Шаг с названием 'Основной этап' уже существует", exception.getMessage());
        verify(stepRepository, times(1)).findById(1L);
        verify(stepRepository, times(1)).findByName("Основной этап");
        verify(stepRepository, never()).save(any(StepEntity.class));
    }

    @Test
    void updateStep_WithSameNameForSameStep_ShouldUpdateSuccessfully() {
        Step updatedStep = new Step(1L, "Подготовка", "Новое описание");
        StepEntity existingEntity = stepEntity1; // ID = 1, имя "Подготовка"

        when(stepRepository.findById(1L)).thenReturn(Optional.of(existingEntity));
        when(stepRepository.findByName("Подготовка")).thenReturn(Optional.of(existingEntity));
        when(stepRepository.save(any(StepEntity.class))).thenReturn(existingEntity);

        Step result = stepService.updateStep(1L, updatedStep);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Подготовка", result.name());
        verify(stepRepository, times(1)).findById(1L);
        verify(stepRepository, times(1)).findByName("Подготовка");
        verify(stepRepository, times(1)).save(existingEntity);
    }

    @Test
    void deleteStep_WithExistingId_ShouldDeleteSuccessfully() {
        when(stepRepository.existsById(1L)).thenReturn(true);
        doNothing().when(stepRepository).deleteById(1L);

        stepService.deleteStep(1L);

        verify(stepRepository, times(1)).existsById(1L);
        verify(stepRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteStep_WithNonExistingId_ShouldThrowException() {
        when(stepRepository.existsById(99L)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> stepService.deleteStep(99L));

        assertEquals("Шаг с ID 99 не найден", exception.getMessage());
        verify(stepRepository, times(1)).existsById(99L);
        verify(stepRepository, never()).deleteById(anyLong());
    }

    @Test
    void existsByName_ShouldReturnTrueWhenExists() {
        when(stepRepository.existsByName("Подготовка")).thenReturn(true);

        boolean result = stepService.existsByName("Подготовка");

        assertTrue(result);
        verify(stepRepository, times(1)).existsByName("Подготовка");
    }

    @Test
    void existsByName_ShouldReturnFalseWhenNotExists() {
        when(stepRepository.existsByName("Несуществующий")).thenReturn(false);

        boolean result = stepService.existsByName("Несуществующий");

        assertFalse(result);
        verify(stepRepository, times(1)).existsByName("Несуществующий");
    }

    @Test
    void existStep_WithExistingName_ShouldReturnId() {
        when(stepRepository.findByName("Подготовка")).thenReturn(Optional.of(stepEntity1));

        Optional<Long> result = stepService.existStep("Подготовка");

        assertTrue(result.isPresent());
        assertEquals(1L, result.get());
        verify(stepRepository, times(1)).findByName("Подготовка");
    }

    @Test
    void existStep_WithNonExistingName_ShouldReturnEmpty() {
        when(stepRepository.findByName("Несуществующий")).thenReturn(Optional.empty());

        Optional<Long> result = stepService.existStep("Несуществующий");

        assertFalse(result.isPresent());
        verify(stepRepository, times(1)).findByName("Несуществующий");
    }

    @Test
    void toDomainStep_ShouldConvertEntityToDto() {
        StepEntity entity = new StepEntity(1L, "Тест", "Описание теста");

        when(stepRepository.findById(1L)).thenReturn(Optional.of(entity));

        Optional<Step> result = stepService.getStepById(1L);

        assertTrue(result.isPresent());
        assertEquals(entity.getId(), result.get().id());
        assertEquals(entity.getName(), result.get().name());
        assertEquals(entity.getDescription(), result.get().description());
    }
}
