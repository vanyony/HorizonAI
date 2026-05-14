package com.horizonai.controller;

import com.horizonai.common.Result;
import com.horizonai.service.TagService;
import com.horizonai.vo.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public Result<List<TagVO>> listAll() {
        return Result.success(tagService.listAll());
    }

    @PostMapping
    public Result<TagVO> create(@RequestParam String name, @RequestParam(required = false) String description) {
        return Result.success(tagService.create(name, description));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return Result.success();
    }
}
