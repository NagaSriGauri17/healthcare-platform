package com.healthcare.healthcare_platform.controller;

import com.healthcare.healthcare_platform.entity.Hospital;
import com.healthcare.healthcare_platform.repository.HospitalRepository;
import com.healthcare.healthcare_platform.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ICU bed availability. Anyone can read it; only staff of a hospital can change that hospital's numbers.
 */
@RestController
@RequestMapping("/api/emergency")
@RequiredArgsConstructor
public class EmergencyController {

    private final HospitalRepository hospitalRepository;
    private final JwtUtil jwtUtil;

    /** All hospitals that have entered their ICU details. */
    @GetMapping("/beds")
    public ResponseEntity<List<Map<String, Object>>> getBeds() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Hospital h : hospitalRepository.findAll()) {
            if (h.getIcuBedsTotal() != null) {
                result.add(toMap(h));
            }
        }
        return ResponseEntity.ok(result);
    }

    /** One hospital's current numbers (used to fill the update form). Zeros if never set. */
    @GetMapping("/beds/{hospitalId}")
    public ResponseEntity<?> getHospitalBeds(@PathVariable Long hospitalId) {
        return hospitalRepository.findById(hospitalId)
                .<ResponseEntity<?>>map(h -> ResponseEntity.ok(toMap(h)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Hospital not found")));
    }

    /** Add / update this hospital's ICU numbers. The login token must belong to the same hospital. */
    @PutMapping("/beds/{hospitalId}")
    public ResponseEntity<?> updateBeds(
            @PathVariable Long hospitalId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> request) {

        // 1. Who is calling?
        if (authHeader == null || !authHeader.startsWith("Bearer ")
                || !jwtUtil.validateToken(authHeader.substring(7))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please log in again"));
        }
        Long tokenHospitalId = jwtUtil.extractHospitalId(authHeader.substring(7));
        if (tokenHospitalId == null || !tokenHospitalId.equals(hospitalId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You can only update your own hospital"));
        }

        Hospital hospital = hospitalRepository.findById(hospitalId).orElse(null);
        if (hospital == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Hospital not found"));
        }

        // 2. Validate the numbers
        int total;
        int available;
        try {
            total = Integer.parseInt(String.valueOf(request.get("icuBedsTotal")).trim());
            available = Integer.parseInt(String.valueOf(request.get("icuBedsAvailable")).trim());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Bed numbers must be whole numbers"));
        }
        if (total < 0 || available < 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Bed numbers cannot be negative"));
        }
        if (available > total) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Available beds cannot be more than total beds"));
        }

        // 3. Save
        hospital.setIcuBedsTotal(total);
        hospital.setIcuBedsAvailable(available);
        if (request.get("emergencyOpen") != null) {
            hospital.setEmergencyOpen(Boolean.parseBoolean(request.get("emergencyOpen").toString()));
        }
        if (request.containsKey("emergencyContact")) {
            Object contact = request.get("emergencyContact");
            String c = contact == null ? "" : contact.toString().trim();
            if (c.length() > 30) {
                return ResponseEntity.badRequest().body(Map.of("message", "Contact number is too long"));
            }
            hospital.setEmergencyContact(c.isEmpty() ? null : c);
        }
        hospital.setBedsUpdatedAt(LocalDateTime.now());
        return ResponseEntity.ok(toMap(hospitalRepository.save(hospital)));
    }

    private Map<String, Object> toMap(Hospital h) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("hospitalId", h.getId());
        m.put("hospitalName", h.getName());
        m.put("city", h.getCity());
        m.put("icuBedsAvailable", h.getIcuBedsAvailable() == null ? 0 : h.getIcuBedsAvailable());
        m.put("icuBedsTotal", h.getIcuBedsTotal() == null ? 0 : h.getIcuBedsTotal());
        m.put("emergencyOpen", Boolean.TRUE.equals(h.getEmergencyOpen()));
        m.put("emergencyContact", h.getEmergencyContact() == null ? "" : h.getEmergencyContact());
        m.put("lastUpdated", h.getBedsUpdatedAt() == null ? ""
                : h.getBedsUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        return m;
    }
}
