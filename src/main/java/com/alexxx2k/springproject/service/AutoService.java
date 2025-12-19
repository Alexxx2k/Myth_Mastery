package com.alexxx2k.springproject.service;

import com.alexxx2k.springproject.domain.dto.Auto;
import com.alexxx2k.springproject.domain.entities.AutoEntity;
import com.alexxx2k.springproject.domain.entities.PersonalEntity;
import com.alexxx2k.springproject.repository.AutoRepository;
import com.alexxx2k.springproject.repository.PersonalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AutoService {

    private final AutoRepository autoRepository;
    private final PersonalRepository personalRepository;

    public AutoService(AutoRepository autoRepository, PersonalRepository personalRepository) {
        this.autoRepository = autoRepository;
        this.personalRepository = personalRepository;
    }

    public List<Auto> getAllAutos() {
        return autoRepository.findAll().stream()
                .map(this::toDomainAuto)
                .toList();
    }

    public Optional<Auto> getAutoById(Integer id) {
        return autoRepository.findById(id)
                .map(this::toDomainAuto);
    }

    public Auto createAuto(Auto auto) {
        PersonalEntity personal = personalRepository.findById(auto.personalId())
                .orElseThrow(() -> new IllegalArgumentException("Personal not found"));
        var entityToSave = new AutoEntity(null, auto.num(), auto.color(), auto.mark(), personal);
        var savedEntity = autoRepository.save(entityToSave);
        return toDomainAuto(savedEntity);
    }

    @Transactional
    public Auto updateAuto(Integer id, Auto auto) {
        AutoEntity existingEntity = autoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Auto not found with id: " + id));
        PersonalEntity personal = personalRepository.findById(auto.personalId())
                .orElseThrow(() -> new IllegalArgumentException("Personal not found"));
        existingEntity.setNum(auto.num());
        existingEntity.setColor(auto.color());
        existingEntity.setMark(auto.mark());
        existingEntity.setPersonal(personal);
        var savedEntity = autoRepository.save(existingEntity);
        return toDomainAuto(savedEntity);
    }

    @Transactional
    public void deleteAuto(Integer id) {
        autoRepository.deleteById(id);
    }

    private Auto toDomainAuto(AutoEntity entity) {
        return new Auto(entity.getId(), entity.getNum(), entity.getColor(), entity.getMark(),
                entity.getPersonal() != null ? entity.getPersonal().getId() : null);
    }
}