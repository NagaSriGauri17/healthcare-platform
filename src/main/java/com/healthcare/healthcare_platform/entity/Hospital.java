package com.healthcare.healthcare_platform.entity;

import jakarta.persistence.*;
import lombok.Data;

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
}