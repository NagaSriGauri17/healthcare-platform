package com.healthcare.healthcare_platform.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "schedules")
@Data
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    private String dayOfWeek;

    private String startTime;

    private String endTime;

    private Integer slotDurationMinutes;

    private Boolean isAvailable;
}