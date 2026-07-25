package com.horizonai.research;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResearchToolPlannerTest {

    private final ResearchToolPlanner planner = new ResearchToolPlanner(List.of(
            tool("site_search"),
            tool("github_trend"),
            tool("interest_match")
    ));

    @Test
    void defaultQuestionUsesSiteSearchAndInterestMatch() {
        List<String> tools = names(planner.plan("Java 虚拟线程适合什么场景？"));

        assertEquals(List.of("site_search", "interest_match"), tools);
    }

    @Test
    void openSourceTrendQuestionUsesAllWhitelistedTools() {
        List<String> tools = names(planner.plan("最近 GitHub 有哪些开源趋势？"));

        assertEquals(List.of("site_search", "github_trend", "interest_match"), tools);
    }

    private ResearchTool tool(String name) {
        return new ResearchTool() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public List<Evidence> execute(ResearchToolContext context) {
                return List.of();
            }
        };
    }

    private List<String> names(List<ResearchTool> tools) {
        return tools.stream().map(ResearchTool::name).collect(Collectors.toList());
    }
}
