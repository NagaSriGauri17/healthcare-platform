package com.healthcare.healthcare_platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String phone;

    private String role;

    private LocalDateTime createdAt;

    private String fcmToken;

    private String otpCode;

    private Long hospitalId;

    // Patient details collected at sign-up (editable later from the profile screen)
    private Integer age;

    private String address;

    private String emergencyPhone;

    private Boolean profileComplete;
}