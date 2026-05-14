package com.horizonai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.horizonai.common.Result;
import com.horizonai.service.AiAnalysisService;
import com.horizonai.service.ArticleService;
import com.horizonai.vo.AiAnalysisVO;
import com.horizonai.vo.ArticleVO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleService articleService;
    private final AiAnalysisService aiAnalysisService;

    // ========== 手动构造器（替代 Lombok @RequiredArgsConstructor） ==========

    public ArticleController(ArticleService articleService, AiAnalysisService aiAnalysisService) {
        this.articleService = articleService;
        this.aiAnalysisService = aiAnalysisService;
    }

    @GetMapping
    public Result<Page<ArticleVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String sourceType,
            @RequestParam(required = false) Long tagId
    ) {
        return Result.success(articleService.page(page, size, sourceType, tagId));
    }

    @GetMapping("/{id}")
    public Result<ArticleVO> detail(@PathVariable Long id) {
        return Result.success(articleService.getById(id));
    }

    @GetMapping("/{id}/analysis")
    public Result<AiAnalysisVO> getAnalysis(@PathVariable Long id) {
        AiAnalysisVO analysis = aiAnalysisService.getLatestAnalysis(id);
        return Result.success(analysis);
    }
}
