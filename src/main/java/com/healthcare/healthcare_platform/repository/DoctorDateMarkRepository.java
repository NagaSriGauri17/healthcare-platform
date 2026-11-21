package com.healthcare.healthcare_platform.repository;

import com.healthcare.healthcare_platform.entity.DoctorDateMark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorDateMarkRepository extends JpaRepository<DoctorDateMark, Long> {
    List<DoctorDateMark> findByDoctorIdAndDateBetweenOrderByDate(Long doctorId, LocalDate from, LocalDate to);
    Optional<DoctorDateMark> findByDoctorIdAndDate(Long doctorId, LocalDate date);
}
