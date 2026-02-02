package com.vinicius.jobhunter.crawler;

import com.vinicius.jobhunter.dto.RawJobDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdzunaCrawler implements Crawler {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${job-hunter.api.adzuna.app-id}")
    private String appId;

    @Value("${job-hunter.api.adzuna.app-key}")
    private String appKey;

    private static final String BASE_URL = "https://api.adzuna.com/v1/api/jobs";

    // Otimizado para Java Junior/Estágio
    private static final List<SearchQuery> SEARCH_QUERIES = Arrays.asList(
            new SearchQuery("br", "Java Junior Developer remote"),
            new SearchQuery("br", "Estágio Java"),
            new SearchQuery("us", "Java Junior remote"),
            new SearchQuery("us", "Java Intern remote"));

    private record SearchQuery(String country, String keywords) {
    }

    @Override
    public List<RawJobDTO> scrape() {
        List<RawJobDTO> jobs = new ArrayList<>();

        // Skip if API keys not configured
        if (appId == null || appId.isEmpty() || appKey == null || appKey.isEmpty()) {
            log.warn("Adzuna API credentials not configured. Skipping.");
            return jobs;
        }

        try {
            for (SearchQuery query : SEARCH_QUERIES) {
                jobs.addAll(searchAdzuna(query.country(), query.keywords()));
            }
        } catch (Exception e) {
            log.error("Failed to scrape Adzuna", e);
        }

        return jobs;
    }

    @Override
    public String getSourceName() {
        return "ADZUNA";
    }

    private List<RawJobDTO> searchAdzuna(String country, String query) {
        List<RawJobDTO> jobs = new ArrayList<>();

        try {
            String url = String.format("%s/%s/search/1?app_id=%s&app_key=%s&what=%s&results_per_page=50",
                    BASE_URL, country, appId, appKey, query.replace(" ", "%20"));

            log.info("Searching Adzuna ({}) for: {}", country, query);
            String response = restTemplate.getForObject(url, String.class);

            if (response != null) {
                JSONObject json = new JSONObject(response);
                JSONArray results = json.getJSONArray("results");

                for (int i = 0; i < results.length(); i++) {
                    JSONObject jobJson = results.getJSONObject(i);

                    String title = jobJson.optString("title", "");
                    String company = jobJson.optJSONObject("company") != null
                            ? jobJson.getJSONObject("company").optString("display_name", "Unknown")
                            : "Unknown";
                    String location = jobJson.optJSONObject("location") != null
                            ? jobJson.getJSONObject("location").optString("display_name", "Remote")
                            : "Remote";
                    String jobUrl = jobJson.optString("redirect_url", "");
                    String description = jobJson.optString("description", "");

                    // Extract salary if available
                    if (jobJson.has("salary_min") && jobJson.has("salary_max")) {
                        double min = jobJson.optDouble("salary_min", 0);
                        double max = jobJson.optDouble("salary_max", 0);
                        if (min > 0 && max > 0) {
                            description = description + "\n\nSalary: $" + (int) min + " - $" + (int) max;
                        }
                    }

                    jobs.add(new RawJobDTO(jobUrl, title, company, description, "ADZUNA_" + country.toUpperCase(),
                            null));
                }

                log.info("Found {} jobs from Adzuna ({})", jobs.size(), country);
            }
        } catch (Exception e) {
            log.error("Failed to search Adzuna ({}): {}", country, e.getMessage());
        }

        return jobs;
    }
}
