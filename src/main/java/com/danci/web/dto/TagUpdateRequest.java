package com.danci.web.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class TagUpdateRequest {
    @NotNull
    private Long id;
    @NotBlank
    private String tagName;
    private String description;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTagName() { return tagName; }
    public void setTagName(String tagName) { this.tagName = tagName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}


