-- 评论回复：记录被回复用户，用于展示「A 回复 B」
ALTER TABLE `comment`
  ADD COLUMN `reply_to_user_id` int NULL DEFAULT NULL COMMENT '被回复用户 ID' AFTER `parent_id`,
  ADD INDEX `idx_reply_to_user_id` (`reply_to_user_id`);
