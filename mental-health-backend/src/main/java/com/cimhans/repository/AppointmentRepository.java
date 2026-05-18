package com.cimhans.repository;

import com.cimhans.domain.entity.Appointment;
import com.cimhans.domain.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    Optional<Appointment> findByIdAndDeletedAtIsNull(UUID id);

    Page<Appointment> findByPatientIdAndDeletedAtIsNull(UUID patientId, Pageable pageable);

    Page<Appointment> findByTherapistIdAndDeletedAtIsNull(UUID therapistId, Pageable pageable);

    @Query("SELECT a FROM Appointment a WHERE a.therapist.id = :therapistId " +
           "AND a.appointmentDate = :date AND a.deletedAt IS NULL " +
           "AND a.status NOT IN ('CANCELLED', 'NO_SHOW') ORDER BY a.startTime")
    List<Appointment> findTherapistScheduleForDate(@Param("therapistId") UUID therapistId,
                                                    @Param("date") LocalDate date);

    @Query("SELECT a FROM Appointment a WHERE a.therapist.id = :therapistId " +
           "AND a.appointmentDate BETWEEN :from AND :to AND a.deletedAt IS NULL " +
           "AND a.status NOT IN ('CANCELLED', 'NO_SHOW') ORDER BY a.appointmentDate, a.startTime")
    List<Appointment> findTherapistAppointmentsInRange(@Param("therapistId") UUID therapistId,
                                                        @Param("from") LocalDate from,
                                                        @Param("to") LocalDate to);

    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId " +
           "AND a.appointmentDate >= :from AND a.deletedAt IS NULL ORDER BY a.appointmentDate DESC")
    Page<Appointment> findUpcomingForPatient(@Param("patientId") UUID patientId,
                                             @Param("from") LocalDate from, Pageable pageable);

    /** Used by reminder scheduler — finds confirmed appointments 24 hours ahead that haven't been notified */
    @Query("SELECT a FROM Appointment a WHERE a.status = 'CONFIRMED' AND a.isReminderSent = false " +
           "AND a.deletedAt IS NULL AND a.appointmentDate = :tomorrow")
    List<Appointment> findAppointmentsForReminder(@Param("tomorrow") LocalDate tomorrow);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.deletedAt IS NULL AND a.status = :status")
    long countByStatus(@Param("status") AppointmentStatus status);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.deletedAt IS NULL " +
           "AND a.createdAt BETWEEN :from AND :to")
    long countCreatedBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT a.status, COUNT(a) FROM Appointment a WHERE a.deletedAt IS NULL " +
           "AND a.appointmentDate BETWEEN :from AND :to GROUP BY a.status")
    List<Object[]> getStatusBreakdownForPeriod(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
