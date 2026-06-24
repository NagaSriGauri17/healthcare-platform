package com.healthcare.healthcare_platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "health_records_v2")
@Data
@NoArgsConstructor
public class HealthRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long patientId;
    private Long appointmentId;
    private String recordType;
    private String fileName;
    private String s3Key;
    private String fileSize;
    private String uploadedBy;
    private LocalDateTime uploadedAt;
}