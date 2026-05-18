package com.cimhans.repository;

import com.cimhans.domain.entity.Therapist;
import com.cimhans.domain.enums.TherapistSpecialization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TherapistRepository extends JpaRepository<Therapist, UUID> {

    Optional<Therapist> findByUserIdAndDeletedAtIsNull(UUID userId);

    Optional<Therapist> findByLicenseNumberAndDeletedAtIsNull(String licenseNumber);

    boolean existsByLicenseNumberAndDeletedAtIsNull(String licenseNumber);

    @Query("SELECT t FROM Therapist t WHERE t.deletedAt IS NULL AND t.isAvailable = true")
    List<Therapist> findAllAvailableTherapists();

    @Query("SELECT t FROM Therapist t WHERE t.deletedAt IS NULL AND t.specialization = :spec AND t.isAvailable = true")
    List<Therapist> findBySpecializationAndAvailable(@Param("spec") TherapistSpecialization spec);

    @Query("SELECT t FROM Therapist t WHERE t.deletedAt IS NULL ORDER BY t.user.firstName")
    Page<Therapist> findAllActive(Pageable pageable);

    /**
     * Finds therapists who have at least one open (not booked) slot on the given date.
     */
    @Query("SELECT DISTINCT t FROM Therapist t JOIN t.slots s WHERE t.deletedAt IS NULL " +
           "AND s.slotDate = :date AND s.isBooked = false AND t.isAvailable = true")
    List<Therapist> findAvailableOnDate(@Param("date") LocalDate date);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.therapist.id = :therapistId " +
           "AND a.appointmentDate = :date AND a.status NOT IN ('CANCELLED', 'NO_SHOW')")
    long countBookedAppointmentsOnDate(@Param("therapistId") UUID therapistId,
                                       @Param("date") LocalDate date);

    // Used by AnalyticsService
    long countByDeletedAtIsNull();

    // Used by TherapistController — alias for findAllAvailableTherapists
    @Query("SELECT t FROM Therapist t WHERE t.deletedAt IS NULL AND t.isAvailable = true")
    List<Therapist> findAvailableTherapists();
}
