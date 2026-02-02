package com.vinicius.jobhunter.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "job_sources")
@Data
@NoArgsConstructor
public class JobSource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String name;

    private String baseUrl;

    public JobSource(String name, String baseUrl) {
        this.name = name;
        this.baseUrl = baseUrl;
    }
}
