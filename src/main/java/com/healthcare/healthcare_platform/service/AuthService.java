package com.healthcare.healthcare_platform.service;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.healthcare.healthcare_platform.entity.User;
import com.healthcare.healthcare_platform.repository.UserRepository;
import com.healthcare.healthcare_platform.util.JwtUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PersistenceContext
    private EntityManager entityManager;

    public String register(String name, String email, String phone) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword("");
        user.setRole("PATIENT");
        userRepository.save(user);
        return "User registered successfully";
    }

    public String sendOtpByPhone(String phone) {
    String otp = "123456";

    // Check if user with this phone already exists
    Long count = ((Number) entityManager.createNativeQuery(
                    "SELECT COUNT(*) FROM users WHERE phone = :phone")
            .setParameter("phone", phone)
            .getSingleResult()).longValue();

    if (count == 0) {
        // Create a new user with placeholder values (phone-only login)
        entityManager.createNativeQuery(
                        "INSERT INTO users (name, email, password, phone, role, otp_code, created_at) " +
                        "VALUES (:name, :email, :password, :phone, :role, :otp, :createdAt)")
                .setParameter("name", "Patient_" + phone)
                .setParameter("email", phone + "@placeholder.healthcare.app")
                .setParameter("password", "PHONE_LOGIN_NO_PASSWORD")
                .setParameter("phone", phone)
                .setParameter("role", "PATIENT")
                .setParameter("otp", otp)
                .setParameter("createdAt", java.time.LocalDateTime.now())
                .executeUpdate();
        System.out.println("Created new user for phone: " + phone);
    } else {
        entityManager.createNativeQuery(
                        "UPDATE users SET otp_code = :otp WHERE phone = :phone")
                .setParameter("otp", otp)
                .setParameter("phone", phone)
                .executeUpdate();
        System.out.println("Updated OTP for existing phone: " + phone);
    }

    System.out.println("OTP for phone " + phone + " is: " + otp);
    return "OTP sent to phone successfully";
}

    public String sendOtpByEmail(String email) {
        String otp = "123456";
        int updated = entityManager.createNativeQuery(
                        "UPDATE users SET otp_code = :otp WHERE email = :email")
                .setParameter("otp", otp)
                .setParameter("email", email)
                .executeUpdate();
        System.out.println("Updated rows: " + updated + " | OTP for email " + email + " is: " + otp);
        return "OTP sent to email successfully";
    }

    public String verifyOtpByPhone(String phone, String otp) {
    java.util.List<?> results = entityManager.createNativeQuery(
                    "SELECT otp_code FROM users WHERE phone = :phone")
            .setParameter("phone", phone)
            .getResultList();

    if (results.isEmpty()) {
        return "Phone not found. Please request OTP first.";
    }

    Object result = results.get(0);
    boolean match = result != null && result.toString().trim().equals(otp.trim());
    System.out.println("Match: " + match + " | Stored: [" + result + "] | Entered: [" + otp + "]");

    if (match) {
        String token = jwtUtil.generateToken(phone);
        System.out.println("Token generated: " + token.substring(0, 20));
        return token;
    }
    return "Invalid OTP";
}

    public String verifyOtpByEmail(String email, String otp) {
        Object result = entityManager.createNativeQuery(
                        "SELECT otp_code FROM users WHERE email = :email")
                .setParameter("email", email)
                .getSingleResult();
        System.out.println("Stored OTP: " + result + " | Entered OTP: " + otp);
        if (result != null && otp.equals(result.toString())) {
            entityManager.createNativeQuery(
                            "UPDATE users SET otp_code = NULL WHERE email = :email")
                    .setParameter("email", email)
                    .executeUpdate();
            return jwtUtil.generateToken(email);
        }
        return "Invalid OTP";
    }

    public String setPassword(String identifier, String password) {
        if (password == null || password.length() < 8) {
            return "Password must be at least 8 characters";
        }
        Optional<User> userOpt = identifier.contains("@")
                ? userRepository.findByEmail(identifier)
                : userRepository.findByPhone(identifier);

        if (userOpt.isEmpty()) {
            User user = new User();
            user.setName("Staff");
            user.setEmail(identifier.contains("@") ? identifier : null);
            user.setPhone(identifier.contains("@") ? null : identifier);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole("HOSPITAL_ADMIN");
            userRepository.save(user);
            return jwtUtil.generateToken(identifier);
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        return jwtUtil.generateToken(identifier);
    }

    public String login(String identifier, String password) {
        Optional<User> userOpt = identifier.contains("@")
                ? userRepository.findByEmail(identifier)
                : userRepository.findByPhone(identifier);

        if (userOpt.isEmpty()) return "User not found";
        User user = userOpt.get();

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return "Please complete signup first";
        }

        if (passwordEncoder.matches(password, user.getPassword())) {
            return jwtUtil.generateToken(identifier);
        }
        return "Invalid password";
    }

    public String resetPassword(String identifier, String otp, String newPassword) {
        Optional<User> userOpt = identifier.contains("@")
                ? userRepository.findByEmail(identifier)
                : userRepository.findByPhone(identifier);

        if (userOpt.isEmpty()) return "User not found";
        User user = userOpt.get();

        if (!otp.equals(user.getOtpCode())) return "Invalid OTP";
        if (newPassword == null || newPassword.length() < 8) {
            return "Password must be at least 8 characters";
        }

        user.setOtpCode(null);
        userRepository.save(user);
        return setPassword(identifier, newPassword);
    }
}