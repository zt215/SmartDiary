-- 动态隐藏字段
ALTER TABLE `diary_circle`
  ADD COLUMN `hidden` tinyint NOT NULL DEFAULT 0 COMMENT '0正常 1隐藏' AFTER `content`;
