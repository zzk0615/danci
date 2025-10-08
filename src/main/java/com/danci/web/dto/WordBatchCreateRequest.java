package com.danci.web.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

public class WordBatchCreateRequest {
    @NotEmpty
    @Valid
    private List<WordCreateRequest> items;
    private List<Long> tagIds;

    public List<WordCreateRequest> getItems() { return items; }
    public void setItems(List<WordCreateRequest> items) { this.items = items; }
    public List<Long> getTagIds() { return tagIds; }
    public void setTagIds(List<Long> tagIds) { this.tagIds = tagIds; }
}


