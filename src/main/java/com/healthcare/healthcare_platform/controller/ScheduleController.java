package com.healthcare.healthcare_platform.controller;

import com.healthcare.healthcare_platform.entity.DoctorDateMark;
import com.healthcare.healthcare_platform.entity.Schedule;
import com.healthcare.healthcare_platform.entity.Slot;
import com.healthcare.healthcare_platform.service.SlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final SlotService slotService;

    @PostMapping("/set")
    public ResponseEntity<Schedule> setSchedule(@RequestBody Map<String, Object> request) {
        Schedule schedule = slotService.setSchedule(
                Long.valueOf(request.get("doctorId").toString()),
                (String) request.get("dayOfWeek"),
                (String) request.get("startTime"),
                (String) request.get("endTime"),
                Integer.valueOf(request.get("slotDurationMinutes").toString())
        );
        return ResponseEntity.ok(schedule);
    }

    @PostMapping("/regenerate/{doctorId}")
    public ResponseEntity<List<Slot>> regenerate(@PathVariable Long doctorId) {
        return ResponseEntity.ok(slotService.generateSlots(doctorId));
    }

    @GetMapping("/doctor/{doctorId}/slots")
    public ResponseEntity<List<Slot>> getSlots(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(slotService.getAvailableSlots(doctorId, date));
    }

    // ───────── Calendar marks: grey = REMEMBER, red = UNAVAILABLE ─────────

    @GetMapping("/doctor/{doctorId}/marks")
    public ResponseEntity<List<DoctorDateMark>> getMarks(@PathVariable Long doctorId,
                                                         @RequestParam int year) {
        return ResponseEntity.ok(slotService.getDateMarks(doctorId, year));
    }

    @PostMapping("/doctor/{doctorId}/marks")
    public ResponseEntity<?> setMark(@PathVariable Long doctorId,
                                     @RequestBody Map<String, Object> request) {
        if (request.get("date") == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Date is required"));
        }
        try {
            SlotService.MarkResult result = slotService.setDateMark(
                    doctorId,
                    LocalDate.parse(request.get("date").toString()),
                    request.get("type") == null ? null : request.get("type").toString(),
                    request.get("note") == null ? null : request.get("note").toString());
            return ResponseEntity.ok(Map.of(
                    "date", result.mark().getDate().toString(),
                    "type", result.mark().getType(),
                    "note", result.mark().getNote() == null ? "" : result.mark().getNote(),
                    "bookedSlots", result.bookedSlots()));
        } catch (IllegalArgumentException | java.time.format.DateTimeParseException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/doctor/{doctorId}/marks")
    public ResponseEntity<Void> clearMark(@PathVariable Long doctorId,
                                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        slotService.clearDateMark(doctorId, date);
        return ResponseEntity.noContent().build();
    }
}
