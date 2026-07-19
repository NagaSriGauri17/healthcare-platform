package com.healthcare.healthcare_platform.entity;
import jakarta.persistence.Transient;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
@Data
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Transient
    private String displayId;

    public String getDisplayId() {
        if (displayId != null) return displayId;
        if (doctor != null && doctor.getHospital() != null) {
            String hospitalName = doctor.getHospital().getName().toLowerCase();
            String prefix;
            if (hospitalName.contains("apollo")) prefix = "AP";
            else if (hospitalName.contains("kamineni")) prefix = "KM";
            else if (hospitalName.contains("manipal")) prefix = "MP";
            else if (hospitalName.contains("sunrise")) prefix = "SR";
            else if (hospitalName.contains("andhra")) prefix = "AH";
            else prefix = "AP";
            return prefix + "-" + id;
        }
        return "APT-" + id;
    }

    @ManyToOne
    @JoinColumn(name = "slot_id", nullable = false)
    private Slot slot;

    private String status;

    private String paymentStatus;

    private String type;

    private Double amount;

    private String orderId;

    private LocalDateTime createdAt;

    private String notes;

    private LocalDateTime followupDate;
}