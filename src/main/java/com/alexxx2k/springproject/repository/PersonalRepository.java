package com.alexxx2k.springproject.repository;

import com.alexxx2k.springproject.domain.entities.PersonalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonalRepository extends JpaRepository<PersonalEntity, Integer> {

}
