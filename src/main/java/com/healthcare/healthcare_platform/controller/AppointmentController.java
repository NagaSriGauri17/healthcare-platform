package com.healthcare.healthcare_platform.controller;

import com.healthcare.healthcare_platform.entity.Appointment;
import com.healthcare.healthcare_platform.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping("/initiate")
    public ResponseEntity<Appointment> initiate(@RequestBody Map<String, Object> request) {
        Appointment appointment = appointmentService.initiateBooking(
                Long.valueOf(request.get("userId").toString()),
                Long.valueOf(request.get("slotId").toString()),
                (String) request.get("notes")
        );
        return ResponseEntity.ok(appointment);
    }

    @PostMapping("/confirm/{appointmentId}")
    public ResponseEntity<Appointment> confirm(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(appointmentService.confirmBooking(appointmentId));
    }

    @PostMapping("/{appointmentId}/cancel")
    public ResponseEntity<Appointment> cancel(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(appointmentService.cancelBooking(appointmentId));
    }

    @PutMapping("/{appointmentId}/reschedule")
    public ResponseEntity<Appointment> reschedule(@PathVariable Long appointmentId,
                                                  @RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(appointmentService.rescheduleBooking(
                appointmentId,
                Long.valueOf(request.get("newSlotId").toString())
        ));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Appointment>> getUserAppointments(@PathVariable Long userId) {
        return ResponseEntity.ok(appointmentService.getUserAppointments(userId));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Appointment>> getDoctorAppointments(@PathVariable Long doctorId) {
        return ResponseEntity.ok(appointmentService.getDoctorAppointments(doctorId));
    }

    @PostMapping("/{id}/followup")
    public ResponseEntity<Appointment> setFollowup(@PathVariable Long id,
                                                   @RequestBody Map<String, String> request) {
        return ResponseEntity.ok(appointmentService.setFollowup(id, request.get("followupDate")));
    }
}