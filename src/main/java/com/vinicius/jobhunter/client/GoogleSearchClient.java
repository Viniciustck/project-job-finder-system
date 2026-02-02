package com.vinicius.jobhunter.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class GoogleSearchClient {

    @Value("${job-hunter.search.google-key}")
    private String apiKey;

    @Value("${job-hunter.search.google-cx}")
    private String cx;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<String> searchLinkedinJobs(String query) {
        if (apiKey == null || apiKey.isEmpty() || cx == null || cx.isEmpty()) {
            log.warn("Google Search API not configured (Key or CX missing). Skipping.");
            return List.of();
        }

        List<String> links = new ArrayList<>();
        try {
            // site:linkedin.com/jobs ensures we get job cards
            String fullQuery = "site:linkedin.com/jobs " + query;

            String url = UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/customsearch/v1")
                    .queryParam("key", apiKey)
                    .queryParam("cx", cx)
                    .queryParam("q", fullQuery)
                    .queryParam("dateRestrict", "d7") // Last 7 days
                    .toUriString();

            String response = restTemplate.getForObject(url, String.class);
            JsonObject json = JsonParser.parseString(response).getAsJsonObject();

            if (json.has("items")) {
                JsonArray items = json.getAsJsonArray("items");
                items.forEach(item -> {
                    String link = item.getAsJsonObject().get("link").getAsString();
                    links.add(link);
                });
            }
        } catch (Exception e) {
            log.error("Failed to search Google", e);
        }
        return links;
    }
}
