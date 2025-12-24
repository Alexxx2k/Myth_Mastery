package com.alexxx2k.springproject.service;

import com.alexxx2k.springproject.domain.dto.Step;
import com.alexxx2k.springproject.domain.entities.StepEntity;
import com.alexxx2k.springproject.repository.StepRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class StepService {

    private final StepRepository stepRepository;

    public StepService(StepRepository stepRepository) {
        this.stepRepository = stepRepository;
    }

    public List<Step> getAllSteps() {
        return stepRepository.findAll().stream()
                .map(this::toDomainStep)
                .toList();
    }

    public Optional<Step> getStepById(Long id) {
        return stepRepository.findById(id)
                .map(this::toDomainStep);
    }

    @Transactional
    public Step createStep(Step step) {
        if (stepRepository.existsByName(step.name())) {
            throw new IllegalArgumentException("Шаг с названием '" + step.name() + "' уже существует");
        }

        StepEntity entity = new StepEntity(null, step.name(), step.description());
        StepEntity savedEntity = stepRepository.save(entity);
        return toDomainStep(savedEntity);
    }

    @Transactional
    public Step updateStep(Long id, Step step) {
        StepEntity existingEntity = stepRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Шаг с ID " + id + " не найден"));

        // Проверяем, не существует ли уже шаг с таким именем (кроме текущего)
        Optional<StepEntity> duplicate = stepRepository.findByName(step.name());
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
            throw new IllegalArgumentException("Шаг с названием '" + step.name() + "' уже существует");
        }

        existingEntity.setName(step.name());
        existingEntity.setDescription(step.description());
        StepEntity savedEntity = stepRepository.save(existingEntity);
        return toDomainStep(savedEntity);
    }

    @Transactional
    public void deleteStep(Long id) {
        if (!stepRepository.existsById(id)) {
            throw new IllegalArgumentException("Шаг с ID " + id + " не найден");
        }
        stepRepository.deleteById(id);
    }

    public boolean existsByName(String name) {
        return stepRepository.existsByName(name);
    }

    public Optional<Long> existStep(String name) {
        return stepRepository.findByName(name)
                .map(StepEntity::getId);
    }

    private Step toDomainStep(StepEntity entity) {
        return new Step(entity.getId(), entity.getName(), entity.getDescription());
    }
}