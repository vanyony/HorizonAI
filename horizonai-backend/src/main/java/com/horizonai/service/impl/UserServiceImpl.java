package com.horizonai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.horizonai.common.BusinessException;
import com.horizonai.entity.*;
import com.horizonai.mapper.*;
import com.horizonai.service.UserService;
import com.horizonai.vo.BrowseHistoryVO;
import com.horizonai.vo.TagVO;
import com.horizonai.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserInterestMapper userInterestMapper;
    private final BrowseHistoryMapper browseHistoryMapper;
    private final TagMapper tagMapper;
    private final ArticleMapper articleMapper;

    @Override
    public UserVO getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        return new UserVO(user.getId(), user.getUsername(), user.getEmail(), user.getRole(), user.getAvatar());
    }

    @Override
    public UserVO updateProfile(Long userId, String email, String avatar) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        if (email != null) user.setEmail(email);
        if (avatar != null) user.setAvatar(avatar);
        userMapper.updateById(user);
        return new UserVO(user.getId(), user.getUsername(), user.getEmail(), user.getRole(), user.getAvatar());
    }

    @Override
    public List<TagVO> getInterests(Long userId) {
        List<UserInterest> interests = userInterestMapper.selectList(
                new LambdaQueryWrapper<UserInterest>().eq(UserInterest::getUserId, userId));
        if (interests.isEmpty()) return new ArrayList<>();

        List<Long> tagIds = interests.stream().map(UserInterest::getTagId).collect(Collectors.toList());
        return tagMapper.selectBatchIds(tagIds).stream()
                .map(t -> new TagVO(t.getId(), t.getName(), t.getDescription(), t.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateInterests(Long userId, List<Long> tagIds) {
        userInterestMapper.delete(new LambdaQueryWrapper<UserInterest>().eq(UserInterest::getUserId, userId));
        if (tagIds != null) {
            for (Long tagId : tagIds) {
                UserInterest interest = new UserInterest();
                interest.setUserId(userId);
                interest.setTagId(tagId);
                interest.setInterestLevel(1);
                userInterestMapper.insert(interest);
            }
        }
    }

    @Override
    public List<BrowseHistoryVO> getBrowseHistory(Long userId) {
        List<BrowseHistory> history = browseHistoryMapper.selectList(
                new LambdaQueryWrapper<BrowseHistory>()
                        .eq(BrowseHistory::getUserId, userId)
                        .orderByDesc(BrowseHistory::getCreatedAt)
                        .last("LIMIT 30"));

        if (history.isEmpty()) return new ArrayList<>();

        List<Long> articleIds = history.stream().map(BrowseHistory::getArticleId).collect(Collectors.toList());
        Map<Long, Article> articleMap = articleMapper.selectBatchIds(articleIds).stream()
                .collect(Collectors.toMap(Article::getId, a -> a));

        return history.stream()
                .map(h -> {
                    Article a = articleMap.get(h.getArticleId());
                    String title = a != null ? a.getTitle() : "已删除";
                    String type = a != null ? a.getSourceType() : "";
                    return new BrowseHistoryVO(h.getArticleId(), title, type, h.getCreatedAt());
                })
                .collect(Collectors.toList());
    }

    @Override
    public void recordBrowse(Long userId, Long articleId) {
        Long count = browseHistoryMapper.selectCount(
                new LambdaQueryWrapper<BrowseHistory>()
                        .eq(BrowseHistory::getUserId, userId)
                        .eq(BrowseHistory::getArticleId, articleId));
        if (count == 0) {
            BrowseHistory bh = new BrowseHistory();
            bh.setUserId(userId);
            bh.setArticleId(articleId);
            browseHistoryMapper.insert(bh);
        }
    }
}
