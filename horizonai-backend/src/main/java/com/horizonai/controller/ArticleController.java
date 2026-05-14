package com.horizonai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.horizonai.common.Result;
import com.horizonai.dto.ArticleSaveDTO;
import com.horizonai.service.ArticleService;
import com.horizonai.vo.ArticleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

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

    @PostMapping
    public Result<ArticleVO> create(@Valid @RequestBody ArticleSaveDTO dto) {
        return Result.success(articleService.create(dto));
    }

    @PutMapping("/{id}")
    public Result<ArticleVO> update(@PathVariable Long id, @Valid @RequestBody ArticleSaveDTO dto) {
        return Result.success(articleService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return Result.success();
    }
}
