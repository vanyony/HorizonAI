package com.horizonai.service;

import com.horizonai.vo.BrowseHistoryVO;
import com.horizonai.vo.TagVO;
import com.horizonai.vo.UserVO;

import java.util.List;

public interface UserService {

    UserVO getProfile(Long userId);

    UserVO updateProfile(Long userId, String email, String avatar);

    List<TagVO> getInterests(Long userId);

    void updateInterests(Long userId, List<Long> tagIds);

    List<BrowseHistoryVO> getBrowseHistory(Long userId);

    void recordBrowse(Long userId, Long articleId);
}
