package com.vinicius.jobhunter.crawler;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.vinicius.jobhunter.dto.RawJobDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class RssCrawler implements Crawler {

    private static final List<String> RSS_FEEDS = List.of(
            "https://weworkremotely.com/categories/remote-programming-jobs.rss",
            "https://remoteok.com/remote-dev-jobs.rss",
            "https://remotive.com/remote-jobs/software-dev/feed",
            "https://stackoverflow.com/jobs/feed");

    @Override
    public List<RawJobDTO> scrape() {
        List<RawJobDTO> jobs = new ArrayList<>();

        for (String feedUrl : RSS_FEEDS) {
            try {
                URL url = new URL(feedUrl);
                SyndFeedInput input = new SyndFeedInput();
                SyndFeed feed = input.build(new XmlReader(url));

                for (SyndEntry entry : feed.getEntries()) {
                    // Basic filtering to ensure it's not empty
                    if (entry.getLink() != null && entry.getTitle() != null) {
                        var postedAt = entry.getPublishedDate() != null
                                ? entry.getPublishedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                                : null;

                        jobs.add(new RawJobDTO(
                                entry.getLink(),
                                entry.getTitle(),
                                "Unknown (RSS)", // Often RSS doesn't give company clearly in title
                                entry.getDescription().getValue(),
                                "RSS_AGGREGATOR",
                                postedAt));
                    }
                }
            } catch (Exception e) {
                log.error("Failed to parse RSS feed: {}", feedUrl, e);
            }
        }
        return jobs;
    }

    @Override
    public String getSourceName() {
        return "RSS_AGGREGATOR";
    }
}
