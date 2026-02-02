package com.vinicius.jobhunter.service;

import com.vinicius.jobhunter.crawler.Crawler;
import com.vinicius.jobhunter.dto.RawJobDTO;
import com.vinicius.jobhunter.model.Company;
import com.vinicius.jobhunter.model.Job;
import com.vinicius.jobhunter.model.JobSource;
import com.vinicius.jobhunter.repository.CompanyRepository;
import com.vinicius.jobhunter.repository.JobRepository;
import com.vinicius.jobhunter.repository.JobSourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobCollectorService {

    private final List<Crawler> crawlers;
    private final JobRepository jobRepository;
    private final JobSourceRepository sourceRepository;
    private final CompanyRepository companyRepository;
    private final JobFilterService jobFilterService;

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "${job-hunter.collection.cron}")
    public void runCollection() {
        log.info("Starting job collection...");
        for (Crawler crawler : crawlers) {
            try {
                processCrawler(crawler);
            } catch (Exception e) {
                log.error("Error running crawler: {}", crawler.getSourceName(), e);
            }
        }
        log.info("Job collection finished.");
    }

    @Transactional
    public void processCrawler(Crawler crawler) {
        String sourceName = crawler.getSourceName();

        // Ensure source exists - use saveAndFlush to immediately persist
        JobSource source = sourceRepository.findByName(sourceName)
                .orElseGet(() -> {
                    JobSource newSource = new JobSource(sourceName, "AGGREGATOR");
                    return sourceRepository.saveAndFlush(newSource);
                });

        List<RawJobDTO> rawJobs = crawler.scrape();
        log.info("Scraped {} jobs from {}", rawJobs.size(), sourceName);

        // Filter only Junior/Intern Backend Java jobs
        List<RawJobDTO> filteredJobs = jobFilterService.filterJuniorBackendJavaJobs(rawJobs);
        log.info("Filtered to {} Junior/Intern Backend Java jobs (from {} total)",
                filteredJobs.size(), rawJobs.size());

        for (RawJobDTO raw : filteredJobs) {
            try {
                if (jobRepository.existsByUrl(raw.url())) {
                    continue; // Skip duplicates
                }

                // Create or find Company
                Company company = null;
                if (raw.companyName() != null && !raw.companyName().isEmpty()) {
                    company = companyRepository.findByName(raw.companyName())
                            .orElseGet(() -> {
                                Company newCompany = new Company(raw.companyName());
                                return companyRepository.saveAndFlush(newCompany);
                            });
                }

                // Basic parsing / Enrichment placeholder
                Job job = new Job();
                job.setUrl(raw.url());
                job.setTitle(raw.title());
                job.setDescription(raw.description());
                job.setCompany(company);
                job.setSource(source);
                job.setPostedAt(raw.postedAt() != null ? raw.postedAt() : LocalDateTime.now());

                // Save
                jobRepository.save(job);
                log.debug("Saved new job: {}", raw.title());

            } catch (Exception e) {
                log.error("Failed to process job: {}", raw.url(), e);
            }
        }
    }
}
