package com.danci.repository;

import com.danci.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WordRepository extends JpaRepository<Word, Long> {
    Optional<Word> findByEnglish(String english);
    List<Word> findByChineseContaining(String keyword);
}


