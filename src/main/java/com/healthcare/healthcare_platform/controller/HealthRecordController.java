package com.healthcare.healthcare_platform.controller;

import com.healthcare.healthcare_platform.entity.HealthRecord;
import com.healthcare.healthcare_platform.service.HealthRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class HealthRecordController {

    private final HealthRecordService healthRecordService;

    @PostMapping("/upload")
    public ResponseEntity<HealthRecord> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("patientId") Long patientId,
            @RequestParam("appointmentId") Long appointmentId,
            @RequestParam("recordType") String recordType,
            @RequestParam("uploadedBy") String uploadedBy) {
        return ResponseEntity.ok(
                healthRecordService.uploadRecord(file, patientId, appointmentId, recordType, uploadedBy)
        );
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<HealthRecord>> getPatientRecords(@PathVariable Long patientId) {
        return ResponseEntity.ok(healthRecordService.getPatientRecords(patientId));
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<List<HealthRecord>> getAppointmentRecords(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(healthRecordService.getAppointmentRecords(appointmentId));
    }

    @GetMapping("/download/{recordId}")
    public ResponseEntity<Map<String, String>> getDownloadUrl(@PathVariable Long recordId) {
        return ResponseEntity.ok(healthRecordService.getDownloadUrl(recordId));
    }

    @DeleteMapping("/{recordId}")
    public ResponseEntity<String> deleteRecord(@PathVariable Long recordId) {
        healthRecordService.deleteRecord(recordId);
        return ResponseEntity.ok("Record deleted");
    }
}