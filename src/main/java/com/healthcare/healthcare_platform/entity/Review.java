package com.healthcare.healthcare_platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long patientId;
    private String patientName;
    private Long doctorId;
    private Long hospitalId;
    private Long appointmentId;

    private Integer rating;
    private String comment;
    private String type; // "DOCTOR" or "HOSPITAL"

    private LocalDateTime createdAt;
}