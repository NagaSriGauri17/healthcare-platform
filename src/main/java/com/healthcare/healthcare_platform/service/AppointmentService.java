package com.healthcare.healthcare_platform.service;

import com.healthcare.healthcare_platform.entity.Appointment;
import com.healthcare.healthcare_platform.entity.Doctor;
import com.healthcare.healthcare_platform.entity.Slot;
import com.healthcare.healthcare_platform.entity.User;
import com.healthcare.healthcare_platform.repository.AppointmentRepository;
import com.healthcare.healthcare_platform.repository.DoctorRepository;
import com.healthcare.healthcare_platform.repository.SlotRepository;
import com.healthcare.healthcare_platform.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final SlotRepository slotRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;

    @Transactional
    public Appointment initiateBooking(Long userId, Long slotId, String notes) {
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        if (!slot.getStatus().equals("AVAILABLE")) {
            throw new RuntimeException("Slot is not available");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Doctor doctor = slot.getDoctor();

        slot.setStatus("PENDING");
        slotRepository.save(slot);

        Appointment appointment = new Appointment();
        appointment.setUser(user);
        appointment.setDoctor(doctor);
        appointment.setSlot(slot);
        appointment.setStatus("PENDING_PAYMENT");
        appointment.setPaymentStatus("PENDING");
        appointment.setAmount(doctor.getConsultationFee());
        appointment.setOrderId("ORDER_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setNotes(notes);
        appointment.setType("ONLINE");

        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment confirmBooking(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        appointment.setStatus("CONFIRMED");
        appointment.setPaymentStatus("PAID");

        Slot slot = appointment.getSlot();
        slot.setStatus("BOOKED");
        slotRepository.save(slot);

        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment cancelBooking(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        appointment.setStatus("CANCELLED");
        if (appointment.getPaymentStatus().equals("PAID")) {
            appointment.setPaymentStatus("REFUNDED");
        }

        Slot slot = appointment.getSlot();
        slot.setStatus("AVAILABLE");
        slotRepository.save(slot);

        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment rescheduleBooking(Long appointmentId, Long newSlotId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        Slot oldSlot = appointment.getSlot();
        oldSlot.setStatus("AVAILABLE");
        slotRepository.save(oldSlot);

        Slot newSlot = slotRepository.findById(newSlotId)
                .orElseThrow(() -> new RuntimeException("New slot not found"));

        if (!newSlot.getStatus().equals("AVAILABLE")) {
            throw new RuntimeException("New slot is not available");
        }

        newSlot.setStatus("BOOKED");
        slotRepository.save(newSlot);

        appointment.setSlot(newSlot);
        appointment.setStatus("CONFIRMED");
        return appointmentRepository.save(appointment);
    }

    public List<Appointment> getUserAppointments(Long userId) {
        return appointmentRepository.findByUserId(userId);
    }

    public List<Appointment> getDoctorAppointments(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }
    public Appointment setFollowup(Long appointmentId, String followupDateStr) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        appointment.setFollowupDate(LocalDateTime.parse(followupDateStr));
        return appointmentRepository.save(appointment);
    }
}