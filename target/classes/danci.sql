-- 1. 标签表 (tags)
CREATE TABLE `tags` (
                        `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
                        `tag_name` VARCHAR(100) NOT NULL UNIQUE COMMENT '标签名称，例如：CET-4, 专业词汇',
                        `description` VARCHAR(255) COMMENT '标签描述，可选',
                        `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单词标签表';

-- 添加索引，提高按标签名查询效率
CREATE INDEX idx_tag_name ON `tags` (`tag_name`);


-- 2. 单词表 (words)
CREATE TABLE `words` (
                         `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
                         `english` VARCHAR(255) NOT NULL COMMENT '英文单词',
                         `chinese` VARCHAR(500) NOT NULL COMMENT '中文释义',
    -- `tag_id` BIGINT COMMENT '单标签外键，如果一个单词只属于一个标签，可以使用此字段',
                         `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
    -- 如果使用单标签模式，需要添加外键约束
    -- , FOREIGN KEY (`tag_id`) REFERENCES `tags`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单词信息表';

-- 添加索引，提高按英文或中文查询效率
CREATE INDEX idx_english ON `words` (`english`);
CREATE INDEX idx_chinese ON `words` (`chinese`(255)); -- 中文释义可能较长，可以只对部分进行索引

-- 3. 单词-标签关系表 (word_tag_rel) - 用于处理多对多关系
-- 一个单词可以有多个标签，一个标签可以包含多个单词
CREATE TABLE `word_tag_rel` (
                                `word_id` BIGINT NOT NULL COMMENT '单词ID',
                                `tag_id` BIGINT NOT NULL COMMENT '标签ID',
                                `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                PRIMARY KEY (`word_id`, `tag_id`), -- 联合主键，确保唯一性
                                FOREIGN KEY (`word_id`) REFERENCES `words`(`id`) ON DELETE CASCADE, -- 级联删除，删除单词时，其关系也删除
                                FOREIGN KEY (`tag_id`) REFERENCES `tags`(`id`) ON DELETE CASCADE -- 级联删除，删除标签时，其关系也删除
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单词与标签关系表';

-- 添加索引，提高查询效率
CREATE INDEX idx_wtr_word_id ON `word_tag_rel` (`word_id`);
CREATE INDEX idx_wtr_tag_id ON `word_tag_rel` (`tag_id`);