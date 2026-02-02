package com.vinicius.jobhunter.crawler;

import com.vinicius.jobhunter.dto.RawJobDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProgramathorCrawler implements Crawler {

    private static final String BASE_URL = "https://programathor.com.br/jobs";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";

    @Override
    public List<RawJobDTO> scrape() {
        List<RawJobDTO> jobs = new ArrayList<>();
        log.info("Crawling Programathor...");

        try {
            Document doc = Jsoup.connect(BASE_URL)
                    .userAgent(USER_AGENT)
                    .timeout(10000)
                    .get();

            // Programathor structure: .cell-list usually contains the job items
            // We'll iterate through all links that look like job postings
            Elements jobElements = doc.select(".cell-list");

            for (Element cell : jobElements) {
                try {
                    String title = cell.select("h3").text();
                    if (title.isEmpty())
                        continue;

                    Element linkElement = cell.select("a").first();
                    String relativeUrl = linkElement != null ? linkElement.attr("href") : "";
                    String url = "https://programathor.com.br" + relativeUrl;

                    // Company name is tricky without specific class, usually mixed in text
                    // We will use a placeholder or try to parse
                    String cellText = cell.text();
                    String company = "Programathor Job"; // Placeholder since structure varies

                    // Filter Logic: "Java" and "Junior/Estágio"
                    if (isRelevant(title, cellText)) {
                        RawJobDTO job = new RawJobDTO(
                                url,
                                title,
                                company,
                                cellText,
                                getSourceName(),
                                LocalDateTime.now());
                        jobs.add(job);
                    }

                } catch (Exception e) {
                    log.warn("Error parsing Programathor items: {}", e.getMessage());
                }
            }
            log.info("Found {} relevant jobs from PROGRAMATHOR", jobs.size());

        } catch (IOException e) {
            log.error("Error crawling Programathor: {}", e.getMessage());
        }

        return jobs;
    }

    private boolean isRelevant(String title, String body) {
        String content = (title + " " + body).toLowerCase();

        boolean isJava = content.contains("java") || content.contains("spring");
        boolean isJunior = content.contains("junior") || content.contains("júnior") ||
                content.contains("estágio") || content.contains("estagiário") ||
                content.contains("intern");

        return isJava && isJunior;
    }

    @Override
    public String getSourceName() {
        return "PROGRAMATHOR";
    }
}
