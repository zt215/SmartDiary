-- 允许通过邮箱被搜索（默认开启）
ALTER TABLE `user`
  ADD COLUMN `allow_email_search` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否允许通过邮箱被搜索' AFTER `allow_phone_search`;
