package com.alexxx2k.springproject.service;

import com.alexxx2k.springproject.domain.dto.Mythology;
import com.alexxx2k.springproject.domain.entities.MythologyEntity;
import com.alexxx2k.springproject.repository.MythologyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MythologyService {

    private final MythologyRepository mythologyRepository;

    public MythologyService(MythologyRepository mythologyRepository) {
        this.mythologyRepository = mythologyRepository;
    }

    public List<Mythology> getAllMythologies() {
        return mythologyRepository.findAll().stream()
                .map(this::toDomainMythology)
                .toList();
    }

    public Optional<Mythology> getMythologyById(Long id) {
        return mythologyRepository.findById(id)
                .map(this::toDomainMythology);
    }

    @Transactional
    public Mythology createMythology(Mythology Mythology) {
        if (mythologyRepository.existsByName(Mythology.name())) {
            throw new IllegalArgumentException("Мифология с названием '" + Mythology.name() + "' уже существует");
        }

        MythologyEntity entity = new MythologyEntity(null, Mythology.name());
        MythologyEntity savedEntity = mythologyRepository.save(entity);
        return toDomainMythology(savedEntity);
    }

    @Transactional
    public Mythology updateMythology(Long id, Mythology Mythology) {
        MythologyEntity existingEntity = mythologyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Мифология с ID " + id + " не найдена"));

        // Проверяем, не существует ли уже мифология с таким именем (кроме текущей)
        Optional<MythologyEntity> duplicate = mythologyRepository.findByName(Mythology.name());
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
            throw new IllegalArgumentException("Мифология с названием '" + Mythology.name() + "' уже существует");
        }

        existingEntity.setName(Mythology.name());
        MythologyEntity savedEntity = mythologyRepository.save(existingEntity);
        return toDomainMythology(savedEntity);
    }

    @Transactional
    public void deleteMythology(Long id) {
        if (!mythologyRepository.existsById(id)) {
            throw new IllegalArgumentException("Мифология с ID " + id + " не найдена");
        }
        mythologyRepository.deleteById(id);
    }

    public boolean existsByName(String name) {
        return mythologyRepository.existsByName(name);
    }

    private Mythology toDomainMythology(MythologyEntity entity) {
        return new Mythology(entity.getId(), entity.getName());
    }
}
