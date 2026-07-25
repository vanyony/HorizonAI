package com.horizonai.collector;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class DemoContentCollector implements ContentCollector {

    public static final String SOURCE_NAME = "DEMO";

    @Override
    public String sourceName() {
        return SOURCE_NAME;
    }

    @Override
    public List<CollectedContent> collect() {
        List<CollectedContent> contents = new ArrayList<>();
        contents.add(content(
                "demo-java-virtual-threads",
                "Java 虚拟线程在高并发服务中的应用边界",
                "虚拟线程降低了线程阻塞型服务的并发成本，但仍需关注数据库连接池和下游限流。",
                "Java"
        ));
        contents.add(content(
                "demo-llm-observability",
                "大模型应用的调用链路可观测性实践",
                "通过 Trace、结构化日志和调用记录关联用户请求、工具执行与模型响应。",
                "AI"
        ));
        contents.add(content(
                "demo-agent-tool-control",
                "受控工具调用让研究助手的回答更可验证",
                "白名单工具、调用次数限制和证据引用可以降低开放式 Agent 的不可控性。",
                "开源项目"
        ));
        return contents;
    }

    private CollectedContent content(String externalId,
                                     String title,
                                     String summary,
                                     String tag) {
        CollectedContent content = new CollectedContent();
        content.setSourceName(SOURCE_NAME);
        content.setExternalId(externalId);
        content.setSourceType("NEWS");
        content.setTitle(title);
        content.setSummary(summary);
        content.setContent(summary);
        content.setSourceUrl("https://demo.horizonai.local/content/" + externalId);
        content.setPublishDate(LocalDate.now());
        content.setTags(List.of(tag));
        return content;
    }
}
