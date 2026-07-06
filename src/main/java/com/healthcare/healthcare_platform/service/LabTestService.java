package com.healthcare.healthcare_platform.service;

import com.healthcare.healthcare_platform.entity.LabTest;
import com.healthcare.healthcare_platform.repository.LabTestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class LabTestService {

    private final LabTestRepository labTestRepository;

    public List<Map<String, Object>> getAvailableTests() {
        List<Map<String, Object>> tests = new ArrayList<>();

        tests.add(createTest("CBC", "Complete Blood Count", 299.0, "Apollo Diagnostics"));
        tests.add(createTest("LFT", "Liver Function Test", 499.0, "Apollo Diagnostics"));
        tests.add(createTest("RFT", "Renal Function Test", 449.0, "Apollo Diagnostics"));
        tests.add(createTest("HBA1C", "HbA1c - Diabetes Test", 349.0, "Thyrocare"));
        tests.add(createTest("LIPID", "Lipid Profile", 399.0, "Thyrocare"));
        tests.add(createTest("TSH", "Thyroid Stimulating Hormone", 299.0, "Thyrocare"));
        tests.add(createTest("VITAMIN_D", "Vitamin D Test", 799.0, "SRL Diagnostics"));
        tests.add(createTest("VITAMIN_B12", "Vitamin B12 Test", 699.0, "SRL Diagnostics"));
        tests.add(createTest("URINE_R", "Urine Routine", 149.0, "Apollo Diagnostics"));
        tests.add(createTest("ECG", "Electrocardiogram", 199.0, "Apollo Diagnostics"));

        return tests;
    }

    private Map<String, Object> createTest(String code, String name,
                                           Double price, String center) {
        Map<String, Object> test = new HashMap<>();
        test.put("testCode", code);
        test.put("testName", name);
        test.put("price", price);
        test.put("centerName", center);
        test.put("homeCollectionAvailable", true);
        test.put("reportTat", "24 hours");
        return test;
    }

    public LabTest bookTest(Long patientId, String testCode,
                            String testName, String centerName, Double price,
                            String paymentMethod) {
        LabTest labTest = new LabTest();
        labTest.setPatientId(patientId);
        labTest.setTestCode(testCode);
        labTest.setTestName(testName);
        labTest.setCenterName(centerName);
        labTest.setPrice(price);
        labTest.setStatus("BOOKED");
        labTest.setPaymentStatus("PAID"); // simplified — real gateway integration comes later
        labTest.setPaymentMethod(paymentMethod);
        labTest.setOrderId("LAB_" + System.currentTimeMillis());
        labTest.setBookedAt(LocalDateTime.now());

        return labTestRepository.save(labTest);
    }

    public LabTest getTestStatus(Long testId) {
        return labTestRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Lab test not found"));
    }

    public List<LabTest> getPatientTests(Long patientId) {
        return labTestRepository.findByPatientIdOrderByBookedAtDesc(patientId);
    }
}