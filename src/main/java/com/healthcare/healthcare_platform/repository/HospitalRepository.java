package com.healthcare.healthcare_platform.repository;

import com.healthcare.healthcare_platform.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, Long> {

    List<Hospital> findByCityIgnoreCase(String city);

    List<Hospital> findByNameContainingIgnoreCase(String name);

    @Query(value = """
            SELECT * FROM hospitals h
            WHERE h.latitude IS NOT NULL AND h.longitude IS NOT NULL
            AND (6371 * acos(cos(radians(:lat)) * cos(radians(h.latitude))
            * cos(radians(h.longitude) - radians(:lng))
            + sin(radians(:lat)) * sin(radians(h.latitude)))) < :radius
            """, nativeQuery = true)
    List<Hospital> findNearbyHospitals(@Param("lat") Double lat,
                                       @Param("lng") Double lng,
                                       @Param("radius") Double radius);
}