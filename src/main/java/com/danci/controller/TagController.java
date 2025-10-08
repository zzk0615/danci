package com.danci.controller;

import com.danci.common.ApiResponse;
import com.danci.entity.Tag;
import com.danci.service.TagService;
import com.danci.web.dto.TagCreateRequest;
import com.danci.web.dto.TagUpdateRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @PostMapping
    public ApiResponse<Tag> create(@Validated @RequestBody TagCreateRequest req) {
        return ApiResponse.success(tagService.create(req));
    }

    @GetMapping
    public ApiResponse<List<Tag>> listAll() {
        return ApiResponse.success(tagService.listAll());
    }

    @PutMapping
    public ApiResponse<Tag> update(@Validated @RequestBody TagUpdateRequest req) {
        return ApiResponse.success(tagService.update(req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        tagService.deleteById(id);
        return ApiResponse.success(null);
    }
}


