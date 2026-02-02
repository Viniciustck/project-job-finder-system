package com.vinicius.jobhunter.service;

import com.vinicius.jobhunter.dto.RawJobDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class JobFilterService {

        private static final List<String> JUNIOR_KEYWORDS = Arrays.asList(
                        "junior", "jr", "jr.", "júnior",
                        "intern", "internship", "estagio", "estágio",
                        "trainee", "entry level", "entry-level",
                        "graduate", "recém formado", "recém-formado");

        private static final List<String> SENIOR_KEYWORDS = Arrays.asList(
                        "senior", "sr", "sr.", "sênior",
                        "pleno", "pl", "pl.",
                        "lead", "tech lead", "technical lead",
                        "principal", "staff", "architect",
                        "manager", "head", "director",
                        "expert", "specialist");

        private static final List<String> BACKEND_JAVA_KEYWORDS = Arrays.asList(
                        "java", "spring", "spring boot", "springboot",
                        "backend", "back-end", "back end",
                        "api", "rest", "microservice", "microservices");

        public boolean isJuniorBackendJavaJob(RawJobDTO job) {
                String title = job.title() != null ? job.title().toLowerCase() : "";
                String description = job.description() != null ? job.description().toLowerCase() : "";
                String combined = title + " " + description;

                boolean isBackendJava = BACKEND_JAVA_KEYWORDS.stream()
                                .anyMatch(combined::contains);

                if (!isBackendJava) {
                        return false;
                }

                boolean isSenior = SENIOR_KEYWORDS.stream()
                                .anyMatch(title::contains);

                if (isSenior) {
                        return false;
                }

                boolean isJunior = JUNIOR_KEYWORDS.stream()
                                .anyMatch(title::contains);

                boolean hasLevelMention = JUNIOR_KEYWORDS.stream().anyMatch(title::contains) ||
                                SENIOR_KEYWORDS.stream().anyMatch(title::contains);

                return isJunior || !hasLevelMention;
        }

        public List<RawJobDTO> filterJuniorBackendJavaJobs(List<RawJobDTO> jobs) {
                return jobs.stream()
                                .filter(this::isJuniorBackendJavaJob)
                                .toList();
        }
}
