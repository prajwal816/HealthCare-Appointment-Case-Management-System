package com.cimhans.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "documents", indexes = {
    @Index(name = "idx_doc_patient", columnList = "patient_id"),
    @Index(name = "idx_doc_uploaded_by", columnList = "uploaded_by_user_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Document extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "uploaded_by_user_id", columnDefinition = "uuid", nullable = false)
    private UUID uploadedByUserId;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_path", nullable = false, length = 1000)
    private String filePath;

    @Column(name = "file_type", length = 10)
    private String fileType;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "document_category", length = 50)
    private String documentCategory;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "appointment_id", columnDefinition = "uuid")
    private UUID appointmentId;
}
