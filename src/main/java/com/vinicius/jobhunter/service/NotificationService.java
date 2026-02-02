package com.vinicius.jobhunter.service;

import com.vinicius.jobhunter.client.TelegramClient;
import com.vinicius.jobhunter.model.Job;
import com.vinicius.jobhunter.model.JobStatus;
import com.vinicius.jobhunter.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JobRepository jobRepository;
    private final TelegramClient telegramClient;

    @Scheduled(cron = "0 0/20 * * * *")
    public void notifyBestJobs() {
        List<Job> highValueJobs = jobRepository.findAll().stream()
                .filter(j -> j.getStatus() == JobStatus.NEW && j.getScore() > 60)
                .toList();

        for (Job job : highValueJobs) {
            try {
                String msg = formatMessage(job);
                telegramClient.sendMessage(msg);

                job.setStatus(JobStatus.ARCHIVED);
                jobRepository.save(job);
            } catch (Exception e) {
                log.error("Failed to notify job {}", job.getId(), e);
            }
        }
    }

    private String formatMessage(Job job) {
        StringBuilder sb = new StringBuilder();
        sb.append("🔥 <b>New Opportunity!</b>\n\n");
        sb.append("<b>Title:</b> ").append(job.getTitle()).append("\n");
        sb.append("<b>Company:</b> ").append(job.getCompany() != null ? job.getCompany().getName() : "Unknown")
                .append("\n");
        sb.append("<b>Score:</b> ").append(job.getScore()).append("/100\n");
        sb.append("<b>Seniority:</b> ").append(job.getSeniority()).append("\n");
        sb.append("<b>Modality:</b> ").append(job.getModality()).append("\n");
        if (job.getTechStack() != null && !job.getTechStack().isEmpty()) {
            sb.append("<b>Stack:</b> ").append(String.join(", ", job.getTechStack())).append("\n");
        }
        sb.append("\n<a href=\"").append(job.getUrl()).append("\">Apply Here</a>");
        return sb.toString();
    }

    @Scheduled(cron = "0 0 6,10,14,18,22 * * *")
    public void sendDailyJobReport() {
        log.info("Starting job report...");

        java.time.LocalDateTime sevenDaysAgo = java.time.LocalDateTime.now().minusDays(7);
        java.time.LocalDateTime startOfDay = java.time.LocalDate.now().atStartOfDay();

        List<Job> todayJobs = jobRepository.findAll().stream()
                .filter(j -> j.getCollectedAt().isAfter(startOfDay))
                .filter(j -> j.getCollectedAt().isAfter(sevenDaysAgo))
                .filter(j -> j.getStatus() == JobStatus.NEW)
                .toList();

        if (todayJobs.isEmpty()) {
            log.info("No new jobs to report today");
            return;
        }

        String summary = String.format(
                "📊 <b>Relatório de Vagas - %s %02d:00</b>\n\n" +
                        "🎯 Vagas Java Junior/Estágio encontradas: <b>%d</b>\n\n" +
                        "Enviando detalhes...",
                java.time.LocalDate.now(),
                java.time.LocalTime.now().getHour(),
                todayJobs.size());
        telegramClient.sendMessage(summary);
        // Enviar cada vaga
        int sent = 0;
        for (int i = 0; i < todayJobs.size(); i++) {
            Job job = todayJobs.get(i);
            try {
                String msg = formatDailyJobMessage(job, i + 1);
                telegramClient.sendMessage(msg);

                job.setStatus(JobStatus.ARCHIVED);
                jobRepository.save(job);
                sent++;

                Thread.sleep(500);
            } catch (Exception e) {
                log.error("Failed to send daily job notification for job {}", job.getId(), e);
            }
        }

        log.info("Daily report sent: {}/{} jobs", sent, todayJobs.size());
    }

    private String formatDailyJobMessage(Job job, int index) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("🔹 <b>Vaga #%d</b>\n\n", index));
        sb.append("<b>Título:</b> ").append(job.getTitle()).append("\n");

        if (job.getCompany() != null) {
            sb.append("<b>Empresa:</b> ").append(job.getCompany().getName()).append("\n");
        }

        if (job.getSource() != null) {
            sb.append("<b>Fonte:</b> ").append(job.getSource().getName()).append("\n");
        }

        sb.append("\n<a href=\"").append(job.getUrl()).append("\">🔗 Ver Vaga</a>");

        return sb.toString();
    }
}
