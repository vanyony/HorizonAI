package com.horizonai.controller;

import com.horizonai.common.Result;
import com.horizonai.dto.LoginRequest;
import com.horizonai.dto.RegisterRequest;
import com.horizonai.service.AuthService;
import com.horizonai.vo.LoginVO;
import com.horizonai.vo.UserVO;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    // ========== 手动构造器（替代 Lombok @RequiredArgsConstructor） ==========

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @GetMapping("/profile")
    public Result<UserVO> profile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return authService.getProfile(userId);
    }
}
