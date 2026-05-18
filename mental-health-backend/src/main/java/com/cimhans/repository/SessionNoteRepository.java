package com.cimhans.repository;

import com.cimhans.domain.entity.SessionNote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionNoteRepository extends JpaRepository<SessionNote, UUID> {

    Optional<SessionNote> findByAppointmentIdAndDeletedAtIsNull(UUID appointmentId);

    Optional<SessionNote> findByIdAndDeletedAtIsNull(UUID id);

    Page<SessionNote> findByPatientIdAndDeletedAtIsNull(UUID patientId, Pageable pageable);

    Page<SessionNote> findByTherapistIdAndDeletedAtIsNull(UUID therapistId, Pageable pageable);

    @Query("SELECT s FROM SessionNote s WHERE s.patient.id = :patientId " +
           "AND s.therapist.id = :therapistId AND s.deletedAt IS NULL ORDER BY s.createdAt DESC")
    Page<SessionNote> findByPatientAndTherapist(@Param("patientId") UUID patientId,
                                                @Param("therapistId") UUID therapistId,
                                                Pageable pageable);

    @Query("SELECT s FROM SessionNote s WHERE s.followUpRequired = true " +
           "AND s.followUpDate <= CURRENT_DATE AND s.deletedAt IS NULL")
    Page<SessionNote> findPendingFollowUps(Pageable pageable);

    boolean existsByAppointmentIdAndDeletedAtIsNull(UUID appointmentId);
}
