package com.vinicius.jobhunter.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.vinicius.jobhunter.client.GeminiClient;
import com.vinicius.jobhunter.model.Job;
import com.vinicius.jobhunter.model.JobStatus;
import com.vinicius.jobhunter.model.Seniority;
import com.vinicius.jobhunter.model.Modality;
import com.vinicius.jobhunter.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobAnalyzerService {

    private final JobRepository jobRepository;
    private final GeminiClient geminiClient;
    private final Gson gson = new Gson();

    @Scheduled(cron = "0 0/15 * * * *")
    public void analyzeNewJobs() {
        List<Job> newJobs = jobRepository.findAll().stream()
                .filter(j -> j.getStatus() == JobStatus.NEW)
                .toList();

        log.info("Found {} new jobs to analyze", newJobs.size());

        for (Job job : newJobs) {
            enrichJob(job);
        }
    }

    private void enrichJob(Job job) {
        try {
            if (job.getDescription() == null || job.getDescription().length() < 50) {
                job.setStatus(JobStatus.ARCHIVED);
                jobRepository.save(job);
                return;
            }

            String jsonResult = geminiClient.analyzeJob(job.getDescription());
            if (jsonResult != null) {
                JsonObject result = gson.fromJson(jsonResult, JsonObject.class);

                try {
                    String senStr = result.get("seniority").getAsString().toUpperCase();
                    job.setSeniority(Seniority.valueOf(senStr));
                } catch (Exception e) {
                    job.setSeniority(Seniority.UNKNOWN);
                }

                try {
                    String modStr = result.get("modality").getAsString().toUpperCase();
                    job.setModality(Modality.valueOf(modStr));
                } catch (Exception e) {
                    job.setModality(Modality.UNKNOWN);
                }

                try {
                    List<String> stack = new ArrayList<>();
                    result.getAsJsonArray("stack").forEach(e -> stack.add(e.getAsString()));
                    job.setTechStack(stack);
                } catch (Exception e) {
                    // ignore
                }

                job.setScore(calculateScore(job));
                job.setStatus(JobStatus.NEW);
            }

            jobRepository.save(job);

        } catch (Exception e) {
            log.error("Failed to enrich job {}", job.getId(), e);
        }
    }

    private int calculateScore(Job job) {
        int score = 0;
        if (job.getSeniority() == Seniority.INTERN || job.getSeniority() == Seniority.JUNIOR)
            score += 50;
        if (job.getModality() == Modality.REMOTE)
            score += 30;
        if (job.getTechStack() != null) {
            if (job.getTechStack().contains("JAVA") || job.getTechStack().contains("SPRING"))
                score += 20;
            if (job.getTechStack().contains("NODE"))
                score += 20;
        }
        return Math.min(score, 100);
    }
}
