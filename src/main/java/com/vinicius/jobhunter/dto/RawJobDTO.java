package com.vinicius.jobhunter.dto;

import java.time.LocalDateTime;

public record RawJobDTO(
        String url,
        String title,
        String companyName,
        String description,
        String sourceName,
        LocalDateTime postedAt) {
}
