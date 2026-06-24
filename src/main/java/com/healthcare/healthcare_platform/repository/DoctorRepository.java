package com.healthcare.healthcare_platform.repository;

import com.healthcare.healthcare_platform.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findByHospitalId(Long hospitalId);

    @Query("SELECT d FROM Doctor d WHERE " +
            "LOWER(d.specialty.name) = LOWER(:specialty) AND " +
            "LOWER(d.hospital.city) = LOWER(:city)")
    List<Doctor> findBySpecialtyAndCity(@Param("specialty") String specialty,
                                        @Param("city") String city);

    @Query("SELECT d FROM Doctor d WHERE LOWER(d.specialty.name) = LOWER(:specialty)")
    List<Doctor> findBySpecialty(@Param("specialty") String specialty);
}