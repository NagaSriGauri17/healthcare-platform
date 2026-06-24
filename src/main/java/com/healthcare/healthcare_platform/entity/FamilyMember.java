package com.healthcare.healthcare_platform.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "family_members")
@Data
public class FamilyMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    private String relationship;

    private String phone;

    private Integer age;
}