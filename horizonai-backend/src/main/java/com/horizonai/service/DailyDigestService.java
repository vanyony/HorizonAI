package com.horizonai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.horizonai.entity.DailyDigest;
import com.horizonai.vo.ArticleVO;

import java.util.List;

public interface DailyDigestService {

    Page<DailyDigest> page(Integer pageNum, Integer pageSize);

    DailyDigest getByDate(String date);

    DailyDigest getToday();
}
