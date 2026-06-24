package com.healthcare.healthcare_platform.repository;

import com.healthcare.healthcare_platform.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByDoctorId(Long doctorId);
    List<Schedule> findByDoctorIdAndDayOfWeek(Long doctorId, String dayOfWeek);
}