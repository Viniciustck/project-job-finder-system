package com.vinicius.jobhunter.crawler;

import com.vinicius.jobhunter.dto.RawJobDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class LinkedInScraperCrawler implements Crawler {

    private static final String BASE_URL = "https://www.linkedin.com/jobs/search/";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

    // Otimizado para Java Junior/Estágio
    private static final List<String> SEARCH_QUERIES = Arrays.asList(
            "Java Junior Developer",
            "Java Intern",
            "Spring Boot Junior",
            "Backend Developer Junior Java",
            "Java Trainee",
            "Estágio Java");

    @Override
    public List<RawJobDTO> scrape() {
        List<RawJobDTO> jobs = new ArrayList<>();

        try {
            for (String query : SEARCH_QUERIES) {
                jobs.addAll(scrapeLinkedIn(query, "Remote"));
            }
        } catch (Exception e) {
            log.error("Failed to scrape LinkedIn", e);
        }

        return jobs;
    }

    @Override
    public String getSourceName() {
        return "LINKEDIN_SCRAPER";
    }

    private List<RawJobDTO> scrapeLinkedIn(String keywords, String location) {
        List<RawJobDTO> jobs = new ArrayList<>();

        try {
            String url = BASE_URL + "?keywords=" + keywords.replace(" ", "%20") +
                    "&location=" + location.replace(" ", "%20") + "&f_WT=2"; // f_WT=2 = Remote

            log.info("Scraping LinkedIn for: {} in {}", keywords, location);

            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(10000)
                    .get();

            Elements jobCards = doc.select("div.base-card");

            for (Element card : jobCards) {
                try {
                    Element titleElement = card.selectFirst("h3.base-search-card__title");
                    Element companyElement = card.selectFirst("h4.base-search-card__subtitle");
                    Element locationElement = card.selectFirst("span.job-search-card__location");
                    Element linkElement = card.selectFirst("a.base-card__full-link");

                    String title = titleElement != null ? titleElement.text().trim() : "";
                    String company = companyElement != null ? companyElement.text().trim() : "Unknown";
                    String loc = locationElement != null ? locationElement.text().trim() : location;
                    String jobUrl = linkElement != null ? linkElement.attr("href") : "";

                    String description = "LinkedIn job posting for " + keywords + " in " + loc;

                    // Only add if we have at least title and URL
                    if (!title.isEmpty() && !jobUrl.isEmpty()) {
                        jobs.add(new RawJobDTO(jobUrl, title, company, description, "LINKEDIN_SCRAPER", null));
                    }
                } catch (Exception e) {
                    log.debug("Failed to parse LinkedIn job card: {}", e.getMessage());
                }
            }

            log.info("Found {} jobs from LinkedIn", jobs.size());
        } catch (Exception e) {
            log.error("Failed to scrape LinkedIn: {}", e.getMessage());
        }

        return jobs;
    }
}
