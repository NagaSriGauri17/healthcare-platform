package com.healthcare.healthcare_platform.repository;

import com.healthcare.healthcare_platform.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByUserId(Long userId);
    List<Appointment> findByDoctorId(Long doctorId);
    List<Appointment> findByStatus(String status);

    @Query("SELECT a FROM Appointment a WHERE a.doctor.hospital.id = :hospitalId ORDER BY a.createdAt ASC")
    List<Appointment> findByHospitalIdOrderByCreatedAtAsc(@Param("hospitalId") Long hospitalId);
}