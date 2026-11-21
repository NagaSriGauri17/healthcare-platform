package com.healthcare.healthcare_platform.repository;

import com.healthcare.healthcare_platform.entity.Slot;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SlotRepository extends JpaRepository<Slot, Long> {
    List<Slot> findByDoctorIdAndDateAndStatusOrderByStartTime(
            Long doctorId, LocalDate date, String status);
    List<Slot> findByDoctorIdAndDate(Long doctorId, LocalDate date);

    /** Locks the slot row until the transaction ends, so app bookings and walk-ins cannot both take it. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Slot s where s.id = :id")
    java.util.Optional<Slot> findByIdForUpdate(@Param("id") Long id);
}