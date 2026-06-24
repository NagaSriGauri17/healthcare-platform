package com.healthcare.healthcare_platform.controller;

import com.healthcare.healthcare_platform.entity.Hospital;
import com.healthcare.healthcare_platform.service.DoctorService;
import com.healthcare.healthcare_platform.service.HospitalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hospital")
@RequiredArgsConstructor
public class HospitalController {

    private final HospitalService hospitalService;
    private final DoctorService doctorService;

    @PostMapping("/register")
    public ResponseEntity<Hospital> register(@RequestBody Map<String, Object> request) {
        Double latitude = request.get("latitude") != null ?
                Double.valueOf(request.get("latitude").toString()) : null;
        Double longitude = request.get("longitude") != null ?
                Double.valueOf(request.get("longitude").toString()) : null;
        Hospital hospital = hospitalService.registerHospital(
                (String) request.get("name"),
                (String) request.get("address"),
                (String) request.get("city"),
                (String) request.get("phone"),
                (String) request.get("email"),
                latitude,
                longitude
        );
        return ResponseEntity.ok(hospital);
    }

    @PostMapping("/geocode/{id}")
    public ResponseEntity<Hospital> geocode(@PathVariable Long id,
                                            @RequestBody Map<String, String> request) {
        Hospital hospital = hospitalService.geocodeAndSave(id, request.get("address"));
        return ResponseEntity.ok(hospital);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Hospital>> getAllHospitals() {
        return ResponseEntity.ok(hospitalService.getAllHospitals());
    }

    @GetMapping("/{id}/doctors")
    public ResponseEntity<?> getDoctorsByHospital(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getDoctorsByHospital(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Hospital>> search(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String name) {
        if (city != null) {
            return ResponseEntity.ok(hospitalService.searchByCity(city));
        } else if (name != null) {
            return ResponseEntity.ok(hospitalService.searchByName(name));
        }
        return ResponseEntity.ok(hospitalService.getAllHospitals());
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<Hospital>> nearby(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam Double radius) {
        return ResponseEntity.ok(hospitalService.findNearbyHospitals(lat, lng, radius));
    }
}