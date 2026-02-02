package com.vinicius.jobhunter.crawler;

import com.vinicius.jobhunter.dto.RawJobDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class GithubCrawler implements Crawler {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String GITHUB_API_URL = "https://api.github.com/repos/backend-br/vagas/issues?state=open&per_page=50";

    @Override
    public List<RawJobDTO> scrape() {
        List<RawJobDTO> jobs = new ArrayList<>();
        log.info("Crawling GitHub Issues (backend-br/vagas)...");

        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    GITHUB_API_URL,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    });

            List<Map<String, Object>> issues = response.getBody();
            if (issues == null)
                return jobs;

            for (Map<String, Object> issue : issues) {
                String title = (String) issue.get("title");
                String body = (String) issue.get("body");
                String url = (String) issue.get("html_url");

                // GitHub labels usually come as a List/Array of maps
                // We're simplifying by checking title/body for keywords

                if (isRelevant(title, body)) {
                    RawJobDTO job = new RawJobDTO();
                    job.setTitle(title);
                    job.setCompany("GitHub Community"); // Usually company name is in title, but hard to parse
                                                        // consistently
                    job.setDescription(body != null ? body : title);
                    job.setUrl(url);
                    job.setSource("GITHUB_ISSUES");
                    jobs.add(job);
                }
            }
            log.info("Found {} relevant jobs from GitHub", jobs.size());

        } catch (Exception e) {
            log.error("Error crawling GitHub Issues: {}", e.getMessage());
        }

        return jobs;
    }

    private boolean isRelevant(String title, String body) {
        if (title == null)
            return false;
        String content = (title + " " + (body != null ? body : "")).toLowerCase();

        boolean isJava = content.contains("java") || content.contains("spring");
        boolean isJunior = content.contains("junior") || content.contains("júnior") ||
                content.contains("estágio") || content.contains("estagiário") ||
                content.contains("intern");

        return isJava && isJunior;
    }

    @Override
    public String getSourceName() {
        return "GITHUB_ISSUES";
    }
}
