-- 移除冗余计数字段（点赞/评论数改由关系表实时 COUNT）
-- 在 smartdiary 库执行一次即可

ALTER TABLE `diary_circle`
  DROP COLUMN `like_count`,
  DROP COLUMN `comment_count`;

ALTER TABLE `comment`
  DROP COLUMN `like_count`;
