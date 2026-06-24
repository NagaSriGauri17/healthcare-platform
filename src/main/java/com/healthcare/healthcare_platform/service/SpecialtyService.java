package com.healthcare.healthcare_platform.service;

import com.healthcare.healthcare_platform.entity.Specialty;
import com.healthcare.healthcare_platform.repository.SpecialtyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpecialtyService {

    private final SpecialtyRepository specialtyRepository;

    public Specialty addSpecialty(String name, String description) {
        Specialty specialty = new Specialty();
        specialty.setName(name);
        specialty.setDescription(description);
        return specialtyRepository.save(specialty);
    }

    public List<Specialty> getAllSpecialties() {
        return specialtyRepository.findAll();
    }
}