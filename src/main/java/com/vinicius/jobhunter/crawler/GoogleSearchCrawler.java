package com.vinicius.jobhunter.crawler;

import com.vinicius.jobhunter.client.GoogleSearchClient;
import com.vinicius.jobhunter.dto.RawJobDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile("google-search") // Disabled by default - enable with spring.profiles.active=google-search
@RequiredArgsConstructor
@Slf4j
public class GoogleSearchCrawler implements Crawler {

    private final GoogleSearchClient googleClient;

    @Override
    public List<RawJobDTO> scrape() {
        // We search for multiple variations to cover ground
        List<String> queries = List.of(
                "(\"estágio\" OR \"junior\") AND \"java\"",
                "(\"estágio\" OR \"junior\") AND \"node\"");

        List<RawJobDTO> collectedJobs = new ArrayList<>();

        for (String q : queries) {
            List<String> urls = googleClient.searchLinkedinJobs(q);
            for (String url : urls) {
                try {
                    // Random delay to be polite
                    Thread.sleep(2000);
                    RawJobDTO job = visitPage(url);
                    if (job != null) {
                        collectedJobs.add(job);
                    }
                } catch (Exception e) {
                    log.error("Error visiting URL: {}", url, e);
                }
            }
        }
        return collectedJobs;
    }

    private RawJobDTO visitPage(String url) {
        try {
            // Jsoup request with Browser User-Agent
            Document doc = Jsoup.connect(url)
                    .userAgent(
                            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .referrer("https://www.google.com/")
                    .get();

            // Linkedin Public pages usually have JSON-LD structured data
            // We look for <script type="application/ld+json">
            String title = doc.title();
            String description = "";
            String company = "Unknown";

            // Fallback: Meta tags
            // <meta property="og:title" content="...">
            // <meta property="og:description" content="...">

            Element ogTitle = doc.selectFirst("meta[property=og:title]");
            if (ogTitle != null)
                title = ogTitle.attr("content");

            Element ogDesc = doc.selectFirst("meta[property=og:description]");
            if (ogDesc != null)
                description = ogDesc.attr("content");

            // Try to find Company from title "Job Title at Company"
            if (title.contains(" at ")) {
                String[] parts = title.split(" at ");
                if (parts.length > 1) {
                    company = parts[1].split(" \\|")[0]; // Remove "| LinkedIn"
                }
            }

            // If description is too short, Jsoup body text (messy but contains keywords)
            if (description.length() < 100) {
                description = doc.body().text().substring(0, Math.min(doc.body().text().length(), 5000));
            }

            return new RawJobDTO(
                    url,
                    title,
                    company,
                    description,
                    "LINKEDIN_VIA_GOOGLE",
                    LocalDateTime.now());

        } catch (Exception e) {
            log.warn("Could not scrape page content for {}: {}", url, e.getMessage());
            return null;
        }
    }

    @Override
    public String getSourceName() {
        return "GOOGLE_LINKEDIN";
    }
}
