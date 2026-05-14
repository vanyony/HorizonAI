package com.horizonai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.horizonai.entity.DailyDigest;
import com.horizonai.mapper.DailyDigestMapper;
import com.horizonai.service.DailyDigestService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class DailyDigestServiceImpl implements DailyDigestService {

    private final DailyDigestMapper dailyDigestMapper;

    // ========== 手动构造器（替代 Lombok @RequiredArgsConstructor） ==========

    public DailyDigestServiceImpl(DailyDigestMapper dailyDigestMapper) {
        this.dailyDigestMapper = dailyDigestMapper;
    }

    @Override
    public Page<DailyDigest> page(Integer pageNum, Integer pageSize) {
        Page<DailyDigest> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DailyDigest> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(DailyDigest::getDigestDate);
        return dailyDigestMapper.selectPage(page, wrapper);
    }

    @Override
    public DailyDigest getByDate(String date) {
        return dailyDigestMapper.selectOne(
                new LambdaQueryWrapper<DailyDigest>()
                        .eq(DailyDigest::getDigestDate, LocalDate.parse(date)));
    }

    @Override
    public DailyDigest getToday() {
        return dailyDigestMapper.selectOne(
                new LambdaQueryWrapper<DailyDigest>()
                        .eq(DailyDigest::getDigestDate, LocalDate.now()));
    }
}
