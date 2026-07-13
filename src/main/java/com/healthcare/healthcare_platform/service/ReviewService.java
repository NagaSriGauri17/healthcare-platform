package com.healthcare.healthcare_platform.service;

import com.healthcare.healthcare_platform.entity.Review;
import com.healthcare.healthcare_platform.entity.User;
import com.healthcare.healthcare_platform.repository.ReviewRepository;
import com.healthcare.healthcare_platform.repository.UserRepository;
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
    private final UserRepository userRepository;

    public Review addReview(Long patientId, Long doctorId, Long hospitalId, Long appointmentId,
                            Integer rating, String comment, String type) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new RuntimeException("Rating must be between 1 and 5");
        }
        String patientName = userRepository.findById(patientId)
                .map(User::getName).orElse("Patient");

        Review review = new Review();
        review.setPatientId(patientId);
        review.setPatientName(patientName);
        review.setDoctorId(doctorId);
        review.setHospitalId(hospitalId);
        review.setAppointmentId(appointmentId);
        review.setRating(rating);
        review.setComment(comment);
        review.setType(type != null ? type : "DOCTOR");
        review.setCreatedAt(LocalDateTime.now());

        return reviewRepository.save(review);
    }

    public Map<String, Object> getDoctorRating(Long doctorId) {
        List<Review> reviews = reviewRepository.findByDoctorIdOrderByCreatedAtDesc(doctorId);
        double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        Map<String, Object> result = new HashMap<>();
        result.put("averageRating", Math.round(avg * 10.0) / 10.0);
        result.put("totalReviews", reviews.size());
        return result;
    }

    public List<Review> getDoctorReviews(Long doctorId) {
        return reviewRepository.findByDoctorIdOrderByCreatedAtDesc(doctorId);
    }

    public List<Review> getHospitalReviews(Long hospitalId) {
        return reviewRepository.findByHospitalIdOrderByCreatedAtDesc(hospitalId);
    }

    public String deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
        return "Review deleted successfully";
    }
}