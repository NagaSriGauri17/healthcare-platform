package com.healthcare.healthcare_platform.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * A date-specific mark on a doctor's calendar.
 *   type = "REMEMBER"    -> grey: a day to remember (holiday, event). No slots are offered.
 *   type = "UNAVAILABLE" -> red: the doctor will not be available. No slots are offered.
 */
@Entity
@Table(name = "doctor_date_marks",
        uniqueConstraints = @UniqueConstraint(columnNames = {"doctor_id", "mark_date"}))
@Data
public class DoctorDateMark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "mark_date", nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String type;

    private String note;
}
