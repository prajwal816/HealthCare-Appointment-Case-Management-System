package com.cimhans.config;

import com.cimhans.domain.entity.Therapist;
import com.cimhans.domain.entity.User;
import com.cimhans.domain.enums.Role;
import com.cimhans.domain.enums.TherapistSpecialization;
import com.cimhans.repository.TherapistRepository;
import com.cimhans.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final TherapistRepository therapistRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedAdmin();
        seedReceptionist();
        seedTherapist();
        log.info("✅ Data seeding complete.");
    }

    private void seedAdmin() {
        if (userRepository.existsByEmailAndDeletedAtIsNull("admin@cimhans.com")) return;
        User admin = User.builder()
                .email("admin@cimhans.com")
                .firstName("System")
                .lastName("Admin")
                .role(Role.ADMIN)
                .passwordHash(passwordEncoder.encode("Admin@123"))
                .isActive(true)
                .build();
        userRepository.save(admin);
        log.info("✅ Admin user seeded: admin@cimhans.com / Admin@123");
    }

    private void seedReceptionist() {
        if (userRepository.existsByEmailAndDeletedAtIsNull("reception@cimhans.com")) return;
        User receptionist = User.builder()
                .email("reception@cimhans.com")
                .firstName("Front")
                .lastName("Desk")
                .role(Role.RECEPTIONIST)
                .passwordHash(passwordEncoder.encode("Reception@123"))
                .isActive(true)
                .build();
        userRepository.save(receptionist);
        log.info("✅ Receptionist seeded: reception@cimhans.com / Reception@123");
    }

    private void seedTherapist() {
        if (userRepository.existsByEmailAndDeletedAtIsNull("doctor@cimhans.com")) return;
        User therapistUser = User.builder()
                .email("doctor@cimhans.com")
                .firstName("Dr. Arjun")
                .lastName("Sharma")
                .role(Role.PSYCHIATRIST)
                .passwordHash(passwordEncoder.encode("Doctor@123"))
                .isActive(true)
                .build();
        // saveAndFlush ensures the User has a DB-assigned ID before Therapist references it
        therapistUser = userRepository.saveAndFlush(therapistUser);

        Therapist therapist = Therapist.builder()
                .user(therapistUser)
                .specialization(TherapistSpecialization.PSYCHIATRY)
                .licenseNumber("PSY-2024-001")
                .qualification("MBBS, MD Psychiatry")
                .consultationFeeInr(500.0)
                .maxPatientsPerDay(8)
                .isAvailable(true)
                .build();
        therapistRepository.save(therapist);
        log.info("✅ Therapist seeded: doctor@cimhans.com / Doctor@123");
    }
}
