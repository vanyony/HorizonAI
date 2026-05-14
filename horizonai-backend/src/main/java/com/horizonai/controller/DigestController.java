package com.horizonai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.horizonai.common.Result;
import com.horizonai.entity.DailyDigest;
import com.horizonai.service.DailyDigestService;
import com.horizonai.vo.ArticleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DigestController {

    private final DailyDigestService dailyDigestService;

    @GetMapping("/digests")
    public Result<Page<DailyDigest>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(dailyDigestService.page(page, size));
    }

    @GetMapping("/digests/{date}")
    public Result<DailyDigest> getByDate(@PathVariable String date) {
        return Result.success(dailyDigestService.getByDate(date));
    }

    @GetMapping("/digests/today")
    public Result<DailyDigest> getToday() {
        return Result.success(dailyDigestService.getToday());
    }
}
