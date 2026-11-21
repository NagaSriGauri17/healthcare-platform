package com.healthcare.healthcare_platform.service;

import com.healthcare.healthcare_platform.entity.Doctor;
import com.healthcare.healthcare_platform.entity.DoctorDateMark;
import com.healthcare.healthcare_platform.entity.Schedule;
import com.healthcare.healthcare_platform.entity.Slot;
import com.healthcare.healthcare_platform.repository.DoctorDateMarkRepository;
import com.healthcare.healthcare_platform.repository.DoctorRepository;
import com.healthcare.healthcare_platform.repository.ScheduleRepository;
import com.healthcare.healthcare_platform.repository.SlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SlotService {

    private final ScheduleRepository scheduleRepository;
    private final SlotRepository slotRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorDateMarkRepository dateMarkRepository;

    public Schedule setSchedule(Long doctorId, String dayOfWeek,
                                String startTime, String endTime,
                                Integer slotDurationMinutes) {
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        // Re-use the existing row for this weekday (if any) so repeated saves don't pile up duplicates
        List<Schedule> existing = scheduleRepository
                .findByDoctorIdAndDayOfWeek(doctorId, dayOfWeek.toUpperCase());
        Schedule schedule = existing.isEmpty() ? new Schedule() : existing.get(0);
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

        // Dates the doctor has marked (grey "remember" or red "not available") get no slots
        Set<LocalDate> markedDates = new HashSet<>();
        for (DoctorDateMark mark : dateMarkRepository
                .findByDoctorIdAndDateBetweenOrderByDate(doctorId, today, today.plusDays(29))) {
            markedDates.add(mark.getDate());
        }

        for (int i = 0; i < 30; i++) {
            LocalDate date = today.plusDays(i);
            if (markedDates.contains(date)) continue;
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
        ensureSlotsForDate(doctorId, date);
        return slotRepository.findByDoctorIdAndDateAndStatusOrderByStartTime(
                doctorId, date, "AVAILABLE");
    }

    /**
     * Creates the slots for one date from the doctor's weekly schedule, if they do not exist yet.
     * Both the patient app and the hospital portal read slots through getAvailableSlots, so they
     * always see the same slots (and the same booked/free status).
     */
    private synchronized void ensureSlotsForDate(Long doctorId, LocalDate date) {
        LocalDate today = LocalDate.now();
        if (date.isBefore(today) || date.isAfter(today.plusDays(60))) return;
        if (!slotRepository.findByDoctorIdAndDate(doctorId, date).isEmpty()) return;
        if (dateMarkRepository.findByDoctorIdAndDate(doctorId, date).isPresent()) return;

        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        if (doctor == null) return;

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String dayOfWeek = date.getDayOfWeek().name().substring(0, 3);
        List<Slot> toSave = new ArrayList<>();

        for (Schedule schedule : scheduleRepository.findByDoctorId(doctorId)) {
            if (Boolean.FALSE.equals(schedule.getIsAvailable())) continue;
            if (!schedule.getDayOfWeek().substring(0, 3).equalsIgnoreCase(dayOfWeek)) continue;

            LocalTime current = LocalTime.parse(schedule.getStartTime(), timeFormatter);
            LocalTime end = LocalTime.parse(schedule.getEndTime(), timeFormatter);
            int minutes = schedule.getSlotDurationMinutes() == null ? 30 : schedule.getSlotDurationMinutes();

            while (current.plusMinutes(minutes).compareTo(end) <= 0) {
                Slot slot = new Slot();
                slot.setSchedule(schedule);
                slot.setDoctor(doctor);
                slot.setDate(date);
                slot.setStartTime(current.format(timeFormatter));
                slot.setEndTime(current.plusMinutes(minutes).format(timeFormatter));
                slot.setStatus("AVAILABLE");
                toSave.add(slot);
                current = current.plusMinutes(minutes);
            }
        }
        if (!toSave.isEmpty()) slotRepository.saveAll(toSave);
    }

    /** Gives every doctor who has no weekly schedule yet a default one: Mon–Sat, 09:00–13:00, 30-minute slots. */
    public int seedDefaultSchedules() {
        String[] days = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"};
        int doctorsUpdated = 0;
        for (Doctor doctor : doctorRepository.findAll()) {
            if (!scheduleRepository.findByDoctorId(doctor.getId()).isEmpty()) continue;
            List<Schedule> rows = new ArrayList<>();
            for (String day : days) {
                Schedule s = new Schedule();
                s.setDoctor(doctor);
                s.setDayOfWeek(day);
                s.setStartTime("09:00");
                s.setEndTime("13:00");
                s.setSlotDurationMinutes(30);
                s.setIsAvailable(true);
                rows.add(s);
            }
            scheduleRepository.saveAll(rows);
            doctorsUpdated++;
        }
        return doctorsUpdated;
    }

    // ───────── Date marks (calendar) ─────────

    public List<DoctorDateMark> getDateMarks(Long doctorId, int year) {
        return dateMarkRepository.findByDoctorIdAndDateBetweenOrderByDate(
                doctorId, LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));
    }

    /**
     * Mark (or re-mark) a date. Any still-open slots on that date are switched off.
     * Returns how many slots on that date are already booked/pending, so the UI can warn the clerk.
     */
    @Transactional
    public MarkResult setDateMark(Long doctorId, LocalDate date, String type, String note) {
        String markType = type == null ? "" : type.trim().toUpperCase();
        if (!markType.equals("REMEMBER") && !markType.equals("UNAVAILABLE")) {
            throw new IllegalArgumentException("Type must be REMEMBER or UNAVAILABLE");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot change a date in the past");
        }
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        DoctorDateMark mark = dateMarkRepository.findByDoctorIdAndDate(doctorId, date)
                .orElseGet(DoctorDateMark::new);
        mark.setDoctor(doctor);
        mark.setDate(date);
        mark.setType(markType);
        String cleanNote = note == null ? null : note.trim();
        mark.setNote(cleanNote == null || cleanNote.isEmpty() ? null : cleanNote);
        mark = dateMarkRepository.save(mark);

        int booked = 0;
        for (Slot slot : slotRepository.findByDoctorIdAndDate(doctorId, date)) {
            if ("AVAILABLE".equals(slot.getStatus())) {
                slot.setStatus("UNAVAILABLE");
                slotRepository.save(slot);
            } else if ("BOOKED".equals(slot.getStatus()) || "PENDING".equals(slot.getStatus())) {
                booked++;
            }
        }
        return new MarkResult(mark, booked);
    }

    /** Remove a mark; slots that were switched off for that date are opened again. */
    @Transactional
    public void clearDateMark(Long doctorId, LocalDate date) {
        dateMarkRepository.findByDoctorIdAndDate(doctorId, date)
                .ifPresent(dateMarkRepository::delete);
        if (date.isBefore(LocalDate.now())) return;
        for (Slot slot : slotRepository.findByDoctorIdAndDate(doctorId, date)) {
            if ("UNAVAILABLE".equals(slot.getStatus())) {
                slot.setStatus("AVAILABLE");
                slotRepository.save(slot);
            }
        }
    }

    public record MarkResult(DoctorDateMark mark, int bookedSlots) {}
}
