package com.healthcare.healthcare_platform.service;

import com.healthcare.healthcare_platform.entity.Appointment;
import com.healthcare.healthcare_platform.entity.QueueToken;
import com.healthcare.healthcare_platform.entity.Slot;
import com.healthcare.healthcare_platform.entity.User;
import com.healthcare.healthcare_platform.repository.AppointmentRepository;
import com.healthcare.healthcare_platform.repository.QueueTokenRepository;
import com.healthcare.healthcare_platform.repository.SlotRepository;
import com.healthcare.healthcare_platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final AppointmentRepository appointmentRepository;
    private final QueueTokenRepository queueTokenRepository;
    private final SlotRepository slotRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final FirebaseService firebaseService;

    // ─── WALK-IN BOOKING ───────────────────────────────────────────────────────

    @Transactional
    public Appointment walkInBooking(Long doctorId, Long slotId,
                                     String patientName, String patientPhone) {
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        if (!slot.getStatus().equals("AVAILABLE")) {
            throw new RuntimeException("Slot not available");
        }

        User user = userRepository.findByPhone(patientPhone).orElseGet(() -> {
            User newUser = new User();
            newUser.setName(patientName);
            newUser.setPhone(patientPhone);
            newUser.setEmail(patientPhone + "@walkin.com");
            newUser.setPassword("");
            newUser.setRole("PATIENT");
            return userRepository.save(newUser);
        });

        slot.setStatus("BOOKED");
        slotRepository.save(slot);

        Appointment appointment = new Appointment();
        appointment.setUser(user);
        appointment.setDoctor(slot.getDoctor());
        appointment.setSlot(slot);
        appointment.setStatus("CONFIRMED");
        appointment.setPaymentStatus("PENDING");
        appointment.setAmount(slot.getDoctor().getConsultationFee());
        appointment.setOrderId("WALKIN_" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setType("WALK_IN");
        Appointment saved = appointmentRepository.save(appointment);

        // Broadcast slot update to all patients viewing this doctor's slots
        Map<String, Object> slotUpdate = new HashMap<>();
        slotUpdate.put("slotId", slotId);
        slotUpdate.put("status", "BOOKED");
        slotUpdate.put("type", "WALK_IN");
        messagingTemplate.convertAndSend("/topic/slots/" + doctorId, slotUpdate);

        return saved;
    }

    // ─── CHECK-IN — THIS IS WHERE THE TOKEN IS BORN ────────────────────────────

    @Transactional
    public Map<String, Object> checkIn(Long appointmentId) {
        // 1. Find the appointment
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        Long doctorId = appointment.getDoctor().getId();
        String today = LocalDate.now().toString();
        String redisKey = "queue:doctor:" + doctorId + ":" + today;

        // Prevent double check-in
        if ("CHECKED_IN".equals(appointment.getStatus())) {
            throw new RuntimeException("Patient already checked in");
        }

        // 2. Assign the next token number from Redis
        Long tokenNumber = redisTemplate.opsForValue().increment(redisKey + ":maxToken");
        if (tokenNumber == null) tokenNumber = 1L;

        // 3. Add token to the waiting list in Redis
        redisTemplate.opsForList().rightPush(redisKey + ":waiting", tokenNumber.toString());

        // 4. Save QueueToken row in PostgreSQL (permanent record)
        QueueToken token = new QueueToken();
        token.setAppointment(appointment);
        token.setDoctorId(doctorId);
        token.setTokenNumber(tokenNumber.intValue());
        token.setStatus("WAITING");
        token.setCheckedInAt(LocalDateTime.now());
        queueTokenRepository.save(token);

        // 5. Update appointment status to CHECKED_IN
        appointment.setStatus("CHECKED_IN");
        appointmentRepository.save(appointment);

        // 6. Calculate queue position
        List<Object> waitingList = redisTemplate.opsForList().range(redisKey + ":waiting", 0, -1);
        int position = waitingList != null ? waitingList.size() : 1;
        int patientsAhead = position - 1;
        int estimatedMinutes = patientsAhead * 15;

        // 7. Broadcast queue update to ALL patients watching this doctor
        Map<String, Object> queueUpdate = new HashMap<>();
        queueUpdate.put("event", "NEW_CHECKIN");
        queueUpdate.put("tokenNumber", tokenNumber);
        queueUpdate.put("totalWaiting", position);
        queueUpdate.put("estimatedWaitMinutes", estimatedMinutes);
        messagingTemplate.convertAndSend("/topic/queue/" + doctorId, queueUpdate);

        // 8. Return response to the staff who clicked check-in
        Map<String, Object> response = new HashMap<>();
        response.put("tokenNumber", tokenNumber);
        response.put("patientsAhead", patientsAhead);
        response.put("estimatedWaitMinutes", estimatedMinutes);
        response.put("appointmentId", appointmentId);
        response.put("patientName", appointment.getUser().getName());
        return response;
    }

    // ─── GET LIVE QUEUE STATUS ──────────────────────────────────────────────────

    public Map<String, Object> getQueueStatus(Long doctorId) {
        String today = LocalDate.now().toString();
        String redisKey = "queue:doctor:" + doctorId + ":" + today;  // ADD :today here

        Object currentTokenObj = redisTemplate.opsForValue().get(redisKey + ":current");
        List<Object> waitingList = redisTemplate.opsForList().range(redisKey + ":waiting", 0, -1);

        int currentToken = currentTokenObj != null ? Integer.parseInt(currentTokenObj.toString()) : 0;
        int waitingCount = waitingList != null ? waitingList.size() : 0;

        Map<String, Object> status = new HashMap<>();
        status.put("doctorId", doctorId);
        status.put("currentToken", currentToken);
        status.put("waitingCount", waitingCount);
        status.put("estimatedWaitMinutes", waitingCount * 15);
        status.put("waitingTokens", waitingList);
        return status;
    }

    public Map<String, Object> getQueueBoard(Long doctorId) {
        LocalDate today = LocalDate.now();
        List<QueueToken> allTokens = queueTokenRepository.findAll();

        List<Map<String, Object>> inProgress = new ArrayList<>();
        List<Map<String, Object>> waiting = new ArrayList<>();
        List<Map<String, Object>> completed = new ArrayList<>();

        for (QueueToken t : allTokens) {
            if (t.getDoctorId() == null || !t.getDoctorId().equals(doctorId)) continue;
            if (t.getCheckedInAt() == null || !t.getCheckedInAt().toLocalDate().equals(today)) continue;

            Map<String, Object> info = new HashMap<>();
            info.put("tokenNumber", t.getTokenNumber());
            info.put("status", t.getStatus());
            if (t.getAppointment() != null) {
                info.put("appointmentId", t.getAppointment().getId());
                if (t.getAppointment().getUser() != null) {
                    info.put("patientName", t.getAppointment().getUser().getName());
                    info.put("patientPhone", t.getAppointment().getUser().getPhone());
                }
            }

            if ("IN_PROGRESS".equals(t.getStatus())) inProgress.add(info);
            else if ("WAITING".equals(t.getStatus())) waiting.add(info);
            else if ("COMPLETED".equals(t.getStatus())) completed.add(info);
        }

        Map<String, Object> board = new HashMap<>();
        board.put("inProgress", inProgress);
        board.put("waiting", waiting);
        board.put("completed", completed);
        return board;
    }

    // ─── ADVANCE QUEUE — STAFF CLICKS "NEXT" ───────────────────────────────────

    @Transactional
    public Map<String, Object> advanceQueue(Long doctorId) {
        String today = LocalDate.now().toString();
        String redisKey = "queue:doctor:" + doctorId + ":" + today;

        // Remove the first token from the waiting list (the one just finished)
        Object finishedToken = redisTemplate.opsForList().leftPop(redisKey + ":waiting");

        // Mark that token as COMPLETED in PostgreSQL
        if (finishedToken != null) {
            int tokenNum = Integer.parseInt(finishedToken.toString());
            queueTokenRepository
                    .findFirstByDoctorIdAndStatusOrderByTokenNumberAsc(doctorId, "IN_PROGRESS")
                    .ifPresent(qt -> {
                        qt.setStatus("COMPLETED");
                        qt.setCompletedAt(LocalDateTime.now());
                        queueTokenRepository.save(qt);
                    });
        }

        // Get next token in line
        List<Object> remainingList = redisTemplate.opsForList().range(redisKey + ":waiting", 0, -1);
        Object nextTokenObj = remainingList != null && !remainingList.isEmpty() ? remainingList.get(0) : null;

        if (nextTokenObj != null) {
            // Set next token as current
            redisTemplate.opsForValue().set(redisKey + ":current", nextTokenObj.toString());

            // Mark as IN_PROGRESS in PostgreSQL
            int nextTokenNum = Integer.parseInt(nextTokenObj.toString());
            queueTokenRepository
                    .findFirstByDoctorIdAndStatusOrderByTokenNumberAsc(doctorId, "WAITING")
                    .ifPresent(qt -> {
                        if (qt.getTokenNumber() == nextTokenNum) {
                            qt.setStatus("IN_PROGRESS");
                            queueTokenRepository.save(qt);
                        }
                    });

            // Check if anyone is 2 tokens away — fire "your turn soon" alert
            if (remainingList.size() >= 2) {
                int alertToken = Integer.parseInt(remainingList.get(1).toString());
                sendNearbyAlert(doctorId, alertToken);
            }
        }

        int waitingCount = remainingList != null ? remainingList.size() : 0;

        // Broadcast to all patients
        Map<String, Object> update = new HashMap<>();
        update.put("event", "QUEUE_ADVANCED");
        update.put("currentToken", nextTokenObj != null ? nextTokenObj : 0);
        update.put("waitingCount", waitingCount);
        update.put("estimatedWaitMinutes", waitingCount * 15);
        messagingTemplate.convertAndSend("/topic/queue/" + doctorId, update);

        return update;
    }

    // ─── SKIP TOKEN ─────────────────────────────────────────────────────────────

    public Map<String, Object> skipToken(Long doctorId) {
        String today = LocalDate.now().toString();
        String redisKey = "queue:doctor:" + doctorId + ":" + today;

        // Move first token to the back of the waiting list
        Object skippedToken = redisTemplate.opsForList().leftPop(redisKey + ":waiting");
        if (skippedToken != null) {
            redisTemplate.opsForList().rightPush(redisKey + ":waiting", skippedToken);
        }

        List<Object> waitingList = redisTemplate.opsForList().range(redisKey + ":waiting", 0, -1);
        Object nextToken = waitingList != null && !waitingList.isEmpty() ? waitingList.get(0) : null;

        Map<String, Object> update = new HashMap<>();
        update.put("event", "TOKEN_SKIPPED");
        update.put("skippedToken", skippedToken);
        update.put("currentToken", nextToken);
        update.put("waitingCount", waitingList != null ? waitingList.size() : 0);
        messagingTemplate.convertAndSend("/topic/queue/" + doctorId, update);

        return update;
    }

    public Map<String, Object> holdToken(Long doctorId, Integer tokenNumber) {
        String today = LocalDate.now().toString();
        String redisKey = "queue:doctor:" + doctorId + ":" + today;

        redisTemplate.opsForValue().set(redisKey + ":hold:" + tokenNumber, "true");

        queueTokenRepository
                .findFirstByDoctorIdAndStatusOrderByTokenNumberAsc(doctorId, "WAITING")
                .ifPresent(qt -> {
                    if (qt.getTokenNumber().equals(tokenNumber)) {
                        qt.setStatus("ON_HOLD");
                        queueTokenRepository.save(qt);
                    }
                });

        Map<String, Object> update = new HashMap<>();
        update.put("event", "TOKEN_ON_HOLD");
        update.put("heldToken", tokenNumber);
        messagingTemplate.convertAndSend("/topic/queue/" + doctorId, update);
        return update;
    }

    public Map<String, Object> resumeToken(Long doctorId, Integer tokenNumber) {
        String today = LocalDate.now().toString();
        String redisKey = "queue:doctor:" + doctorId + ":" + today;

        redisTemplate.delete(redisKey + ":hold:" + tokenNumber);

        queueTokenRepository
                .findFirstByDoctorIdAndStatusOrderByTokenNumberAsc(doctorId, "ON_HOLD")
                .ifPresent(qt -> {
                    if (qt.getTokenNumber().equals(tokenNumber)) {
                        qt.setStatus("WAITING");
                        queueTokenRepository.save(qt);
                    }
                });

        Map<String, Object> update = new HashMap<>();
        update.put("event", "TOKEN_RESUMED");
        update.put("resumedToken", tokenNumber);
        messagingTemplate.convertAndSend("/topic/queue/" + doctorId, update);
        return update;
    }
    // ─── GET PATIENT'S TOKEN STATUS ─────────────────────────────────────────────

    public Map<String, Object> getPatientTokenStatus(Long appointmentId) {
        String today = LocalDate.now().toString();

        QueueToken token = queueTokenRepository.findFirstByAppointmentIdOrderByIdDesc(appointmentId)
                .orElseThrow(() -> new RuntimeException("Token not found — patient not checked in yet"));

        Long doctorId = token.getDoctorId();
        String redisKey = "queue:doctor:" + doctorId + ":" + today;

        Object currentTokenObj = redisTemplate.opsForValue().get(redisKey + ":current");
        List<Object> waitingList = redisTemplate.opsForList().range(redisKey + ":waiting", 0, -1);

        int currentToken = currentTokenObj != null ? Integer.parseInt(currentTokenObj.toString()) : 0;
        int myToken = token.getTokenNumber();

        // Calculate how many are ahead of this patient
        int patientsAhead = 0;
        if (waitingList != null) {
            for (Object t : waitingList) {
                int tNum = Integer.parseInt(t.toString());
                if (tNum < myToken) patientsAhead++;
                else break;
            }
        }

        Map<String, Object> status = new HashMap<>();
        status.put("myToken", myToken);
        status.put("currentToken", currentToken);
        status.put("patientsAhead", patientsAhead);
        status.put("estimatedWaitMinutes", patientsAhead * 15);
        status.put("tokenStatus", token.getStatus());
        return status;
    }

    // ─── PRIVATE: SEND "YOUR TURN SOON" ALERT ───────────────────────────────────

    private void sendNearbyAlert(Long doctorId, int tokenNumber) {
        // Find the appointment for this token and send FCM push
        queueTokenRepository
                .findFirstByDoctorIdAndStatusOrderByTokenNumberAsc(doctorId, "WAITING")
                .ifPresent(qt -> {
                    if (qt.getTokenNumber() == tokenNumber) {
                        String fcmToken = qt.getAppointment().getUser().getFcmToken();
                        if (fcmToken != null && !fcmToken.isEmpty()) {
                            firebaseService.sendPush(fcmToken,
                                    "Almost your turn!",
                                    "2 patients ahead of you. Please head to the hospital.");
                        }
                    }
                });
    }
    public List<Map<String, Object>> getPendingCheckins(Long doctorId) {
        LocalDate today = LocalDate.now();
        List<Appointment> appointments = appointmentRepository.findByDoctorId(doctorId);
        List<Map<String, Object>> pending = new ArrayList<>();

        for (Appointment apt : appointments) {
            boolean isToday = apt.getSlot() != null && apt.getSlot().getDate() != null
                    && apt.getSlot().getDate().equals(today);
            boolean notYetCheckedIn = "CONFIRMED".equals(apt.getStatus());

            if (isToday && notYetCheckedIn) {
                Map<String, Object> info = new HashMap<>();
                info.put("appointmentId", apt.getId());
                info.put("patientName", apt.getUser() != null ? apt.getUser().getName() : "Patient");
                info.put("patientPhone", apt.getUser() != null ? apt.getUser().getPhone() : "");
                info.put("slotTime", apt.getSlot() != null ? apt.getSlot().getStartTime() : "");
                pending.add(info);
            }
        }
        return pending;
    }
}