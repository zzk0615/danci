package com.danci.web.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

public class WordUpdateRequest {
    @NotNull
    private Long id;
    @NotBlank
    private String english;
    @NotBlank
    private String chinese;
    private List<Long> tagIds;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEnglish() { return english; }
    public void setEnglish(String english) { this.english = english; }
    public String getChinese() { return chinese; }
    public void setChinese(String chinese) { this.chinese = chinese; }
    public List<Long> getTagIds() { return tagIds; }
    public void setTagIds(List<Long> tagIds) { this.tagIds = tagIds; }
}


