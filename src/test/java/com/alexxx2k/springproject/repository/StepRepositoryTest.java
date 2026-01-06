package com.alexxx2k.springproject.repository;

import com.alexxx2k.springproject.domain.entities.StepEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class StepRepositoryTest {

    @Autowired
    private StepRepository stepRepository;

    @Test
    void findAll_ShouldReturnAllSteps() {
        // Arrange - создаем тестовые данные через репозиторий
        StepEntity step1 = new StepEntity(null, "Шаг 1", "Описание шага 1");
        StepEntity step2 = new StepEntity(null, "Шаг 2", "Описание шага 2");
        stepRepository.save(step1);
        stepRepository.save(step2);

        // Act
        List<StepEntity> steps = stepRepository.findAll();

        // Assert
        assertNotNull(steps);
        assertEquals(2, steps.size());
        assertTrue(steps.stream().anyMatch(s -> s.getName().equals("Шаг 1")));
        assertTrue(steps.stream().anyMatch(s -> s.getName().equals("Шаг 2")));
    }

    @Test
    void findById_WithExistingId_ShouldReturnStep() {
        // Arrange
        StepEntity savedStep = stepRepository.save(
                new StepEntity(null, "Тестовый шаг", "Описание тестового шага")
        );

        // Act
        Optional<StepEntity> step = stepRepository.findById(savedStep.getId());

        // Assert
        assertTrue(step.isPresent());
        assertEquals("Тестовый шаг", step.get().getName());
        assertEquals("Описание тестового шага", step.get().getDescription());
    }

    @Test
    void findById_WithNonExistingId_ShouldReturnEmpty() {
        // Act
        Optional<StepEntity> step = stepRepository.findById(999L);

        // Assert
        assertFalse(step.isPresent());
    }

    @Test
    void save_ShouldPersistStep() {
        // Arrange
        StepEntity newStep = new StepEntity(null, "Новый шаг", "Описание нового шага");

        // Act
        StepEntity savedStep = stepRepository.save(newStep);

        // Assert
        assertNotNull(savedStep.getId());
        assertEquals("Новый шаг", savedStep.getName());
        assertEquals("Описание нового шага", savedStep.getDescription());

        // Verify it can be retrieved
        Optional<StepEntity> retrieved = stepRepository.findById(savedStep.getId());
        assertTrue(retrieved.isPresent());
        assertEquals("Новый шаг", retrieved.get().getName());
    }

    @Test
    void update_ShouldUpdateStep() {
        // Arrange
        StepEntity savedStep = stepRepository.save(
                new StepEntity(null, "Исходное название", "Исходное описание")
        );

        // Получаем entity для обновления
        StepEntity stepToUpdate = stepRepository.findById(savedStep.getId()).orElseThrow();
        stepToUpdate.setName("Обновленное название");
        stepToUpdate.setDescription("Обновленное описание");

        // Act
        StepEntity updated = stepRepository.save(stepToUpdate);

        // Assert
        assertEquals(savedStep.getId(), updated.getId());
        assertEquals("Обновленное название", updated.getName());
        assertEquals("Обновленное описание", updated.getDescription());

        // Проверяем, что данные действительно обновились в БД
        Optional<StepEntity> retrieved = stepRepository.findById(savedStep.getId());
        assertTrue(retrieved.isPresent());
        assertEquals("Обновленное название", retrieved.get().getName());
        assertEquals("Обновленное описание", retrieved.get().getDescription());
    }

    @Test
    void delete_ShouldRemoveStep() {
        // Arrange
        StepEntity savedStep = stepRepository.save(
                new StepEntity(null, "Шаг для удаления", "Описание для удаления")
        );
        Long stepId = savedStep.getId();
        assertTrue(stepRepository.existsById(stepId));

        // Act
        stepRepository.deleteById(stepId);

        // Assert
        assertFalse(stepRepository.existsById(stepId));
    }

    @Test
    void existsByName_WhenNameExists_ShouldReturnTrue() {
        // Arrange
        stepRepository.save(new StepEntity(null, "Проверочный шаг", "Описание"));

        // Act
        boolean exists = stepRepository.existsByName("Проверочный шаг");

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByName_WhenNameNotExists_ShouldReturnFalse() {
        // Act
        boolean exists = stepRepository.existsByName("Несуществующий шаг");

        // Assert
        assertFalse(exists);
    }

    @Test
    void findByName_WithExistingName_ShouldReturnStep() {
        // Arrange
        StepEntity savedStep = stepRepository.save(
                new StepEntity(null, "Конкретный шаг", "Описание конкретного шага")
        );

        // Act
        Optional<StepEntity> step = stepRepository.findByName("Конкретный шаг");

        // Assert
        assertTrue(step.isPresent());
        assertEquals(savedStep.getId(), step.get().getId());
        assertEquals("Конкретный шаг", step.get().getName());
        assertEquals("Описание конкретного шага", step.get().getDescription());
    }

    @Test
    void findByName_WithNonExistingName_ShouldReturnEmpty() {
        // Act
        Optional<StepEntity> step = stepRepository.findByName("Несуществующий шаг");

        // Assert
        assertFalse(step.isPresent());
    }

    @Test
    void findByName_ShouldBeCaseSensitive() {
        // Arrange
        stepRepository.save(new StepEntity(null, "Шаг", "Описание"));

        // Act
        Optional<StepEntity> lowerCase = stepRepository.findByName("шаг");
        Optional<StepEntity> upperCase = stepRepository.findByName("ШАГ");

        // Assert
        // Зависит от настроек БД, но обычно поиск регистрозависимый
        // Можно протестировать оба варианта
        assertFalse(lowerCase.isPresent() || upperCase.isPresent());
    }

    @Test
    void count_ShouldReturnCorrectNumberOfSteps() {
        // Arrange
        stepRepository.save(new StepEntity(null, "Шаг 1", "Описание 1"));
        stepRepository.save(new StepEntity(null, "Шаг 2", "Описание 2"));

        // Act
        long count = stepRepository.count();

        // Assert
        assertEquals(2, count);
    }

    @Test
    void existsById_WithExistingId_ShouldReturnTrue() {
        // Arrange
        StepEntity savedStep = stepRepository.save(
                new StepEntity(null, "Шаг", "Описание")
        );

        // Act
        boolean exists = stepRepository.existsById(savedStep.getId());

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsById_WithNonExistingId_ShouldReturnFalse() {
        // Act
        boolean exists = stepRepository.existsById(999L);

        // Assert
        assertFalse(exists);
    }
}