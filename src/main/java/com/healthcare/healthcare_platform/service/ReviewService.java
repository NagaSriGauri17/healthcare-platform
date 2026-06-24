package com.healthcare.healthcare_platform.service;

import com.healthcare.healthcare_platform.entity.Review;
import com.healthcare.healthcare_platform.repository.AppointmentRepository;
import com.healthcare.healthcare_platform.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final AppointmentRepository appointmentRepository;

    public Review addReview(Long patientId, Long doctorId, Long appointmentId,
                            Integer rating, String comment) {

        // Only allow review if appointment is COMPLETED
        var appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!"COMPLETED".equals(appointment.getStatus())) {
            throw new RuntimeException(
                    "Cannot review — appointment status is " + appointment.getStatus() +
                            ". Reviews are only allowed after consultation is completed.");
        }

        // Prevent duplicate reviews for same appointment
        if (reviewRepository.existsByAppointmentId(appointmentId)) {
            throw new RuntimeException("You have already reviewed this appointment");
        }

        if (rating < 1 || rating > 5) {
            throw new RuntimeException("Rating must be between 1 and 5");
        }

        Review review = new Review();
        review.setPatientId(patientId);
        review.setDoctorId(doctorId);
        review.setAppointmentId(appointmentId);
        review.setRating(rating);
        review.setComment(comment);
        review.setCreatedAt(LocalDateTime.now());

        return reviewRepository.save(review);
    }

    public Map<String, Object> getDoctorRating(Long doctorId) {
        List<Review> reviews = reviewRepository.findByDoctorIdOrderByCreatedAtDesc(doctorId);
        Double average = reviewRepository.findAverageRatingByDoctorId(doctorId);

        Map<String, Object> result = new HashMap<>();
        result.put("doctorId", doctorId);
        result.put("averageRating", average != null ? Math.round(average * 10.0) / 10.0 : 0.0);
        result.put("totalReviews", reviews.size());
        result.put("reviews", reviews);
        return result;
    }
}