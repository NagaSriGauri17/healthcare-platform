package com.healthcare.healthcare_platform.service;

import com.healthcare.healthcare_platform.entity.User;
import com.healthcare.healthcare_platform.repository.UserRepository;
import com.healthcare.healthcare_platform.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    private final Map<String, String> otpStorage = new HashMap<>();
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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

    public String sendOtpByEmail(String email) {
        String otp = generateOtp();
        otpStorage.put(email, otp);
        System.out.println("OTP for " + email + " is: " + otp);
        return "OTP sent to email successfully";
    }

    public String sendOtpByPhone(String phone) {
        String otp = generateOtp();
        otpStorage.put(phone, otp);
        System.out.println("OTP for phone " + phone + " is: " + otp);
        return "OTP sent to phone successfully";
    }

    public String verifyOtpByEmail(String email, String otp) {
        return verifyOtp(email, otp);
    }

    public String verifyOtpByPhone(String phone, String otp) {
        return verifyOtp(phone, otp);
    }

    // Set password after OTP verified
    public String setPassword(String identifier, String password) {
        if (password == null || password.length() < 8) {
            return "Password must be at least 8 characters";
        }
        Optional<User> userOpt = identifier.contains("@")
                ? userRepository.findByEmail(identifier)
                : userRepository.findByPhone(identifier);

        if (userOpt.isEmpty()) {
            // Auto-register if user doesn't exist yet
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

    // Login with email/phone + password
    public String login(String identifier, String password) {
        Optional<User> userOpt = identifier.contains("@")
                ? userRepository.findByEmail(identifier)
                : userRepository.findByPhone(identifier);

        if (userOpt.isEmpty()) {
            return "User not found";
        }

        User user = userOpt.get();

        // If no password set yet, tell them to sign up first
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return "Please complete signup first";
        }

        if (passwordEncoder.matches(password, user.getPassword())) {
            return jwtUtil.generateToken(identifier);
        }

        return "Invalid password";
    }

    // Reset password after forgot-password OTP verified
    public String resetPassword(String identifier, String otp, String newPassword) {
        String storedOtp = otpStorage.get(identifier);
        if (storedOtp == null || !storedOtp.equals(otp)) {
            return "Invalid OTP";
        }
        if (newPassword == null || newPassword.length() < 8) {
            return "Password must be at least 8 characters";
        }
        otpStorage.remove(identifier);
        return setPassword(identifier, newPassword);
    }

    private String verifyOtp(String key, String otp) {
        String storedOtp = otpStorage.get(key);
        if (storedOtp != null && storedOtp.equals(otp)) {
            otpStorage.remove(key);
            return jwtUtil.generateToken(key);
        }
        return "Invalid OTP";
    }

    private String generateOtp() {
        return "123456";
    }
}