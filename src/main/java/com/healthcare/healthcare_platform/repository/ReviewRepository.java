package com.healthcare.healthcare_platform.repository;

import com.healthcare.healthcare_platform.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByDoctorIdOrderByCreatedAtDesc(Long doctorId);
    boolean existsByAppointmentId(Long appointmentId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.doctorId = :doctorId")
    Double findAverageRatingByDoctorId(Long doctorId);
}