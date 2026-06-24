package com.healthcare.healthcare_platform.service;

import com.healthcare.healthcare_platform.entity.Doctor;
import com.healthcare.healthcare_platform.entity.Schedule;
import com.healthcare.healthcare_platform.entity.Slot;
import com.healthcare.healthcare_platform.repository.DoctorRepository;
import com.healthcare.healthcare_platform.repository.ScheduleRepository;
import com.healthcare.healthcare_platform.repository.SlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SlotService {

    private final ScheduleRepository scheduleRepository;
    private final SlotRepository slotRepository;
    private final DoctorRepository doctorRepository;

    public Schedule setSchedule(Long doctorId, String dayOfWeek,
                                String startTime, String endTime,
                                Integer slotDurationMinutes) {
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        Schedule schedule = new Schedule();
        schedule.setDoctor(doctor);
        schedule.setDayOfWeek(dayOfWeek.toUpperCase());
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        schedule.setSlotDurationMinutes(slotDurationMinutes);
        schedule.setIsAvailable(true);
        return scheduleRepository.save(schedule);
    }

    public List<Slot> generateSlots(Long doctorId) {
        List<Schedule> schedules = scheduleRepository.findByDoctorId(doctorId);
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        List<Slot> generatedSlots = new ArrayList<>();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalDate today = LocalDate.now();

        for (int i = 0; i < 30; i++) {
            LocalDate date = today.plusDays(i);
            String dayOfWeek = date.getDayOfWeek().name().substring(0, 3);

            for (Schedule schedule : schedules) {
                String scheduledDay = schedule.getDayOfWeek().substring(0, 3);
                if (!scheduledDay.equalsIgnoreCase(dayOfWeek)) continue;

                List<Slot> existingSlots = slotRepository
                        .findByDoctorIdAndDate(doctorId, date);
                if (!existingSlots.isEmpty()) continue;

                LocalTime current = LocalTime.parse(schedule.getStartTime(), timeFormatter);
                LocalTime end = LocalTime.parse(schedule.getEndTime(), timeFormatter);

                while (current.plusMinutes(schedule.getSlotDurationMinutes()).compareTo(end) <= 0) {
                    Slot slot = new Slot();
                    slot.setSchedule(schedule);
                    slot.setDoctor(doctor);
                    slot.setDate(date);
                    slot.setStartTime(current.format(timeFormatter));
                    slot.setEndTime(current.plusMinutes(
                            schedule.getSlotDurationMinutes()).format(timeFormatter));
                    slot.setStatus("AVAILABLE");
                    generatedSlots.add(slotRepository.save(slot));
                    current = current.plusMinutes(schedule.getSlotDurationMinutes());
                }
            }
        }
        return generatedSlots;
    }

    public List<Slot> getAvailableSlots(Long doctorId, LocalDate date) {
        return slotRepository.findByDoctorIdAndDateAndStatusOrderByStartTime(
                doctorId, date, "AVAILABLE");
    }
}