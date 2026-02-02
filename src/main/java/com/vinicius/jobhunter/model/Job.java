package com.vinicius.jobhunter.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String externalId;

    @Column(unique = true, nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(nullable = false)
    private String title;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Normalized
    @Enumerated(EnumType.STRING)
    private Seniority seniority;

    @Enumerated(EnumType.STRING)
    private Modality modality;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "tech_stack", columnDefinition = "text[]")
    private List<String> techStack;

    // Metadata
    private LocalDateTime postedAt;

    private LocalDateTime collectedAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "source_id")
    private JobSource source;

    // Logic
    @Column(length = 64)
    private String rawTextHash;

    private Integer score = 0;

    @Enumerated(EnumType.STRING)
    private JobStatus status = JobStatus.NEW;

}
