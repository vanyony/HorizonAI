package com.horizonai.service;

import com.horizonai.collector.CollectedContent;
import com.horizonai.collector.ContentCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ContentCollectionService {

    private static final Logger log = LoggerFactory.getLogger(ContentCollectionService.class);

    private final Map<String, ContentCollector> collectors;
    private final ContentIngestionService ingestionService;

    public ContentCollectionService(List<ContentCollector> collectorList,
                                    ContentIngestionService ingestionService) {
        this.collectors = new LinkedHashMap<>();
        for (ContentCollector collector : collectorList) {
            this.collectors.put(collector.sourceName().toUpperCase(Locale.ROOT), collector);
        }
        this.ingestionService = ingestionService;
    }

    public CollectionSummary collect(String sourceName) {
        ContentCollector collector = collectors.get(sourceName.toUpperCase(Locale.ROOT));
        if (collector == null) {
            throw new IllegalArgumentException("不支持的数据源: " + sourceName);
        }

        List<CollectedContent> contents = collector.collect();
        int created = 0;
        int skipped = 0;
        for (CollectedContent content : contents) {
            IngestionResult result = ingestionService.ingest(content);
            if (result.isCreated()) {
                created++;
            } else {
                skipped++;
            }
        }
        log.info("内容采集完成: source={}, fetched={}, created={}, skipped={}",
                sourceName, contents.size(), created, skipped);
        return new CollectionSummary(sourceName, contents.size(), created, skipped);
    }

    public static class CollectionSummary {
        private final String sourceName;
        private final int fetched;
        private final int created;
        private final int skipped;

        public CollectionSummary(String sourceName, int fetched, int created, int skipped) {
            this.sourceName = sourceName;
            this.fetched = fetched;
            this.created = created;
            this.skipped = skipped;
        }

        public String getSourceName() { return sourceName; }
        public int getFetched() { return fetched; }
        public int getCreated() { return created; }
        public int getSkipped() { return skipped; }
    }
}
