package com.healthcare.healthcare_platform.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import java.util.*;

@RestController
@RequiredArgsConstructor
public class MockApiController {

    // ─── EMERGENCY — ICU BED AVAILABILITY ──────────────────────────────────────

    @GetMapping("/api/emergency/beds")
    public ResponseEntity<List<Map<String, Object>>> getEmergencyBeds() {
        List<Map<String, Object>> beds = new ArrayList<>();

        beds.add(createHospitalBeds("Apollo Hospital", "Vijayawada", 4, 12, true));
        beds.add(createHospitalBeds("KIMS Hospital", "Vijayawada", 1, 8, true));
        beds.add(createHospitalBeds("Andhra Hospitals", "Vijayawada", 0, 6, false));
        beds.add(createHospitalBeds("NRI General Hospital", "Guntur", 3, 10, true));
        beds.add(createHospitalBeds("Ramesh Hospitals", "Vijayawada", 2, 15, true));

        return ResponseEntity.ok(beds);
    }

    private Map<String, Object> createHospitalBeds(String name, String city,
                                                   int available, int total,
                                                   boolean emergencyOpen) {
        Map<String, Object> hospital = new HashMap<>();
        hospital.put("hospitalName", name);
        hospital.put("city", city);
        hospital.put("icuBedsAvailable", available);
        hospital.put("icuBedsTotal", total);
        hospital.put("emergencyOpen", emergencyOpen);
        hospital.put("emergencyContact", "0866-" + (100000 + new Random().nextInt(900000)));
        hospital.put("lastUpdated", LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        return hospital;
    }

    // ─── AMBULANCE BOOKING ───────────────────────────────────────────────────────

    @PostMapping("/api/ambulance/book")
    public ResponseEntity<Map<String, Object>> bookAmbulance(
            @RequestBody Map<String, Object> request) {

        String[] drivers = {"Ravi Kumar", "Suresh Babu", "Venkat Rao", "Krishna Murthy"};
        String[] vehicles = {"AP16AB1234", "AP37CD5678", "AP39EF9012", "AP16GH3456"};
        int idx = new Random().nextInt(4);

        Map<String, Object> response = new HashMap<>();
        response.put("bookingId", "AMB_" + System.currentTimeMillis());
        response.put("status", "DISPATCHED");
        response.put("driverName", drivers[idx]);
        response.put("driverPhone", "98765" + (10000 + new Random().nextInt(90000)));
        response.put("vehicleNumber", vehicles[idx]);
        response.put("estimatedArrivalMinutes", 5 + new Random().nextInt(10));
        response.put("trackerUrl", "https://track.healthcare-platform.com/amb/" + System.currentTimeMillis());
        response.put("dispatchedAt", LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return ResponseEntity.ok(response);
    }

    // ─── AMBULANCE TRACKING ──────────────────────────────────────────────────────

    @GetMapping("/api/ambulance/track/{bookingId}")
    public ResponseEntity<Map<String, Object>> trackAmbulance(
            @PathVariable String bookingId) {

        Map<String, Object> response = new HashMap<>();
        response.put("bookingId", bookingId);
        response.put("status", "EN_ROUTE");
        response.put("currentLocation", Map.of(
                "lat", 16.5062 + (Math.random() * 0.01),
                "lng", 80.6480 + (Math.random() * 0.01)
        ));
        response.put("estimatedArrivalMinutes", 3 + new Random().nextInt(5));
        response.put("distanceKm", 1.2 + Math.random() * 2);
        return ResponseEntity.ok(response);
    }

    // ─── INSURANCE — CLAIM STATUS ────────────────────────────────────────────────

    @GetMapping("/api/insurance/claim/{claimId}/status")
    public ResponseEntity<Map<String, Object>> getClaimStatus(
            @PathVariable String claimId) {

        Map<String, Object> response = new HashMap<>();
        response.put("claimId", claimId);
        response.put("status", "UNDER_REVIEW");
        response.put("insurer", "Star Health Insurance");
        response.put("policyNumber", "P-SH-2024-" + claimId);
        response.put("claimAmount", 15000.0);
        response.put("approvedAmount", null);
        response.put("submittedDate", "2026-06-20");
        response.put("lastUpdated", "2026-06-24");
        response.put("remarks", "Documents verified. Awaiting medical team review.");
        response.put("timeline", List.of(
                Map.of("stage", "SUBMITTED", "date", "2026-06-20", "done", true),
                Map.of("stage", "DOCUMENTS_VERIFIED", "date", "2026-06-22", "done", true),
                Map.of("stage", "UNDER_REVIEW", "date", "2026-06-24", "done", true),
                Map.of("stage", "APPROVED", "date", "", "done", false),
                Map.of("stage", "SETTLED", "date", "", "done", false)
        ));
        return ResponseEntity.ok(response);
    }

    // ─── INSURANCE — INITIATE CLAIM ──────────────────────────────────────────────

    @PostMapping("/api/insurance/claim/initiate")
    public ResponseEntity<Map<String, Object>> initiateClaim(
            @RequestBody Map<String, Object> request) {

        String claimId = "CLM-" + System.currentTimeMillis();
        Map<String, Object> response = new HashMap<>();
        response.put("claimId", claimId);
        response.put("status", "SUBMITTED");
        response.put("message", "Your claim has been submitted successfully");
        response.put("insurer", request.get("insurerName"));
        response.put("policyNumber", request.get("policyNumber"));
        response.put("estimatedProcessingDays", 7);
        response.put("submittedAt", LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return ResponseEntity.ok(response);
    }

    // ─── HOME SAMPLE COLLECTION ──────────────────────────────────────────────────

    @PostMapping("/api/lab/home-collection/book")
    public ResponseEntity<Map<String, Object>> bookHomeCollection(
            @RequestBody Map<String, Object> request) {

        Map<String, Object> response = new HashMap<>();
        response.put("bookingId", "HC_" + System.currentTimeMillis());
        response.put("status", "CONFIRMED");
        response.put("technicianName", "Prasad Rao");
        response.put("technicianPhone", "9876512345");
        response.put("scheduledDate", request.get("preferredDate"));
        response.put("scheduledTime", request.get("preferredTime"));
        response.put("address", request.get("address"));
        response.put("instructions", "Please fast for 8-12 hours before sample collection. Keep your ID proof ready.");
        response.put("reportDelivery", "Results will be available in 24 hours in your health locker");
        return ResponseEntity.ok(response);
    }

    // ─── AI SYMPTOM CHECKER ──────────────────────────────────────────────────────

    @PostMapping("/api/ai/chat")
    public ResponseEntity<Map<String, Object>> aiChat(
            @RequestBody Map<String, Object> request) {

        String message = request.get("message") != null ?
                request.get("message").toString().toLowerCase() : "";

        String reply;
        String suggestedSpecialty;
        String urgencyLevel;
        boolean suggestDoctor;

        if (message.contains("fever") || message.contains("temperature")) {
            reply = "I understand you have a fever. How many days has this been going on? Do you also have body ache, cough, or cold?";
            suggestedSpecialty = "General Medicine";
            urgencyLevel = "MEDIUM";
            suggestDoctor = false;
        } else if (message.contains("chest pain") || message.contains("heart")) {
            reply = "Chest pain can be serious. Are you experiencing shortness of breath, sweating, or pain radiating to your arm? Please seek immediate medical attention if symptoms are severe.";
            suggestedSpecialty = "Cardiology";
            urgencyLevel = "HIGH";
            suggestDoctor = true;
        } else if (message.contains("headache") || message.contains("migraine")) {
            reply = "I see you have a headache. Is it a throbbing pain on one side? How long has it been? Any nausea or sensitivity to light?";
            suggestedSpecialty = "Neurology";
            urgencyLevel = "LOW";
            suggestDoctor = false;
        } else if (message.contains("skin") || message.contains("rash") || message.contains("itch")) {
            reply = "Skin issues can have various causes. Is there redness, swelling, or discharge? How long have you had this rash?";
            suggestedSpecialty = "Dermatology";
            urgencyLevel = "LOW";
            suggestDoctor = false;
        } else if (message.contains("stomach") || message.contains("abdomen") || message.contains("vomit")) {
            reply = "Stomach problems are common. Are you experiencing nausea, vomiting, or diarrhea? When did the pain start?";
            suggestedSpecialty = "General Medicine";
            urgencyLevel = "MEDIUM";
            suggestDoctor = false;
        } else if (message.contains("diabetes") || message.contains("sugar") || message.contains("blood sugar")) {
            reply = "For diabetes management, regular monitoring is key. Are you experiencing increased thirst, frequent urination, or fatigue?";
            suggestedSpecialty = "Endocrinology";
            urgencyLevel = "MEDIUM";
            suggestDoctor = true;
        } else if (message.contains("child") || message.contains("baby") || message.contains("infant")) {
            reply = "For your child's health concern, could you tell me their age and what symptoms they are showing?";
            suggestedSpecialty = "Pediatrics";
            urgencyLevel = "MEDIUM";
            suggestDoctor = false;
        } else if (message.contains("hello") || message.contains("hi") || message.contains("help")) {
            reply = "Hello! I am your AI health assistant. Please describe your symptoms and I will help guide you to the right specialist. What are you experiencing?";
            suggestedSpecialty = null;
            urgencyLevel = "NONE";
            suggestDoctor = false;
        } else {
            reply = "I understand you are not feeling well. Could you describe your symptoms in more detail? For example, where is the pain or discomfort? How long have you had these symptoms?";
            suggestedSpecialty = "General Medicine";
            urgencyLevel = "LOW";
            suggestDoctor = false;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("reply", reply);
        response.put("suggestedSpecialty", suggestedSpecialty);
        response.put("urgencyLevel", urgencyLevel);
        response.put("suggestTalkToDoctor", suggestDoctor);
        response.put("disclaimer", "This is AI guidance only. Please consult a real doctor for medical advice.");
        return ResponseEntity.ok(response);
    }
}