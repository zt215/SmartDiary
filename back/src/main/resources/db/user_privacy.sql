-- 用户隐私设置：手机号搜索开关、手机号/邮箱对他人隐藏
ALTER TABLE `user`
  ADD COLUMN `allow_phone_search` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否允许通过手机号被搜索' AFTER `email`,
  ADD COLUMN `hide_phone` tinyint(1) NOT NULL DEFAULT 0 COMMENT '对好友隐藏手机号' AFTER `allow_phone_search`,
  ADD COLUMN `hide_email` tinyint(1) NOT NULL DEFAULT 0 COMMENT '对好友隐藏邮箱' AFTER `hide_phone`;
