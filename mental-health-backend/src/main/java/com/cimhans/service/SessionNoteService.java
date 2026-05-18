package com.cimhans.service;

import com.cimhans.domain.entity.*;
import com.cimhans.dto.request.CreateSessionNoteRequest;
import com.cimhans.dto.response.SessionNoteResponse;
import com.cimhans.exception.BusinessException;
import com.cimhans.exception.ResourceNotFoundException;
import com.cimhans.exception.UnauthorizedException;
import com.cimhans.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionNoteService {

    private final SessionNoteRepository sessionNoteRepository;
    private final AppointmentRepository appointmentRepository;
    private final EncryptionService encryptionService;
    private final AuditService auditService;

    @Transactional
    public SessionNoteResponse createNote(UUID appointmentId,
                                          CreateSessionNoteRequest request,
                                          UUID therapistUserId) {
        Appointment appointment = appointmentRepository.findByIdAndDeletedAtIsNull(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", appointmentId));

        // Only the assigned therapist can write the note
        if (!appointment.getTherapist().getUser().getId().equals(therapistUserId)) {
            auditService.logFailure("SESSION_NOTE_CREATE", "SessionNote", appointmentId,
                    "Unauthorized attempt by user: " + therapistUserId);
            throw new UnauthorizedException("You are not the assigned therapist for this appointment");
        }

        if (sessionNoteRepository.existsByAppointmentIdAndDeletedAtIsNull(appointmentId)) {
            throw new BusinessException("A session note already exists for this appointment");
        }

        String encrypted = encryptionService.encrypt(request.getContent());

        SessionNote note = SessionNote.builder()
                .appointment(appointment)
                .patient(appointment.getPatient())
                .therapist(appointment.getTherapist())
                .encryptedContent(encrypted)
                .followUpInstructions(request.getFollowUpInstructions())
                .moodRating(request.getMoodRating())
                .riskLevel(request.getRiskLevel())
                .followUpRequired(request.isFollowUpRequired())
                .followUpDate(request.getFollowUpDate())
                .diagnosisCodes(request.getDiagnosisCodes())
                .treatmentPlan(request.getTreatmentPlan())
                .build();

        note = sessionNoteRepository.save(note);
        auditService.logSuccess("SESSION_NOTE_CREATED", "SessionNote", note.getId(),
                "AppointmentId: " + appointmentId);

        return toResponse(note, request.getContent());
    }

    @Transactional(readOnly = true)
    public SessionNoteResponse getNoteByAppointment(UUID appointmentId, UUID requestingUserId, String role) {
        SessionNote note = sessionNoteRepository.findByAppointmentIdAndDeletedAtIsNull(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("SessionNote", "appointmentId", appointmentId));

        // Access control: only ADMIN or the assigned therapist
        boolean isAdmin = role.contains("ADMIN");
        boolean isAssignedTherapist = note.getTherapist().getUser().getId().equals(requestingUserId);

        if (!isAdmin && !isAssignedTherapist) {
            auditService.logFailure("SESSION_NOTE_READ", "SessionNote", note.getId(),
                    "Unauthorized access by user: " + requestingUserId);
            throw new UnauthorizedException("You are not authorized to view this session note");
        }

        auditService.logSuccess("SESSION_NOTE_READ", "SessionNote", note.getId(),
                "Read by: " + requestingUserId);

        String decrypted = encryptionService.decrypt(note.getEncryptedContent());
        return toResponse(note, decrypted);
    }

    @Transactional(readOnly = true)
    public Page<SessionNoteResponse> getNotesForPatient(UUID patientId, UUID requestingUserId,
                                                         String role, Pageable pageable) {
        boolean isAdmin = role.contains("ADMIN");
        Page<SessionNote> notes = sessionNoteRepository.findByPatientIdAndDeletedAtIsNull(patientId, pageable);

        return notes.map(note -> {
            boolean canAccess = isAdmin || note.getTherapist().getUser().getId().equals(requestingUserId);
            String content = canAccess ? encryptionService.decrypt(note.getEncryptedContent())
                                       : "[CONFIDENTIAL - Access Restricted]";
            return toResponse(note, content);
        });
    }

    @Transactional
    public SessionNoteResponse finalizeNote(UUID noteId, UUID therapistUserId) {
        SessionNote note = sessionNoteRepository.findByIdAndDeletedAtIsNull(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("SessionNote", "id", noteId));

        if (!note.getTherapist().getUser().getId().equals(therapistUserId)) {
            throw new UnauthorizedException("Only the assigned therapist can finalize this note");
        }
        if (note.isFinalized()) {
            throw new BusinessException("Note is already finalized");
        }

        note.setFinalized(true);
        note = sessionNoteRepository.save(note);
        auditService.logSuccess("SESSION_NOTE_FINALIZED", "SessionNote", noteId, "");

        String decrypted = encryptionService.decrypt(note.getEncryptedContent());
        return toResponse(note, decrypted);
    }

    private SessionNoteResponse toResponse(SessionNote n, String decryptedContent) {
        return SessionNoteResponse.builder()
                .id(n.getId().toString())
                .appointmentId(n.getAppointment().getId().toString())
                .patientId(n.getPatient().getId().toString())
                .patientName(n.getPatient().getUser().getFullName())
                .therapistId(n.getTherapist().getId().toString())
                .therapistName(n.getTherapist().getUser().getFullName())
                .content(decryptedContent)
                .followUpInstructions(n.getFollowUpInstructions())
                .moodRating(n.getMoodRating())
                .riskLevel(n.getRiskLevel())
                .followUpRequired(n.isFollowUpRequired())
                .followUpDate(n.getFollowUpDate())
                .diagnosisCodes(n.getDiagnosisCodes())
                .treatmentPlan(n.getTreatmentPlan())
                .isFinalized(n.isFinalized())
                .appointmentDate(n.getAppointment().getAppointmentDate())
                .createdAt(n.getCreatedAt())
                .updatedAt(n.getUpdatedAt())
                .build();
    }
}
