package com.healthcare.healthcare_platform.controller;

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
}