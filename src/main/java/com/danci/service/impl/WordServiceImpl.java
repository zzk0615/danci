package com.danci.service.impl;

import com.danci.entity.Tag;
import com.danci.entity.Word;
import com.danci.repository.TagRepository;
import com.danci.repository.WordRepository;
import com.danci.service.WordService;
import com.danci.web.dto.PdfGenerateRequest;
import com.danci.web.dto.WordBatchCreateRequest;
import com.danci.web.dto.WordCreateRequest;
import com.danci.web.dto.WordUpdateRequest;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WordServiceImpl implements WordService {

    private final WordRepository wordRepository;
    private final TagRepository tagRepository;
    private static final Logger log = LoggerFactory.getLogger(WordServiceImpl.class);

    public WordServiceImpl(WordRepository wordRepository, TagRepository tagRepository) {
        this.wordRepository = wordRepository;
        this.tagRepository = tagRepository;
    }

    @Override
    @Transactional
    public Word create(WordCreateRequest request) {
        Word word = new Word();
        word.setEnglish(request.getEnglish());
        word.setChinese(request.getChinese());
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(request.getTagIds()));
            word.setTags(tags);
        }
        return wordRepository.save(word);
    }

    @Override
    @Transactional
    public List<Word> batchCreate(WordBatchCreateRequest request) {
        Set<Tag> boundTags = new HashSet<>();
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            boundTags.addAll(tagRepository.findAllById(request.getTagIds()));
        }
        List<Word> toSave = request.getItems().stream().map(i -> {
            Word w = new Word();
            w.setEnglish(i.getEnglish());
            w.setChinese(i.getChinese());
            if (!boundTags.isEmpty()) {
                w.setTags(boundTags);
            }
            return w;
        }).collect(Collectors.toList());
        return wordRepository.saveAll(toSave);
    }

    @Override
    public Word getById(Long id) {
        return wordRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Word not found: " + id));
    }

    @Override
    @Transactional
    public Word update(WordUpdateRequest request) {
        Word word = getById(request.getId());
        word.setEnglish(request.getEnglish());
        word.setChinese(request.getChinese());
        if (request.getTagIds() != null) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(request.getTagIds()));
            word.setTags(tags);
        }
        return wordRepository.save(word);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        wordRepository.deleteById(id);
    }

    @Override
    public List<Word> listByTag(Long tagId) {
        Tag tag = tagRepository.findById(tagId).orElseThrow(() -> new NoSuchElementException("Tag not found: " + tagId));
        return wordRepository.findAll().stream()
                .filter(w -> w.getTags() != null && w.getTags().stream().anyMatch(t -> Objects.equals(t.getId(), tag.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public byte[] generatePdf(PdfGenerateRequest request) {
        List<Word> words = new ArrayList<>();
        if (request.getWordIds() != null && !request.getWordIds().isEmpty()) {
            words.addAll(wordRepository.findAllById(request.getWordIds()));
        }
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            List<Word> byTags = wordRepository.findAll().stream()
                    .filter(w -> w.getTags() != null && w.getTags().stream().anyMatch(t -> request.getTagIds().contains(t.getId())))
                    .collect(Collectors.toList());
            words.addAll(byTags);
        }
        // 去重
        Map<Long, Word> map = new LinkedHashMap<>();
        for (Word w : words) { map.put(w.getId(), w); }
        words = new ArrayList<>(map.values());
        Collections.shuffle(words);

        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // 页面与度量
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            // 字体
            org.apache.pdfbox.pdmodel.font.PDFont font = loadCjkFont(doc);

            // =======================
            // 版式/布局说明（A4 竖向）
            // margin:    页面四周留白，避免打印裁切；单位：pt（1/72 英寸）
            // colGutter: 三个大列之间的水平间距
            // innerGutter: 一个大列内，左右两个小格之间的水平间距
            // numCols:   每行放置的大列数量（本需求为 3）
            // colWidth:  单个大列的宽度，基于可用宽度与间距自动计算
            // rowHeight: 每一行（两小格的垂直高度相同）的高度；影响一页能容纳的行数
            // rowGap:    行与行之间的垂直留白
            // 注：若想挤更多行/列，可减少 margin、rowGap 或 rowHeight；
            //     若想放大书写区，可增加 rowHeight 或减少列数。
            // =======================
            final float margin = 36f;               // 约 0.5 英寸
            final float pageWidth = page.getMediaBox().getWidth();
            final float pageHeight = page.getMediaBox().getHeight();
            final float usableWidth = pageWidth - margin * 2f; // 内容可用宽度
            final float colGutter = 12f;            // 大列间距
            final float innerGutter = 4f;           // 小格中间间距
            final int numCols = 3;                  // 三个大列
            final float colWidth = (usableWidth - colGutter * (numCols - 1)) / numCols; // 列宽自动分配
            final float rowHeight = 50f;            // 每行高度
            final float rowGap = 10f;               // 行距

            float cursorY = pageHeight - margin; // 当前行顶部 Y
            PDPageContentStream cs = new PDPageContentStream(doc, page);

            boolean modeDictationEnglish = "默写英文".equals(request.getMode());
            int colIndex = 0;
            for (int i = 0; i < words.size(); i++) {
                Word w = words.get(i);

                // 新行或新页判断
                if (colIndex == 0) {
                    if (cursorY - rowHeight < margin) {
                        cs.close();
                        page = new PDPage(PDRectangle.A4);
                        doc.addPage(page);
                        cursorY = page.getMediaBox().getHeight() - margin;
                        cs = new PDPageContentStream(doc, page);
                    }
                }

                float colX = margin + colIndex * (colWidth + colGutter);
                // 小格尺寸：左右各半
                float smallWidth = (colWidth - innerGutter) / 2f;
                float smallHeight = rowHeight;
                float leftX = colX;
                float rightX = colX + smallWidth + innerGutter;
                float topY = cursorY;

                // 网格策略：
                // - 默写英文：左侧显示中文，不画四线三格；右侧空，画四线三格
                // - 默写中文：左侧显示英文，左右两侧都不画四线三格（仅外框）
                boolean leftNeedsGrid = modeDictationEnglish ? false : false; // 均为 false；显式写出便于理解
                boolean rightNeedsGrid = modeDictationEnglish ? true : false;

                drawCell(cs, leftX, topY - smallHeight, smallWidth, smallHeight, leftNeedsGrid);
                drawCell(cs, rightX, topY - smallHeight, smallWidth, smallHeight, rightNeedsGrid);

                // 写入文字：左侧填内容，右侧留空
                String leftText = modeDictationEnglish ? (w.getChinese() == null ? "" : w.getChinese()) : (w.getEnglish() == null ? "" : w.getEnglish());
                if (!leftText.isEmpty()) {
                    float prefer = modeDictationEnglish ? 16f : 14f; // 中文略大，英文略小
                    drawTextInCellAutoFit(cs, font, prefer, 10f, leftX, topY - smallHeight, smallWidth, smallHeight, leftText);
                }

                // 下一个列
                colIndex++;
                if (colIndex >= numCols) {
                    // 换行
                    colIndex = 0;
                    cursorY -= (rowHeight + rowGap);
                }
            }

            cs.close();
            doc.save(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("PDF generate failed", e);
        }
    }

    private org.apache.pdfbox.pdmodel.font.PDFont loadCjkFont(PDDocument doc) throws IOException {
        // 1) 优先从类路径加载内置 CJK 字体（推荐放置在 resources/fonts/ 下）
        String[] classpathCandidates = new String[] {
                "/fonts/NotoSansCJK-Regular.otf",
                "/fonts/NotoSansCJKsc-Regular.otf",
                "/NotoSansCJK-Regular.otf",
                "/NotoSansCJKsc-Regular.otf"
        };
        for (String cp : classpathCandidates) {
            java.io.InputStream in = getClass().getResourceAsStream(cp);
            if (in != null) {
                try {
                    // 先尝试从流直接加载（subset=false -> true）
                    try {
                        org.apache.pdfbox.pdmodel.font.PDFont f = PDType0Font.load(doc, in, false);
                        log.info("Loaded CJK font from classpath (no subset): {}", cp);
                        return f;
                    } catch (IOException e1) {
                        log.warn("Direct load (no subset) failed for {}: {}", cp, String.valueOf(e1.getMessage()));
                    }
                } finally {
                    try { in.close(); } catch (IOException ignored) {}
                }
                // 某些环境下 InputStream + shaded jar 可能有问题：解压到临时文件再加载
                java.io.InputStream in2 = getClass().getResourceAsStream(cp);
                if (in2 != null) {
                    java.nio.file.Path tmp = null;
                    try {
                        tmp = java.nio.file.Files.createTempFile("NotoSansCJK-", ".otf");
                        java.nio.file.Files.copy(in2, tmp, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        try {
                            org.apache.pdfbox.pdmodel.font.PDFont f = PDType0Font.load(doc, java.nio.file.Files.newInputStream(tmp), false);
                            log.info("Loaded CJK font from temp file (no subset): {}", tmp);
                            return f;
                        } catch (IOException e3) {
                            log.warn("Temp load (no subset) failed: {}", String.valueOf(e3.getMessage()));
                            try {
                                org.apache.pdfbox.pdmodel.font.PDFont f2 = PDType0Font.load(doc, java.nio.file.Files.newInputStream(tmp), true);
                                log.info("Loaded CJK font from temp file (subset): {}", tmp);
                                return f2;
                            } catch (IOException e4) {
                                log.warn("Temp load (subset) failed: {}", String.valueOf(e4.getMessage()));
                            }
                        }
                    } catch (IOException e2) {
                        log.warn("Copy classpath font to temp failed for {}: {}", cp, String.valueOf(e2.getMessage()));
                    } finally {
                        try { in2.close(); } catch (IOException ignored) {}
                        if (tmp != null) {
                            try { java.nio.file.Files.deleteIfExists(tmp); } catch (IOException ignored) {}
                        }
                    }
                }
            }
        }
        // 作为补充：使用上下文类加载器尝试查找（兼容某些打包方式）
        try {
            java.io.InputStream alt = Thread.currentThread().getContextClassLoader().getResourceAsStream("fonts/NotoSansCJK-Regular.otf");
            if (alt != null) {
                try {
                    org.apache.pdfbox.pdmodel.font.PDFont f = PDType0Font.load(doc, alt, true);
                    log.info("Loaded CJK font from TCCL: fonts/NotoSansCJK-Regular.otf");
                    return f;
                } catch (IOException ignored) {
                    // continue
                }
            }
        } catch (Throwable ignored) {
        }

        // 2) 回退到常见的系统字体（Windows / Linux）
        String[] candidates = new String[] {
                "C:/Windows/Fonts/msyh.ttf",
                "C:/Windows/Fonts/msyh.ttc",
                "C:/Windows/Fonts/msyhl.ttc",
                "C:/Windows/Fonts/msyhbd.ttc",
                "C:/Windows/Fonts/simsun.ttc",
                "/usr/share/fonts/opentype/noto/NotoSansCJK-Bold.ttc",
                "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.otf",
                "/usr/share/fonts/opentype/noto/NotoSansCJKsc-Regular.otf",
                "/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.ttc",
                "/usr/share/fonts/truetype/noto/NotoSansCJKsc-Regular.ttc",
                "/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.ttf",
                "/usr/share/fonts/truetype/noto/NotoSansCJKsc-Regular.ttf",
                "/usr/share/fonts/chinese/NotoSansCJK-Regular.otf",
                "/usr/share/fonts/truetype/wqy/wqy-microhei.ttc",
                "/usr/share/fonts/truetype/wqy/wqy-microhei.ttf",
                "/usr/share/fonts/truetype/arphic/uming.ttc",
                "/usr/share/fonts/truetype/arphic/ukai.ttc"
        };
        for (String path : candidates) {
            java.nio.file.Path p = java.nio.file.Paths.get(path);
            if (java.nio.file.Files.exists(p)) {
                try {
                    String lower = path.toLowerCase(java.util.Locale.ROOT);
                    if (lower.endsWith(".ttc")) {
                        try (java.io.InputStream in = java.nio.file.Files.newInputStream(p);
                             org.apache.fontbox.ttf.TrueTypeCollection ttc = new org.apache.fontbox.ttf.TrueTypeCollection(in)) {
                            final org.apache.fontbox.ttf.TrueTypeFont[] chosen = new org.apache.fontbox.ttf.TrueTypeFont[1];
                            // 优先选择包含 glyf 表的 TrueType 子字体（避免 CFF 子字体）
                            ttc.processAllFonts(ttf -> {
                                try {
                                    if (chosen[0] == null) {
                                        org.apache.fontbox.ttf.GlyphTable glyf = ttf.getGlyph();
                                        if (glyf != null) {
                                            chosen[0] = ttf;
                                        }
                                    }
                                } catch (Throwable ignored2) {
                                }
                            });
                            if (chosen[0] != null) {
                                org.apache.pdfbox.pdmodel.font.PDFont f = PDType0Font.load(doc, chosen[0], true);
                                log.info("Loaded CJK font from TTC (glyf subfont): {}", path);
                                return f;
                            } else {
                                log.warn("No TrueType glyf subfont found in TTC: {}", path);
                            }
                        } catch (IOException eTtc) {
                            log.warn("Load TTC failed for {}: {}", path, String.valueOf(eTtc.getMessage()));
                        }
                    } else {
                        try {
                            org.apache.pdfbox.pdmodel.font.PDFont f = PDType0Font.load(doc, java.nio.file.Files.newInputStream(p), false);
                            log.info("Loaded CJK font from system path (no subset): {}", path);
                            return f;
                        } catch (IOException e1) {
                            log.warn("System font load (no subset) failed for {}: {}", path, String.valueOf(e1.getMessage()));
                            org.apache.pdfbox.pdmodel.font.PDFont f2 = PDType0Font.load(doc, java.nio.file.Files.newInputStream(p), true);
                            log.info("Loaded CJK font from system path (subset): {}", path);
                            return f2;
                        }
                    }
                } catch (IOException ignored) {
                    // try next
                }
            }
        }

        // 3) 未找到可用中文字体：兜底回退内置 Helvetica（仅英文），避免抛错中断
        log.warn("No CJK font found. Falling back to Helvetica (will render non-ASCII as '?'). Ensure fonts/NotoSansCJK-Regular.otf is on classpath.");
        return org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA;
    }

    private void drawCell(PDPageContentStream cs, float x, float y, float width, float height, boolean withGrid) throws IOException {
        // 外框
        cs.addRect(x, y, width, height);
        cs.setLineWidth(0.8f);
        cs.stroke();

        if (!withGrid) {
            return; // 仅外框，不画四线三格（用于中文）
        }

        // 四线三格：等分为三段，需 4 条横线
        float step = height / 3f;
        float y1 = y;
        float y2 = y + step;
        float y3 = y + step * 2f;
        float y4 = y + height;

        // 顶/底为实线
        cs.moveTo(x, y4); cs.lineTo(x + width, y4); cs.stroke();
        cs.moveTo(x, y1); cs.lineTo(x + width, y1); cs.stroke();

        // 中两条使用虚线
        cs.setLineDashPattern(new float[]{3f, 3f}, 0);
        cs.moveTo(x, y2); cs.lineTo(x + width, y2); cs.stroke();
        cs.moveTo(x, y3); cs.lineTo(x + width, y3); cs.stroke();
        cs.setLineDashPattern(new float[]{}, 0);
    }

    private void drawTextInCellAutoFit(PDPageContentStream cs,
                                        org.apache.pdfbox.pdmodel.font.PDFont font,
                                        float preferredFontSize,
                                        float minFontSize,
                                        float x, float y, float width, float height,
                                        String text) throws IOException {
        float padding = 6f;
        float maxWidth = Math.max(1f, width - padding * 2f);
        float fontSize = preferredFontSize;
        // 根据可用宽度缩放字号
        float textWidth = 0;
        String safe = sanitizeTextForFont(font, text);
        while (true) {
            try {
                textWidth = font.getStringWidth(safe) / 1000f * fontSize;
            } catch (IllegalArgumentException ex) {
                // 字体不支持该字符：使用已做过过滤的 safe 文本继续
                break;
            }
            if (textWidth <= maxWidth || fontSize <= minFontSize) break;
            fontSize -= 1f;
        }
        float textX = x + Math.max(padding, (width - Math.min(textWidth, maxWidth)) / 2f);
        float textY = y + (height - fontSize) / 2f + 2f;
        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(textX, textY);
        cs.showText(safe);
        cs.endText();
    }

    private String sanitizeTextForFont(org.apache.pdfbox.pdmodel.font.PDFont font, String text) {
        if (text == null) return "";
        // 若为支持 CJK 的 Type0 字体，直接返回
        if (font instanceof org.apache.pdfbox.pdmodel.font.PDType0Font) {
            return text;
        }
        // 内置 Helvetica 等 Type1 字体不支持中文：过滤成可打印 ASCII，其他替换为 '?'
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= 32 && c <= 126) { // 基本可打印 ASCII
                sb.append(c);
            } else {
                sb.append('?');
            }
        }
        return sb.toString();
    }
}


