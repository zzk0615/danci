package com.danci.web.dto;

import javax.validation.constraints.NotBlank;

public class TagCreateRequest {
    @NotBlank
    private String tagName;
    private String description;

    public String getTagName() { return tagName; }
    public void setTagName(String tagName) { this.tagName = tagName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}


