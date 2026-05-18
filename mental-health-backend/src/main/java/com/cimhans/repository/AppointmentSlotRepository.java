package com.cimhans.repository;

import com.cimhans.domain.entity.AppointmentSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlot, UUID> {

    List<AppointmentSlot> findByTherapistIdAndSlotDateAndIsBookedFalseAndDeletedAtIsNull(
            UUID therapistId, LocalDate slotDate);

    @Query("SELECT s FROM AppointmentSlot s WHERE s.therapist.id = :therapistId " +
           "AND s.slotDate BETWEEN :from AND :to AND s.deletedAt IS NULL " +
           "ORDER BY s.slotDate, s.startTime")
    List<AppointmentSlot> findSlotsByTherapistAndDateRange(@Param("therapistId") UUID therapistId,
                                                            @Param("from") LocalDate from,
                                                            @Param("to") LocalDate to);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM AppointmentSlot s " +
           "WHERE s.therapist.id = :therapistId AND s.slotDate = :date " +
           "AND s.startTime = :startTime AND s.deletedAt IS NULL")
    boolean existsSlotForTherapistAtTime(@Param("therapistId") UUID therapistId,
                                          @Param("date") LocalDate date,
                                          @Param("startTime") LocalTime startTime);
}
