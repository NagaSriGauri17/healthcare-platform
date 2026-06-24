package com.healthcare.healthcare_platform.controller;

import com.healthcare.healthcare_platform.entity.Specialty;
import com.healthcare.healthcare_platform.service.SpecialtyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/specialty")
@RequiredArgsConstructor
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    @PostMapping("/add")
    public ResponseEntity<Specialty> addSpecialty(@RequestBody Map<String, String> request) {
        Specialty specialty = specialtyService.addSpecialty(
                request.get("name"),
                request.get("description")
        );
        return ResponseEntity.ok(specialty);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Specialty>> getAllSpecialties() {
        return ResponseEntity.ok(specialtyService.getAllSpecialties());
    }
}