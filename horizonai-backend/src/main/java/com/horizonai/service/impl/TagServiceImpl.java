package com.horizonai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.horizonai.common.BusinessException;
import com.horizonai.entity.ArticleTag;
import com.horizonai.entity.Tag;
import com.horizonai.mapper.ArticleTagMapper;
import com.horizonai.mapper.TagMapper;
import com.horizonai.service.TagService;
import com.horizonai.vo.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;

    @Override
    public List<TagVO> listAll() {
        return tagMapper.selectList(null).stream()
                .map(t -> new TagVO(t.getId(), t.getName(), t.getDescription(), t.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public TagVO create(String name, String description) {
        Long count = tagMapper.selectCount(
                new LambdaQueryWrapper<Tag>().eq(Tag::getName, name)
        );
        if (count > 0) {
            throw new BusinessException("标签已存在");
        }
        Tag tag = new Tag();
        tag.setName(name);
        tag.setDescription(description);
        tagMapper.insert(tag);
        return new TagVO(tag.getId(), tag.getName(), tag.getDescription(), tag.getCreatedAt());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // 删除标签关联
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>()
                .eq(ArticleTag::getTagId, id));
        tagMapper.deleteById(id);
    }
}
