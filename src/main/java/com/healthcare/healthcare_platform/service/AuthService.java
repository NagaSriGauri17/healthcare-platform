package com.healthcare.healthcare_platform.service;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.healthcare.healthcare_platform.entity.User;
import com.healthcare.healthcare_platform.repository.UserRepository;
import com.healthcare.healthcare_platform.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
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
        String otp = "123456";
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setOtpCode(otp);
            userRepository.save(user);
        } else {
            User user = new User();
            user.setName("User");
            user.setEmail(email);
            user.setPhone("");
            user.setPassword("");
            user.setRole("PATIENT");
            user.setOtpCode(otp);
            userRepository.save(user);
        }
        System.out.println("OTP for " + email + " is: " + otp);
        return "OTP sent to email successfully";
    }

    public String sendOtpByPhone(String phone) {
        String otp = "123456";
        Optional<User> userOpt = userRepository.findByPhone(phone);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setOtpCode(otp);
            userRepository.save(user);
        } else {
            User user = new User();
            user.setName("User");
            user.setEmail(phone + "@temp.com");
            user.setPhone(phone);
            user.setPassword("");
            user.setRole("PATIENT");
            user.setOtpCode(otp);
            userRepository.save(user);
        }
        System.out.println("OTP for phone " + phone + " is: " + otp);
        return "OTP sent to phone successfully";
    }

    public String verifyOtpByPhone(String phone, String otp) {
        Optional<User> userOpt = userRepository.findByPhone(phone);
        if (userOpt.isEmpty()) return "User not found";
        User user = userOpt.get();
        if (otp.equals(user.getOtpCode())) {
            user.setOtpCode(null);
            userRepository.save(user);
            return jwtUtil.generateToken(phone);
        }
        return "Invalid OTP";
    }

    public String verifyOtpByEmail(String email, String otp) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return "User not found";
        User user = userOpt.get();
        if (otp.equals(user.getOtpCode())) {
            user.setOtpCode(null);
            userRepository.save(user);
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