-- 用户封禁记录表
CREATE TABLE IF NOT EXISTS `user_ban` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '封禁记录ID',
  `user_id` int NOT NULL COMMENT '被封禁用户ID',
  `enforcer_id` int NULL DEFAULT NULL COMMENT '操作执法者ID',
  `reason` varchar(64) NOT NULL COMMENT '封禁理由',
  `custom_reason` varchar(500) NULL DEFAULT NULL COMMENT '自定义理由',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user_ban_user`(`user_id`),
  INDEX `idx_user_ban_end`(`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户封禁表';

-- 用户账户状态：0 正常 1 已注销（若列已存在可跳过）
ALTER TABLE `user`
  ADD COLUMN `account_status` tinyint NOT NULL DEFAULT 0 COMMENT '0正常 1已注销' AFTER `theme`;
