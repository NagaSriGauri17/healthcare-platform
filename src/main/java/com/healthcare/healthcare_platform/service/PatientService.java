package com.healthcare.healthcare_platform.service;

import com.healthcare.healthcare_platform.entity.FamilyMember;
import com.healthcare.healthcare_platform.entity.User;
import com.healthcare.healthcare_platform.repository.FamilyMemberRepository;
import com.healthcare.healthcare_platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final UserRepository userRepository;
    private final FamilyMemberRepository familyMemberRepository;

    public User getProfile(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public User updateProfile(Long userId, String name, String email, String phone) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            if (name != null) user.setName(name);
            if (email != null) user.setEmail(email);
            if (phone != null) user.setPhone(phone);
            return userRepository.save(user);
        }
        return null;
    }

    public FamilyMember addFamilyMember(Long userId, String name, String relationship,
                                        String phone, Integer age) {
        User user = userRepository.findById(userId).orElse(null);
        FamilyMember member = new FamilyMember();
        member.setUser(user);
        member.setName(name);
        member.setRelationship(relationship);
        member.setPhone(phone);
        member.setAge(age);
        return familyMemberRepository.save(member);
    }

    public List<FamilyMember> getFamilyMembers(Long userId) {
        return familyMemberRepository.findByUserId(userId);
    }

    public FamilyMember updateFamilyMember(Long memberId, String name,
                                           String relationship, String phone, Integer age) {
        FamilyMember member = familyMemberRepository.findById(memberId).orElse(null);
        if (member != null) {
            if (name != null) member.setName(name);
            if (relationship != null) member.setRelationship(relationship);
            if (phone != null) member.setPhone(phone);
            if (age != null) member.setAge(age);
            return familyMemberRepository.save(member);
        }
        return null;
    }

    public List<User> searchPatients(String query) {
        if (query == null || query.trim().length() < 2) {
            return List.of();
        }
        return userRepository.findByNameContainingIgnoreCase(query.trim());
    }

    public String deleteFamilyMember(Long memberId) {
        familyMemberRepository.deleteById(memberId);
        return "Family member removed successfully";
    }
}