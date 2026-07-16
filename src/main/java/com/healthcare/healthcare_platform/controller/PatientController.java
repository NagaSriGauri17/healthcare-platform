package com.healthcare.healthcare_platform.controller;

import com.healthcare.healthcare_platform.entity.FamilyMember;
import com.healthcare.healthcare_platform.entity.User;
import com.healthcare.healthcare_platform.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.healthcare.healthcare_platform.service.PatientCodeService;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final PatientCodeService patientCodeService;

    @GetMapping("/profile/{userId}")
    public ResponseEntity<User> getProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(patientService.getProfile(userId));
    }

    @PutMapping("/profile/{userId}")
    public ResponseEntity<User> updateProfile(@PathVariable Long userId,
                                              @RequestBody Map<String, String> request) {
        User user = patientService.updateProfile(
                userId,
                request.get("name"),
                request.get("email"),
                request.get("phone")
        );
        return ResponseEntity.ok(user);
    }

    @PostMapping("/family/add")
    public ResponseEntity<FamilyMember> addFamilyMember(@RequestBody Map<String, Object> request) {
        FamilyMember member = patientService.addFamilyMember(
                Long.valueOf(request.get("userId").toString()),
                (String) request.get("name"),
                (String) request.get("relationship"),
                (String) request.get("phone"),
                Integer.valueOf(request.get("age").toString())
        );
        return ResponseEntity.ok(member);
    }

    @GetMapping("/search")
    public ResponseEntity<List<User>> searchPatients(@RequestParam String query) {
        return ResponseEntity.ok(patientService.searchPatients(query));
    }

    @GetMapping("/family/list/{userId}")
    public ResponseEntity<List<FamilyMember>> getFamilyMembers(@PathVariable Long userId) {
        return ResponseEntity.ok(patientService.getFamilyMembers(userId));
    }

    @PutMapping("/family/{memberId}")
    public ResponseEntity<FamilyMember> updateFamilyMember(@PathVariable Long memberId,
                                                           @RequestBody Map<String, Object> request) {
        FamilyMember member = patientService.updateFamilyMember(
                memberId,
                (String) request.get("name"),
                (String) request.get("relationship"),
                (String) request.get("phone"),
                request.get("age") != null ? Integer.valueOf(request.get("age").toString()) : null
        );
        return ResponseEntity.ok(member);
    }
    @GetMapping("/code")
    public ResponseEntity<String> getPatientCode(@RequestParam Long hospitalId, @RequestParam Long userId) {
        return ResponseEntity.ok(patientCodeService.getPatientCode(hospitalId, userId));
    }

    @DeleteMapping("/family/{memberId}")
    public ResponseEntity<String> deleteFamilyMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(patientService.deleteFamilyMember(memberId));
    }
}