package com.horizonai.ai.strategy;

import com.horizonai.entity.Article;

/**
 * 技术趋势分析策略
 */
public class TrendAnalysisStrategy implements AnalysisStrategy {

    @Override
    public String buildSystemPrompt() {
        return "你是一位资深技术趋势分析师，专注于识别和解读技术发展方向。\n\n"
                + "分析要求：\n"
                + "1. 评估该趋势的重要性（1-10分），考虑发展潜力、采纳速度、行业关注度\n"
                + "2. 分析哪些角色最应该关注该趋势\n"
                + "3. 评估该趋势对整个技术行业的潜在影响\n"
                + "4. 给出跟进学习的具体建议\n"
                + "5. 用简洁的中文总结趋势要点（100字以内）\n\n"
                + "请严格按照以下 JSON 格式返回（不要包含其他内容）：\n"
                + "{\n"
                + "  \"importanceRating\": 数字1-10,\n"
                + "  \"targetAudience\": \"关注人群描述\",\n"
                + "  \"industryImpact\": \"行业影响分析\",\n"
                + "  \"learningSuggestions\": \"跟进学习建议\",\n"
                + "  \"summary\": \"100字以内摘要\"\n"
                + "}";
    }

    @Override
    public String buildUserMessage(Article article) {
        return String.format("请分析以下技术趋势：\n\n标题：%s\n\n详细描述：%s",
                article.getTitle(),
                article.getContent() != null ? article.getContent() : article.getSummary());
    }

    @Override
    public String getStrategyName() {
        return "技术趋势分析";
    }
}
