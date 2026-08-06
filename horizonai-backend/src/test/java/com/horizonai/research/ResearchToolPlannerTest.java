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

    @Test
    void missingToolIsSkippedInsteadOfReturningNull() {
        ResearchToolPlanner limitedPlanner = new ResearchToolPlanner(List.of(
                tool("site_search"),
                tool("interest_match")
        ));

        List<ResearchTool> planned = limitedPlanner.plan("GitHub 开源趋势");

        assertEquals(List.of("site_search", "interest_match"), names(planned));
    }

    @Test
    void plannerNeverReturnsMoreThanThreeTools() {
        List<String> tools = names(planner.plan("GitHub 开源仓库趋势"));

        assertEquals(3, tools.size());
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
