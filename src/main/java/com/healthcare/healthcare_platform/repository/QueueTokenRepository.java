package com.healthcare.healthcare_platform.repository;

import com.healthcare.healthcare_platform.entity.QueueToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QueueTokenRepository extends JpaRepository<QueueToken, Long> {
    List<QueueToken> findByDoctorIdAndStatusOrderByTokenNumberAsc(Long doctorId, String status);
    Optional<QueueToken> findFirstByAppointmentIdOrderByIdDesc(Long appointmentId);
    Optional<QueueToken> findFirstByDoctorIdAndStatusOrderByTokenNumberAsc(Long doctorId, String status);
}