package com.horizonai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.horizonai.dto.ArticleSaveDTO;
import com.horizonai.vo.ArticleVO;

public interface ArticleService {

    Page<ArticleVO> page(Integer pageNum, Integer pageSize, String sourceType, Long tagId);

    ArticleVO getById(Long id);

    ArticleVO create(ArticleSaveDTO dto);

    ArticleVO update(Long id, ArticleSaveDTO dto);

    void delete(Long id);
}
