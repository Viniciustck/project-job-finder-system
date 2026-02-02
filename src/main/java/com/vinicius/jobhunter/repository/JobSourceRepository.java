package com.vinicius.jobhunter.repository;

import com.vinicius.jobhunter.model.JobSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobSourceRepository extends JpaRepository<JobSource, Integer> {
    Optional<JobSource> findByName(String name);
}
