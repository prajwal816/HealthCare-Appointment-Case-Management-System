package com.cimhans.repository;

import com.cimhans.domain.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    Optional<Patient> findByMrnAndDeletedAtIsNull(String mrn);

    Optional<Patient> findByUserIdAndDeletedAtIsNull(UUID userId);

    boolean existsByMrnAndDeletedAtIsNull(String mrn);

    @Query("SELECT p FROM Patient p WHERE p.deletedAt IS NULL AND " +
           "(LOWER(p.user.firstName) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(p.user.lastName) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(p.user.email) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(p.mrn) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(p.user.phone) LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<Patient> searchPatients(@Param("q") String query, Pageable pageable);

    @Query("SELECT p FROM Patient p WHERE p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    Page<Patient> findAllActivePatients(Pageable pageable);

    @Query("SELECT COUNT(p) FROM Patient p WHERE p.deletedAt IS NULL AND p.isActiveCase = true")
    long countActivePatients();

    @Query("SELECT p FROM Patient p JOIN p.appointments a WHERE a.therapist.id = :therapistId " +
           "AND p.deletedAt IS NULL GROUP BY p.id")
    Page<Patient> findPatientsByTherapistId(@Param("therapistId") UUID therapistId, Pageable pageable);
}
