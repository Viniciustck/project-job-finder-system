package com.vinicius.jobhunter.service;

import com.vinicius.jobhunter.client.TelegramClient;
import com.vinicius.jobhunter.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthCheckService {

    private final JobRepository jobRepository;
    private final TelegramClient telegramClient;

    @Scheduled(cron = "0 0 8 * * *") // Every day at 8:00 AM
    public void performHealthCheck() {
        log.info("Performing daily health check...");
        try {
            long jobCount = jobRepository.count();
            String message = String.format(
                    "✅ **System Health Check**\n\n- **Status**: Online\n- **Database**: Connected\n- **Total Jobs**: %d\n- **Time**: %s",
                    jobCount, java.time.LocalDateTime.now());

            telegramClient.sendMessage(message);
            log.info("Health check passed. Message sent.");
        } catch (Exception e) {
            log.error("Health check failed", e);
            telegramClient.sendMessage("❌ **CRITICAL: System Health Check Failed!**\n\nError: " + e.getMessage());
        }
    }
}
