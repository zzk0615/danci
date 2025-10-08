package com.danci.service;

import com.danci.entity.Word;
import com.danci.web.dto.PdfGenerateRequest;
import com.danci.web.dto.WordBatchCreateRequest;
import com.danci.web.dto.WordCreateRequest;
import com.danci.web.dto.WordUpdateRequest;

import java.util.List;

public interface WordService {
    Word create(WordCreateRequest request);
    List<Word> batchCreate(WordBatchCreateRequest request);
    Word getById(Long id);
    Word update(WordUpdateRequest request);
    void deleteById(Long id);
    List<Word> listByTag(Long tagId);
    byte[] generatePdf(PdfGenerateRequest request);
}


