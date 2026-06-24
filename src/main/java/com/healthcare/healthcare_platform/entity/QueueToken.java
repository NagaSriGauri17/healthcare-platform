package com.healthcare.healthcare_platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "queue_tokens")
@Data
@NoArgsConstructor
public class QueueToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    private Long doctorId;
    private Integer tokenNumber;
    private String status; // WAITING, IN_PROGRESS, COMPLETED, SKIPPED
    private LocalDateTime checkedInAt;
    private LocalDateTime completedAt;
}