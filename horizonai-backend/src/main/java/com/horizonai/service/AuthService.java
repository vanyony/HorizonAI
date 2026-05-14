package com.horizonai.service;

import com.horizonai.common.Result;
import com.horizonai.dto.LoginRequest;
import com.horizonai.dto.RegisterRequest;
import com.horizonai.vo.LoginVO;
import com.horizonai.vo.UserVO;

public interface AuthService {

    Result<LoginVO> login(LoginRequest request);

    Result<Void> register(RegisterRequest request);

    Result<UserVO> getProfile(Long userId);
}
