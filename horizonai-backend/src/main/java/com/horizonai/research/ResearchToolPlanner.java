package com.horizonai.research;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ResearchToolPlanner {

    private final Map<String, ResearchTool> tools;

    public ResearchToolPlanner(List<ResearchTool> toolList) {
        this.tools = toolList.stream()
                .collect(Collectors.toMap(ResearchTool::name, Function.identity()));
    }

    public List<ResearchTool> plan(String query) {
        List<ResearchTool> selected = new ArrayList<>();
        add(selected, "site_search");

        String normalized = query.toLowerCase(Locale.ROOT);
        if (normalized.contains("github")
                || normalized.contains("开源")
                || normalized.contains("仓库")
                || normalized.contains("趋势")) {
            add(selected, "github_trend");
        }
        add(selected, "interest_match");
        return selected.stream().limit(3).collect(Collectors.toList());
    }

    private void add(List<ResearchTool> selected, String toolName) {
        ResearchTool tool = tools.get(toolName);
        if (tool != null) {
            selected.add(tool);
        }
    }
}
