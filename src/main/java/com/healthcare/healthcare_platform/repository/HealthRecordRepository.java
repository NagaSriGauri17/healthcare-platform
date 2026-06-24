package com.healthcare.healthcare_platform.repository;

import com.healthcare.healthcare_platform.entity.HealthRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HealthRecordRepository extends JpaRepository<HealthRecord, Long> {
    List<HealthRecord> findByPatientIdOrderByUploadedAtDesc(Long patientId);
    List<HealthRecord> findByAppointmentIdOrderByUploadedAtDesc(Long appointmentId);
}