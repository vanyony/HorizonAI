package com.horizonai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.horizonai.common.BusinessException;
import com.horizonai.common.Result;
import com.horizonai.dto.LoginRequest;
import com.horizonai.dto.RegisterRequest;
import com.horizonai.entity.User;
import com.horizonai.entity.UserInterest;
import com.horizonai.mapper.UserInterestMapper;
import com.horizonai.mapper.UserMapper;
import com.horizonai.security.JwtTokenProvider;
import com.horizonai.service.AuthService;
import com.horizonai.vo.LoginVO;
import com.horizonai.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final UserInterestMapper userInterestMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Result<LoginVO> login(LoginRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername())
        );
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        LoginVO vo = new LoginVO(token, user.getUsername(), user.getRole());
        return Result.success(vo);
    }

    @Override
    @Transactional
    public Result<Void> register(RegisterRequest request) {
        // 检查用户名是否已存在
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername())
        );
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole("USER");
        userMapper.insert(user);

        // 保存用户兴趣标签
        if (request.getInterestTagIds() != null && !request.getInterestTagIds().isEmpty()) {
            for (Long tagId : request.getInterestTagIds()) {
                UserInterest interest = new UserInterest();
                interest.setUserId(user.getId());
                interest.setTagId(tagId);
                interest.setInterestLevel(1);
                userInterestMapper.insert(interest);
            }
        }

        return Result.success();
    }

    @Override
    public Result<UserVO> getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        UserVO vo = new UserVO(user.getId(), user.getUsername(), user.getEmail(), user.getRole(), user.getAvatar());
        return Result.success(vo);
    }
}
