package com.vinicius.jobhunter;

import com.vinicius.jobhunter.client.GeminiClient;
import com.vinicius.jobhunter.crawler.Crawler;
import com.vinicius.jobhunter.dto.RawJobDTO;
import com.vinicius.jobhunter.model.Job;
import com.vinicius.jobhunter.model.JobStatus;
import com.vinicius.jobhunter.model.Modality;
import com.vinicius.jobhunter.model.Seniority;
import com.vinicius.jobhunter.repository.JobRepository;
import com.vinicius.jobhunter.repository.JobSourceRepository;
import com.vinicius.jobhunter.service.JobAnalyzerService;
import com.vinicius.jobhunter.service.JobCollectorService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class JobHunterFlowTest {

    @Autowired
    private JobCollectorService collectorService;

    @Autowired
    private JobAnalyzerService analyzerService;

    @MockBean
    private JobRepository jobRepository;

    @MockBean
    private JobSourceRepository sourceRepository;

    @MockBean
    private GeminiClient geminiClient;

    @MockBean
    private Crawler rSSCrawler;

    @Test
    public void testFullFlow() {
        // 1. Setup Mock Crawler
        when(rSSCrawler.getSourceName()).thenReturn("MOCK_RSS");
        when(rSSCrawler.scrape()).thenReturn(List.of(
                new RawJobDTO(
                        "http://example.com/job/1",
                        "Junior Java Developer",
                        "Tech Corp",
                        "Description",
                        "MOCK_RSS",
                        LocalDateTime.now())));

        // 2. Setup Mock Repositories
        when(sourceRepository.findByName("MOCK_RSS")).thenReturn(Optional.empty()); // Will trigger save
        when(sourceRepository.save(Mockito.any()))
                .thenReturn(new com.vinicius.jobhunter.model.JobSource("MOCK_RSS", "AGGREGATOR"));

        when(jobRepository.existsByUrl("http://example.com/job/1")).thenReturn(false);

        // Capture saved job
        Mockito.doAnswer(invocation -> {
            Job j = invocation.getArgument(0);
            j.setId(java.util.UUID.randomUUID()); // Simulate ID gen
            return j;
        }).when(jobRepository).save(Mockito.any(Job.class));

        // 3. Run Collection
        collectorService.processCrawler(rSSCrawler);

        // Verify save called once
        Mockito.verify(jobRepository, Mockito.times(1)).save(Mockito.any(Job.class));

        // 4. Setup Analysis Mocking
        // Need to mock findAll() to return the job we "saved" (or just a new one)
        Job newJob = new Job();
        newJob.setId(java.util.UUID.randomUUID());
        newJob.setDescription(
                "Java Spring Boot Junior Job. We are looking for a developer to join our team. Experience with Java is required. Remote work available.");
        newJob.setStatus(JobStatus.NEW);

        when(jobRepository.findAll()).thenReturn(List.of(newJob));

        when(geminiClient.analyzeJob(anyString())).thenReturn(
                "{ \"seniority\": \"JUNIOR\", \"modality\": \"REMOTE\", \"stack\": [\"JAVA\", \"SPRING\"], \"is_english\": true }");

        // 5. Run Analysis
        analyzerService.analyzeNewJobs();

        // 6. Verify Enrichment and Update
        assertEquals(Seniority.JUNIOR, newJob.getSeniority());
        assertEquals(Modality.REMOTE, newJob.getModality());
        assertTrue(newJob.getTechStack().contains("JAVA"));
        assertTrue(newJob.getScore() > 0);

        // Verify save called again (update)
        Mockito.verify(jobRepository, Mockito.atLeast(2)).save(Mockito.any(Job.class));

        System.out.println("Test Passed - Logic Verified with Mocks!");
    }
}
