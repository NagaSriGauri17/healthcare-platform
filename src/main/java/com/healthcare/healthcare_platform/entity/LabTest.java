package com.healthcare.healthcare_platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "lab_tests")
@Data
@NoArgsConstructor
public class LabTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long patientId;
    private String testName;
    private String testCode;
    private String centerName;
    private Double price;
    private String status;
    private String orderId;
    private String paymentStatus;
    private String paymentMethod;
    private String reportUrl;
    private LocalDateTime bookedAt;
    private LocalDateTime reportUploadedAt;
}