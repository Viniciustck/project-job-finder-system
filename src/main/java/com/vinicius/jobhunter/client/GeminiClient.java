package com.vinicius.jobhunter.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;

@Service
@Slf4j
public class GeminiClient {

    @Value("${job-hunter.api.gemini.key}")
    private String apiKey;

    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=";
    private final RestTemplate restTemplate = new RestTemplate();
    private final Gson gson = new Gson();

    public String analyzeJob(String description) {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("Gemini API Key is missing. Skipping AI enrichment.");
            return null;
        }

        try {
            String prompt = """
                    Analyze this job description and extract JSON:
                    {
                       "seniority": "INTERN | JUNIOR | SENIOR | UNKNOWN",
                       "modality": "REMOTE | HYBRID | ONSITE | UNKNOWN",
                       "stack": ["JAVA", "NODE", "SPRING", "REACT", ...],
                       "is_english": boolean
                    }

                    Description:
                    """ + description.substring(0, Math.min(description.length(), 5000)); // Truncate to avoid limits

            JsonObject requestBody = new JsonObject();
            JsonArray contents = new JsonArray();
            JsonObject content = new JsonObject();
            JsonArray parts = new JsonArray();
            JsonObject part = new JsonObject();
            part.addProperty("text", prompt);
            parts.add(part);
            content.add("parts", parts);
            contents.add(content);
            requestBody.add("contents", contents);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

            String response = restTemplate.postForObject(API_URL + apiKey, entity, String.class);
            return extractTextFromResponse(response);

        } catch (Exception e) {
            log.error("Error calling Gemini API", e);
            return null;
        }
    }

    private String extractTextFromResponse(String jsonResponse) {
        try {
            JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();
            return root.getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString()
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();
        } catch (Exception e) {
            log.error("Failed to parse Gemini response", e);
            return null;
        }
    }
}
