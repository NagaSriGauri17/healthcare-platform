package com.healthcare.healthcare_platform.controller;

import com.healthcare.healthcare_platform.entity.Review;
import com.healthcare.healthcare_platform.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/add")
    public ResponseEntity<?> addReview(@RequestBody Map<String, Object> request) {
        try {
            Review review = reviewService.addReview(
                    Long.valueOf(request.get("patientId").toString()),
                    Long.valueOf(request.get("doctorId").toString()),
                    Long.valueOf(request.get("appointmentId").toString()),
                    Integer.valueOf(request.get("rating").toString()),
                    (String) request.get("comment")
            );
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<Map<String, Object>> getDoctorRating(@PathVariable Long doctorId) {
        return ResponseEntity.ok(reviewService.getDoctorRating(doctorId));
    }
}