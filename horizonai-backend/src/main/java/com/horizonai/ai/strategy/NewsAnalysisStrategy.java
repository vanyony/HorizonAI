package com.horizonai.ai.strategy;

import com.horizonai.entity.Article;

/**
 * 技术新闻分析策略
 */
public class NewsAnalysisStrategy implements AnalysisStrategy {

    @Override
    public String buildSystemPrompt() {
        return "你是一位资深技术新闻分析师，专注于评估技术新闻的价值和影响。\n\n"
                + "分析要求：\n"
                + "1. 评估新闻的重要性（1-10分），考虑时效性、行业影响范围、技术深度\n"
                + "2. 分析这条新闻对哪些人群最有价值\n"
                + "3. 评估对相关行业的短期和长期影响\n"
                + "4. 给出具体的学习建议或行动建议\n"
                + "5. 用简洁的中文总结新闻要点（100字以内）\n\n"
                + "请严格按照以下 JSON 格式返回（不要包含其他内容）：\n"
                + "{\n"
                + "  \"importanceRating\": 数字1-10,\n"
                + "  \"targetAudience\": \"目标受众描述\",\n"
                + "  \"industryImpact\": \"行业影响分析\",\n"
                + "  \"learningSuggestions\": \"学习或行动建议\",\n"
                + "  \"summary\": \"100字以内摘要\"\n"
                + "}";
    }

    @Override
    public String buildUserMessage(Article article) {
        return String.format("请分析以下技术新闻：\n\n标题：%s\n\n内容：%s",
                article.getTitle(),
                article.getContent() != null ? article.getContent() : article.getSummary());
    }

    @Override
    public String getStrategyName() {
        return "技术新闻分析";
    }
}
