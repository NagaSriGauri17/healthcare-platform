package com.healthcare.healthcare_platform.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "hospitals")
@Data
public class Hospital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String address;

    private String city;

    private String phone;

    private String email;

    private Double rating;

    private Double latitude;

    private Double longitude;

    // ICU beds / emergency desk — filled in by the hospital itself (null = not set yet)
    private Integer icuBedsTotal;

    private Integer icuBedsAvailable;

    private Boolean emergencyOpen;

    private String emergencyContact;

    private LocalDateTime bedsUpdatedAt;
}