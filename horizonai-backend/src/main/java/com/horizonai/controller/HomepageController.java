package com.horizonai.controller;

import com.horizonai.common.Result;
import com.horizonai.service.AiAnalysisService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class HomepageController {

    private final AiAnalysisService aiAnalysisService;

    // ========== 手动构造器（替代 Lombok @RequiredArgsConstructor） ==========

    public HomepageController(AiAnalysisService aiAnalysisService) {
        this.aiAnalysisService = aiAnalysisService;
    }

    @GetMapping("/homepage/today")
    public Result<Object> todayOverview() {
        return Result.success(aiAnalysisService.getTodayOverview());
    }
}
