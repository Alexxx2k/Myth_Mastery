package com.alexxx2k.springproject.service;

import com.alexxx2k.springproject.domain.dto.Personal;
import com.alexxx2k.springproject.domain.entities.PersonalEntity;
import com.alexxx2k.springproject.repository.PersonalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PersonalService {

    private final PersonalRepository personalRepository;

    public PersonalService(PersonalRepository personalRepository) {
        this.personalRepository = personalRepository;
    }

    public List<Personal> getAllPersonal() {
        return personalRepository.findAll().stream()
                .map(this::toDomainPerson)
                .toList();
    }

    public Optional<Personal> getPersonalById(Integer id) {
        return personalRepository.findById(id)
                .map(this::toDomainPerson);
    }

    public Personal createPersonal(Personal personal) {
        var entityToSave = new PersonalEntity(null, personal.firstName(), personal.lastName(), personal.fatherName());
        var savedEntity = personalRepository.save(entityToSave);
        return toDomainPerson(savedEntity);
    }

    @Transactional
    public Personal updatePersonal(Integer id, Personal personal) {
        PersonalEntity existingEntity = personalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Personal not found with id: " + id));
        existingEntity.setFirstName(personal.firstName());
        existingEntity.setLastName(personal.lastName());
        existingEntity.setFatherName(personal.fatherName());
        var savedEntity = personalRepository.save(existingEntity);
        return toDomainPerson(savedEntity);
    }

    @Transactional
    public void deletePersonal(Integer id) {
        personalRepository.deleteById(id);
    }

    private Personal toDomainPerson(PersonalEntity entity) {
        return new Personal(entity.getId(), entity.getFirstName(), entity.getLastName(), entity.getFatherName());
    }
}