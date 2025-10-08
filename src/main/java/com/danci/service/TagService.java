package com.danci.service;

import com.danci.entity.Tag;
import com.danci.web.dto.TagCreateRequest;
import com.danci.web.dto.TagUpdateRequest;

import java.util.List;

public interface TagService {
    Tag create(TagCreateRequest request);
    Tag update(TagUpdateRequest request);
    List<Tag> listAll();
    void deleteById(Long id);
}


