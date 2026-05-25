-- 用户表增加账号 UID（从 10000000 起）与可选邮箱
-- 在 smartdiary 库执行一次即可

ALTER TABLE `user`
  ADD COLUMN `uid` BIGINT NULL COMMENT '账号UID' AFTER `id`,
  ADD COLUMN `email` VARCHAR(255) NULL COMMENT '邮箱' AFTER `phone`;

-- 为已有用户按 id 顺序分配 UID
SET @uid_seq := 9999999;
UPDATE `user` SET `uid` = (@uid_seq := @uid_seq + 1) WHERE `uid` IS NULL ORDER BY `id`;

ALTER TABLE `user`
  MODIFY COLUMN `uid` BIGINT NOT NULL COMMENT '账号UID',
  ADD UNIQUE INDEX `uk_user_uid` (`uid` ASC);
