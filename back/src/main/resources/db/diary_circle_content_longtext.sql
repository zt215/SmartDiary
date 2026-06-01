-- 字迹圈 / 日记正文支持富文本内嵌图片（Base64），需 LONGTEXT
-- 在 smartdiary 库执行一次即可

ALTER TABLE `diary_circle`
  MODIFY COLUMN `content` LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '动态内容';

ALTER TABLE `diary`
  MODIFY COLUMN `content` LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '内容';
