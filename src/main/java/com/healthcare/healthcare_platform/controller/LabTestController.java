package com.healthcare.healthcare_platform.controller;

import com.healthcare.healthcare_platform.entity.LabTest;
import com.healthcare.healthcare_platform.service.LabTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lab")
@RequiredArgsConstructor
public class LabTestController {

    private final LabTestService labTestService;

    @GetMapping("/tests")
    public ResponseEntity<List<Map<String, Object>>> getAvailableTests() {
        return ResponseEntity.ok(labTestService.getAvailableTests());
    }

    @PostMapping("/book")
    public ResponseEntity<LabTest> bookTest(@RequestBody Map<String, Object> request) {
        LabTest test = labTestService.bookTest(
                Long.valueOf(request.get("patientId").toString()),
                (String) request.get("testCode"),
                (String) request.get("testName"),
                (String) request.get("centerName"),
                Double.valueOf(request.get("price").toString()),
                (String) request.get("paymentMethod")
        );
        return ResponseEntity.ok(test);
    }

    @GetMapping("/status/{testId}")
    public ResponseEntity<LabTest> getTestStatus(@PathVariable Long testId) {
        return ResponseEntity.ok(labTestService.getTestStatus(testId));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<LabTest>> getPatientTests(@PathVariable Long patientId) {
        return ResponseEntity.ok(labTestService.getPatientTests(patientId));
    }
}