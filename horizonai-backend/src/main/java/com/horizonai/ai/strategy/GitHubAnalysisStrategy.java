package com.horizonai.ai.strategy;

import com.horizonai.entity.Article;

/**
 * GitHub 项目分析策略
 */
public class GitHubAnalysisStrategy implements AnalysisStrategy {

    @Override
    public String buildSystemPrompt() {
        return "你是一位资深开源项目评估专家，专注于评估 GitHub 项目的技术价值和实用性。\n\n"
                + "分析要求：\n"
                + "1. 评估项目的实用价值（1-10分），考虑 Star 趋势、技术先进性、社区活跃度\n"
                + "2. 分析适合什么层次的开发者使用（入门/中级/高级）\n"
                + "3. 评估该项目对技术生态的影响\n"
                + "4. 给出学习该项目的具体建议或入门路径\n"
                + "5. 用简洁的中文总结项目核心亮点（100字以内）\n\n"
                + "请严格按照以下 JSON 格式返回（不要包含其他内容）：\n"
                + "{\n"
                + "  \"importanceRating\": 数字1-10,\n"
                + "  \"targetAudience\": \"目标用户描述\",\n"
                + "  \"industryImpact\": \"技术生态影响分析\",\n"
                + "  \"learningSuggestions\": \"学习路径或使用建议\",\n"
                + "  \"summary\": \"100字以内摘要\"\n"
                + "}";
    }

    @Override
    public String buildUserMessage(Article article) {
        return String.format("请分析以下 GitHub 项目：\n\n项目名称：%s\n\n项目描述：%s\n\n项目链接：%s",
                article.getTitle(),
                article.getContent() != null ? article.getContent() : article.getSummary(),
                article.getSourceUrl() != null ? article.getSourceUrl() : "无");
    }

    @Override
    public String getStrategyName() {
        return "GitHub项目分析";
    }
}
