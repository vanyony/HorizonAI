package com.horizonai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.horizonai.common.Result;
import com.horizonai.dto.ArticleSaveDTO;
import com.horizonai.service.AiAnalysisService;
import com.horizonai.service.ArticleService;
import com.horizonai.vo.AiAnalysisVO;
import com.horizonai.vo.ArticleVO;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 后台管理接口（需要 ADMIN 角色）
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ArticleService articleService;
    private final AiAnalysisService aiAnalysisService;

    // ========== 手动构造器（替代 Lombok @RequiredArgsConstructor） ==========

    public AdminController(ArticleService articleService, AiAnalysisService aiAnalysisService) {
        this.articleService = articleService;
        this.aiAnalysisService = aiAnalysisService;
    }

    // ========== 文章管理 ==========

    @GetMapping("/articles")
    public Result<Page<ArticleVO>> listArticles(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String sourceType,
            @RequestParam(required = false) Long tagId
    ) {
        return Result.success(articleService.page(page, size, sourceType, tagId));
    }

    @GetMapping("/articles/{id}")
    public Result<ArticleVO> getArticle(@PathVariable Long id) {
        return Result.success(articleService.getById(id));
    }

    @PostMapping("/articles")
    public Result<ArticleVO> createArticle(@Valid @RequestBody ArticleSaveDTO dto) {
        return Result.success(articleService.create(dto));
    }

    @PutMapping("/articles/{id}")
    public Result<ArticleVO> updateArticle(@PathVariable Long id, @Valid @RequestBody ArticleSaveDTO dto) {
        return Result.success(articleService.update(id, dto));
    }

    @DeleteMapping("/articles/{id}")
    public Result<Void> deleteArticle(@PathVariable Long id) {
        articleService.delete(id);
        return Result.success();
    }

    // ========== AI 分析 ==========

    @PostMapping("/articles/{id}/reanalyze")
    public Result<AiAnalysisVO> reanalyze(@PathVariable Long id) {
        return Result.success(aiAnalysisService.analyzeArticle(id));
    }
}
