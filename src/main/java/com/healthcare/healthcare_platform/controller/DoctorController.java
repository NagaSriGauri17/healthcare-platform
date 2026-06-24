package com.healthcare.healthcare_platform.controller;

import com.healthcare.healthcare_platform.entity.Doctor;
import com.healthcare.healthcare_platform.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @PostMapping("/add")
    public ResponseEntity<Doctor> addDoctor(@RequestBody Map<String, Object> request) {
        Doctor doctor = doctorService.addDoctor(
                (String) request.get("name"),
                (String) request.get("phone"),
                (String) request.get("email"),
                Double.valueOf(request.get("fee").toString()),
                (String) request.get("experience"),
                Long.valueOf(request.get("hospitalId").toString()),
                Long.valueOf(request.get("specialtyId").toString())
        );
        return ResponseEntity.ok(doctor);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @GetMapping("/hospital/{hospitalId}")
    public ResponseEntity<List<Doctor>> getDoctorsByHospital(@PathVariable Long hospitalId) {
        return ResponseEntity.ok(doctorService.getDoctorsByHospital(hospitalId));
    }

    @GetMapping("/{id}/profile")
    public ResponseEntity<Doctor> getDoctorProfile(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Doctor>> searchDoctors(
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String city) {
        return ResponseEntity.ok(doctorService.searchDoctors(specialty, city));
    }
}