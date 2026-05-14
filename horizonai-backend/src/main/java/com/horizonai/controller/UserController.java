package com.horizonai.controller;

import com.horizonai.common.Result;
import com.horizonai.service.UserService;
import com.horizonai.vo.BrowseHistoryVO;
import com.horizonai.vo.TagVO;
import com.horizonai.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public Result<UserVO> profile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(userService.getProfile(userId));
    }

    @PutMapping("/profile")
    public Result<UserVO> updateProfile(Authentication authentication,
                                        @RequestBody Map<String, String> body) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(userService.updateProfile(userId,
                body.get("email"), body.get("avatar")));
    }

    @GetMapping("/interests")
    public Result<List<TagVO>> getInterests(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(userService.getInterests(userId));
    }

    @PutMapping("/interests")
    public Result<Void> updateInterests(Authentication authentication,
                                        @RequestBody Map<String, List<Long>> body) {
        Long userId = (Long) authentication.getPrincipal();
        userService.updateInterests(userId, body.get("tagIds"));
        return Result.success();
    }

    @GetMapping("/history")
    public Result<List<BrowseHistoryVO>> history(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(userService.getBrowseHistory(userId));
    }

    @PostMapping("/history/{articleId}")
    public Result<Void> recordBrowse(Authentication authentication,
                                     @PathVariable Long articleId) {
        Long userId = (Long) authentication.getPrincipal();
        userService.recordBrowse(userId, articleId);
        return Result.success();
    }
}
