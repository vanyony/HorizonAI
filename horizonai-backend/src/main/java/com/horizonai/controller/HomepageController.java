package com.horizonai.controller;

import com.horizonai.common.Result;
import com.horizonai.service.AiAnalysisService;
import com.horizonai.vo.AiAnalysisVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HomepageController {

    private final AiAnalysisService aiAnalysisService;

    @GetMapping("/homepage/today")
    public Result<Object> todayOverview() {
        return Result.success(aiAnalysisService.getTodayOverview());
    }
}
