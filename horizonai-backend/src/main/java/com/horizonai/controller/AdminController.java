package com.horizonai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.horizonai.common.Result;
import com.horizonai.dto.ArticleSaveDTO;
import com.horizonai.service.ArticleService;
import com.horizonai.vo.ArticleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 后台管理接口（需要 ADMIN 角色）
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ArticleService articleService;

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
}
