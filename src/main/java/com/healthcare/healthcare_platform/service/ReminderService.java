package com.healthcare.healthcare_platform.service;

import com.healthcare.healthcare_platform.entity.Appointment;
import com.healthcare.healthcare_platform.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReminderService {

    private final AppointmentRepository appointmentRepository;
    private final FirebaseService firebaseService;

    @Scheduled(fixedRate = 1800000)
    public void sendReminders() {
        System.out.println("Running reminder check at: " + LocalDateTime.now());
        int[] hoursWindows = {24, 12, 6, 3, 1};

        for (int hours : hoursWindows) {
            LocalDateTime windowStart = LocalDateTime.now().plusHours(hours).minusMinutes(15);
            LocalDateTime windowEnd = LocalDateTime.now().plusHours(hours).plusMinutes(15);

            List<Appointment> appointments = appointmentRepository.findByStatus("CONFIRMED");

            for (Appointment apt : appointments) {
                if (apt.getSlot() != null && apt.getSlot().getDate() != null) {
                    LocalDateTime aptTime = apt.getSlot().getDate().atTime(
                            Integer.parseInt(apt.getSlot().getStartTime().split(":")[0]),
                            Integer.parseInt(apt.getSlot().getStartTime().split(":")[1])
                    );

                    if (aptTime.isAfter(windowStart) && aptTime.isBefore(windowEnd)) {
                        System.out.println("Reminder: Appointment " + apt.getId() +
                                " is in " + hours + " hours for patient " +
                                apt.getUser().getName());
                    }
                }
            }
        }
    }
}