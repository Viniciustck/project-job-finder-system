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
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class IndeedScraperCrawler implements Crawler {

    private static final String BASE_URL = "https://www.indeed.com/jobs";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

    @Override
    public List<RawJobDTO> scrape() {
        List<RawJobDTO> jobs = new ArrayList<>();

        try {
            jobs.addAll(scrapeIndeed("Java Developer", "Remote"));
            jobs.addAll(scrapeIndeed("Spring Boot", "Remote"));
        } catch (Exception e) {
            log.error("Failed to scrape Indeed", e);
        }

        return jobs;
    }

    @Override
    public String getSourceName() {
        return "INDEED_SCRAPER";
    }

    private List<RawJobDTO> scrapeIndeed(String query, String location) {
        List<RawJobDTO> jobs = new ArrayList<>();

        try {
            String url = BASE_URL + "?q=" + query.replace(" ", "+") +
                    "&l=" + location.replace(" ", "+") + "&remotejob=032b3046-06a3-4876-8dfd-474eb5e7ed11";

            log.info("Scraping Indeed for: {} in {}", query, location);

            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(10000)
                    .get();

            Elements jobCards = doc.select("div.job_seen_beacon");

            for (Element card : jobCards) {
                try {
                    Element titleElement = card.selectFirst("h2.jobTitle span");
                    Element companyElement = card.selectFirst("span.companyName");
                    Element locationElement = card.selectFirst("div.companyLocation");
                    Element linkElement = card.selectFirst("h2.jobTitle a");

                    String title = titleElement != null ? titleElement.text().trim() : "";
                    String company = companyElement != null ? companyElement.text().trim() : "Unknown";
                    String loc = locationElement != null ? locationElement.text().trim() : location;

                    String jobUrl = "";
                    if (linkElement != null) {
                        String jobKey = linkElement.attr("data-jk");
                        if (!jobKey.isEmpty()) {
                            jobUrl = "https://www.indeed.com/viewjob?jk=" + jobKey;
                        }
                    }

                    // Try to get snippet/description
                    Element snippetElement = card.selectFirst("div.job-snippet");
                    String description = snippetElement != null ? snippetElement.text().trim()
                            : "Indeed job posting for " + query;

                    // Only add if we have at least title and URL
                    if (!title.isEmpty() && !jobUrl.isEmpty()) {
                        jobs.add(new RawJobDTO(jobUrl, title, company, description, "INDEED_SCRAPER", null));
                    }
                } catch (Exception e) {
                    log.debug("Failed to parse Indeed job card: {}", e.getMessage());
                }
            }

            log.info("Found {} jobs from Indeed", jobs.size());
        } catch (Exception e) {
            log.error("Failed to scrape Indeed: {}", e.getMessage());
        }

        return jobs;
    }
}
