package com.vinicius.jobhunter.repository;

import com.vinicius.jobhunter.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {
    boolean existsByUrl(String url);

    boolean existsByRawTextHash(String rawTextHash);

    Optional<Job> findByUrl(String url);
}
