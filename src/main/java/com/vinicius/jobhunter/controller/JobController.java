package com.vinicius.jobhunter.controller;

import com.vinicius.jobhunter.client.TelegramClient;
import com.vinicius.jobhunter.model.Job;
import com.vinicius.jobhunter.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Slf4j
public class JobController {

    private final JobRepository jobRepository;
    private final TelegramClient telegramClient;

    @PostMapping("/notify-today")
    public String notifyTodayJobs() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

        List<Job> todayJobs = jobRepository.findAll().stream()
                .filter(j -> j.getCollectedAt().isAfter(startOfDay))
                .filter(j -> j.getCollectedAt().isAfter(sevenDaysAgo))
                .toList();

        log.info("Found {} jobs collected today", todayJobs.size());

        if (todayJobs.isEmpty()) {
            return "No jobs collected today";
        }

        String summary = String.format(
                "📊 <b>Relatório de Vagas - %s</b>\n\n" +
                        "Total de vagas Java Junior/Estágio coletadas hoje: <b>%d</b>\n\n" +
                        "Enviando detalhes...",
                LocalDate.now(), todayJobs.size());
        telegramClient.sendMessage(summary);

        int sent = 0;

        for (int i = 0; i < todayJobs.size(); i++) {
            Job job = todayJobs.get(i);
            try {
                String msg = formatJobMessage(job, i + 1);
                telegramClient.sendMessage(msg);
                sent++;

                Thread.sleep(500);
            } catch (Exception e) {
                log.error("Failed to send job notification", e);
            }
        }

        String result = String.format("Sent %d/%d jobs to Telegram", sent, todayJobs.size());
        log.info(result);

        return result;
    }

    private String formatJobMessage(Job job, int index) {
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
