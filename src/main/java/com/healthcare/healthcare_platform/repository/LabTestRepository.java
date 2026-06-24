package com.healthcare.healthcare_platform.repository;

import com.healthcare.healthcare_platform.entity.LabTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LabTestRepository extends JpaRepository<LabTest, Long> {
    List<LabTest> findByPatientIdOrderByBookedAtDesc(Long patientId);
}