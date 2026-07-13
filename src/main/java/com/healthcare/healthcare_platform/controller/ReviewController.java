package com.healthcare.healthcare_platform.controller;

import com.healthcare.healthcare_platform.entity.Review;
import com.healthcare.healthcare_platform.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/add")
    public ResponseEntity<?> addReview(@RequestBody Map<String, Object> request) {
        try {
            Long doctorId = request.get("doctorId") != null ? Long.valueOf(request.get("doctorId").toString()) : null;
            Long hospitalId = request.get("hospitalId") != null ? Long.valueOf(request.get("hospitalId").toString()) : null;

            Review review = reviewService.addReview(
                    Long.valueOf(request.get("patientId").toString()),
                    doctorId,
                    hospitalId,
                    Long.valueOf(request.get("appointmentId").toString()),
                    Integer.valueOf(request.get("rating").toString()),
                    (String) request.get("comment"),
                    (String) request.get("type")
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

    @GetMapping("/doctor/{doctorId}/list")
    public ResponseEntity<List<Review>> getDoctorReviews(@PathVariable Long doctorId) {
        return ResponseEntity.ok(reviewService.getDoctorReviews(doctorId));
    }

    @GetMapping("/hospital/{hospitalId}/list")
    public ResponseEntity<List<Review>> getHospitalReviews(@PathVariable Long hospitalId) {
        return ResponseEntity.ok(reviewService.getHospitalReviews(hospitalId));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<String> deleteReview(@PathVariable Long reviewId) {
        return ResponseEntity.ok(reviewService.deleteReview(reviewId));
    }
}