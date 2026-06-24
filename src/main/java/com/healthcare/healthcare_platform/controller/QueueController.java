package com.healthcare.healthcare_platform.controller;

import com.healthcare.healthcare_platform.entity.Appointment;
import com.healthcare.healthcare_platform.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;

    // Staff books a walk-in patient
    @PostMapping("/walkin")
    public ResponseEntity<Appointment> walkIn(@RequestBody Map<String, Object> request) {
        Appointment appointment = queueService.walkInBooking(
                Long.valueOf(request.get("doctorId").toString()),
                Long.valueOf(request.get("slotId").toString()),
                (String) request.get("patientName"),
                (String) request.get("patientPhone")
        );
        return ResponseEntity.ok(appointment);
    }

    // Staff checks in a patient — token is born here
    @PostMapping("/checkin/{appointmentId}")
    public ResponseEntity<Map<String, Object>> checkIn(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(queueService.checkIn(appointmentId));
    }

    // Staff advances the queue — marks current as done, moves to next
    @PostMapping("/next/{doctorId}")
    public ResponseEntity<Map<String, Object>> nextPatient(@PathVariable Long doctorId) {
        return ResponseEntity.ok(queueService.advanceQueue(doctorId));
    }

    // Staff skips current patient — moves them to back of line
    @PostMapping("/skip/{doctorId}")
    public ResponseEntity<Map<String, Object>> skipPatient(@PathVariable Long doctorId) {
        return ResponseEntity.ok(queueService.skipToken(doctorId));
    }

    // Get live queue status for a doctor (staff dashboard)
    @GetMapping("/status/{doctorId}")
    public ResponseEntity<Map<String, Object>> getStatus(@PathVariable Long doctorId) {
        return ResponseEntity.ok(queueService.getQueueStatus(doctorId));
    }

    // Get patient's personal token status (patient app — live queue screen)
    @GetMapping("/token/{appointmentId}")
    public ResponseEntity<Map<String, Object>> getTokenStatus(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(queueService.getPatientTokenStatus(appointmentId));
    }

    // Staff puts a token on hold
    @PostMapping("/hold/{doctorId}/{tokenNumber}")
    public ResponseEntity<Map<String, Object>> holdToken(
            @PathVariable Long doctorId,
            @PathVariable Integer tokenNumber) {
        return ResponseEntity.ok(queueService.holdToken(doctorId, tokenNumber));
    }

    @PostMapping("/resume/{doctorId}/{tokenNumber}")
    public ResponseEntity<Map<String, Object>> resumeToken(
            @PathVariable Long doctorId,
            @PathVariable Integer tokenNumber) {
        return ResponseEntity.ok(queueService.resumeToken(doctorId, tokenNumber));
    }
}