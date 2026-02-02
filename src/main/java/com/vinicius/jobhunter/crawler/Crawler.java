package com.vinicius.jobhunter.crawler;

import com.vinicius.jobhunter.dto.RawJobDTO;
import java.util.List;

public interface Crawler {
    List<RawJobDTO> scrape();

    String getSourceName();
}
