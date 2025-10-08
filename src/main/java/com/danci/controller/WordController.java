package com.danci.controller;

import com.danci.common.ApiResponse;
import com.danci.entity.Word;
import com.danci.service.WordService;
import com.danci.web.dto.PdfGenerateRequest;
import com.danci.web.dto.WordBatchCreateRequest;
import com.danci.web.dto.WordCreateRequest;
import com.danci.web.dto.WordUpdateRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/words")
public class WordController {

    private final WordService wordService;

    public WordController(WordService wordService) {
        this.wordService = wordService;
    }

    @PostMapping
    public ApiResponse<Word> create(@Validated @RequestBody WordCreateRequest req) {
        return ApiResponse.success(wordService.create(req));
    }

    @PostMapping("/batch")
    public ApiResponse<List<Word>> batchCreate(@Validated @RequestBody WordBatchCreateRequest req) {
        return ApiResponse.success(wordService.batchCreate(req));
    }

    @GetMapping("/{id}")
    public ApiResponse<Word> get(@PathVariable Long id) {
        return ApiResponse.success(wordService.getById(id));
    }

    @PutMapping
    public ApiResponse<Word> update(@Validated @RequestBody WordUpdateRequest req) {
        return ApiResponse.success(wordService.update(req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        wordService.deleteById(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/byTag")
    public ApiResponse<List<Word>> listByTag(@RequestParam Long tagId) {
        return ApiResponse.success(wordService.listByTag(tagId));
    }

    @PostMapping("/generatePdf")
    public ResponseEntity<byte[]> generatePdf(@Validated @RequestBody PdfGenerateRequest req) {
        byte[] pdf = wordService.generatePdf(req);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=words.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}


