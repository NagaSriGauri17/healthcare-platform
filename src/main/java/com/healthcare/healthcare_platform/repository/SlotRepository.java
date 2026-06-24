package com.healthcare.healthcare_platform.repository;

import com.healthcare.healthcare_platform.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SlotRepository extends JpaRepository<Slot, Long> {
    List<Slot> findByDoctorIdAndDateAndStatusOrderByStartTime(
            Long doctorId, LocalDate date, String status);
    List<Slot> findByDoctorIdAndDate(Long doctorId, LocalDate date);
}