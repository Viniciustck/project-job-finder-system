package com.vinicius.jobhunter.crawler;

import com.vinicius.jobhunter.dto.RawJobDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JSearchCrawler implements Crawler {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${job-hunter.api.jsearch.key}")
    private String apiKey;

    private static final String API_URL = "https://jsearch.p.rapidapi.com/search";

    // Otimizado para Java Junior/Estágio
    private static final List<String> SEARCH_QUERIES = Arrays.asList(
            "Java Junior Developer remote",
            "Java Intern remote",
            "Spring Boot Junior remote",
            "Backend Java Junior remote");

    @Override
    public List<RawJobDTO> scrape() {
        List<RawJobDTO> jobs = new ArrayList<>();

        // Skip if API key not configured
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("JSearch API key not configured. Skipping.");
            return jobs;
        }

        try {
            for (String query : SEARCH_QUERIES) {
                jobs.addAll(searchJSearch(query));
            }
        } catch (Exception e) {
            log.error("Failed to scrape JSearch", e);
        }

        return jobs;
    }

    @Override
    public String getSourceName() {
        return "JSEARCH";
    }

    private List<RawJobDTO> searchJSearch(String query) {
        List<RawJobDTO> jobs = new ArrayList<>();

        try {
            String url = API_URL + "?query=" + query.replace(" ", "%20") + "&num_pages=1";

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-RapidAPI-Key", apiKey);
            headers.set("X-RapidAPI-Host", "jsearch.p.rapidapi.com");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            log.info("Searching JSearch for: {}", query);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getBody() != null) {
                JSONObject json = new JSONObject(response.getBody());
                JSONArray data = json.optJSONArray("data");

                if (data != null) {
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject jobJson = data.getJSONObject(i);

                        String title = jobJson.optString("job_title", "");
                        String company = jobJson.optString("employer_name", "Unknown");
                        String location = jobJson.optString("job_city", "Remote");
                        String jobUrl = jobJson.optString("job_apply_link", "");
                        String description = jobJson.optString("job_description", "");

                        // Add employment type if available
                        String employmentType = jobJson.optString("job_employment_type", "");
                        if (!employmentType.isEmpty()) {
                            description = "Type: " + employmentType + "\n\n" + description;
                        }

                        jobs.add(new RawJobDTO(jobUrl, title, company, description, "JSEARCH", null));
                    }
                }

                log.info("Found {} jobs from JSearch", jobs.size());
            }
        } catch (Exception e) {
            log.error("Failed to search JSearch: {}", e.getMessage());
        }

        return jobs;
    }
}
