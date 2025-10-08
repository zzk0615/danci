package com.danci.web.dto;

import javax.validation.constraints.NotBlank;
import java.util.List;

public class PdfGenerateRequest {
    private List<Long> tagIds;
    private List<Long> wordIds;
    @NotBlank
    private String mode; // 默写中文 / 默写英文

    public List<Long> getTagIds() { return tagIds; }
    public void setTagIds(List<Long> tagIds) { this.tagIds = tagIds; }
    public List<Long> getWordIds() { return wordIds; }
    public void setWordIds(List<Long> wordIds) { this.wordIds = wordIds; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
}


