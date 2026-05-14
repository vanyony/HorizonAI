package com.horizonai.service;

import com.horizonai.vo.TagVO;
import java.util.List;

public interface TagService {

    List<TagVO> listAll();

    TagVO create(String name, String description);

    void delete(Long id);
}
