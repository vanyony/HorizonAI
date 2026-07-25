package com.horizonai.collector;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RssContentCollector implements ContentCollector {

    public static final String SOURCE_NAME = "RSS";

    private final RestTemplate restTemplate;
    private final String configuredUrls;

    public RssContentCollector(
            RestTemplate restTemplate,
            @Value("${collector.rss.urls:}") String configuredUrls) {
        this.restTemplate = restTemplate;
        this.configuredUrls = configuredUrls;
    }

    @Override
    public String sourceName() {
        return SOURCE_NAME;
    }

    @Override
    public List<CollectedContent> collect() {
        List<String> urls = Arrays.stream(configuredUrls.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
        if (urls.isEmpty()) {
            throw new IllegalStateException("未配置 RSS 数据源，请设置 RSS_FEED_URLS");
        }

        List<CollectedContent> collected = new ArrayList<>();
        for (String url : urls) {
            String xml = restTemplate.getForObject(URI.create(url), String.class);
            if (!StringUtils.hasText(xml)) {
                continue;
            }
            collected.addAll(parseFeed(url, xml));
        }
        return collected;
    }

    private List<CollectedContent> parseFeed(String feedUrl, String xml) {
        try {
            DocumentBuilderFactory factory = secureDocumentBuilderFactory();
            Document document = factory.newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xml)));

            NodeList items = document.getElementsByTagName("item");
            boolean atom = items.getLength() == 0;
            if (atom) {
                items = document.getElementsByTagName("entry");
            }

            List<CollectedContent> result = new ArrayList<>();
            for (int index = 0; index < items.getLength(); index++) {
                Node node = items.item(index);
                if (!(node instanceof Element)) {
                    continue;
                }
                Element element = (Element) node;
                String title = childText(element, "title");
                String link = atom ? atomLink(element) : childText(element, "link");
                String summary = firstNonBlank(
                        childText(element, "description"),
                        childText(element, "summary"),
                        childText(element, "content")
                );
                if (!StringUtils.hasText(title) || !StringUtils.hasText(link)) {
                    continue;
                }

                CollectedContent content = new CollectedContent();
                content.setSourceName(SOURCE_NAME);
                content.setExternalId(firstNonBlank(
                        childText(element, "guid"),
                        childText(element, "id"),
                        sha256(link)
                ));
                content.setSourceType("NEWS");
                content.setTitle(title.trim());
                content.setSummary(cleanText(summary));
                content.setContent(cleanText(summary));
                content.setSourceUrl(link.trim());
                content.setPublishDate(parseDate(firstNonBlank(
                        childText(element, "pubDate"),
                        childText(element, "published"),
                        childText(element, "updated")
                )));
                content.setTags(List.of(feedHost(feedUrl)));
                result.add(content);
            }
            return result;
        } catch (Exception e) {
            throw new IllegalStateException("RSS 解析失败: " + feedUrl, e);
        }
    }

    private DocumentBuilderFactory secureDocumentBuilderFactory() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setExpandEntityReferences(false);
        factory.setXIncludeAware(false);
        return factory;
    }

    private String childText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        return nodes.getLength() == 0 ? "" : nodes.item(0).getTextContent();
    }

    private String atomLink(Element parent) {
        NodeList nodes = parent.getElementsByTagName("link");
        for (int index = 0; index < nodes.getLength(); index++) {
            Node node = nodes.item(index);
            if (node instanceof Element) {
                String href = ((Element) node).getAttribute("href");
                if (StringUtils.hasText(href)) {
                    return href;
                }
            }
        }
        return "";
    }

    private LocalDate parseDate(String value) {
        if (!StringUtils.hasText(value)) {
            return LocalDate.now();
        }
        try {
            return ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME).toLocalDate();
        } catch (Exception ignored) {
            try {
                return OffsetDateTime.parse(value).toLocalDate();
            } catch (Exception ignoredAgain) {
                return LocalDate.now();
            }
        }
    }

    private String cleanText(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.replaceAll("<[^>]+>", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return "";
    }

    private String feedHost(String feedUrl) {
        try {
            String host = URI.create(feedUrl).getHost();
            return StringUtils.hasText(host) ? host : "RSS";
        } catch (Exception e) {
            return "RSS";
        }
    }

    private String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte item : digest) {
                builder.append(String.format("%02x", item));
            }
            return builder.toString();
        } catch (Exception e) {
            throw new IllegalStateException("无法生成 RSS 外部标识", e);
        }
    }
}
