package com.danci.service.impl;

import com.danci.entity.Tag;
import com.danci.repository.TagRepository;
import com.danci.service.TagService;
import com.danci.web.dto.TagCreateRequest;
import com.danci.web.dto.TagUpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    public TagServiceImpl(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    @Transactional
    public Tag create(TagCreateRequest request) {
        Tag tag = new Tag();
        tag.setTagName(request.getTagName());
        tag.setDescription(request.getDescription());
        return tagRepository.save(tag);
    }

    @Override
    public List<Tag> listAll() {
        return tagRepository.findAll();
    }

    @Override
    @Transactional
    public Tag update(TagUpdateRequest request) {
        Tag tag = tagRepository.findById(request.getId()).orElseThrow(() -> new IllegalArgumentException("Tag not found: " + request.getId()));
        tag.setTagName(request.getTagName());
        tag.setDescription(request.getDescription());
        return tagRepository.save(tag);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        tagRepository.deleteById(id);
    }
}


