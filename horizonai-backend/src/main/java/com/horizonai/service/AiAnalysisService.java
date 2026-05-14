package com.horizonai.service;

import com.horizonai.vo.AiAnalysisVO;

public interface AiAnalysisService {

    /**
     * 对指定文章执行 AI 分析
     */
    AiAnalysisVO analyzeArticle(Long articleId);

    /**
     * 获取文章的最新 AI 分析结果
     */
    AiAnalysisVO getLatestAnalysis(Long articleId);

    /**
     * 获取今日技术速览数据
     */
    Object getTodayOverview();
}
