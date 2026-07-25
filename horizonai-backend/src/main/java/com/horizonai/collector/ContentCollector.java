package com.horizonai.collector;

import java.util.List;

public interface ContentCollector {

    String sourceName();

    List<CollectedContent> collect();
}
