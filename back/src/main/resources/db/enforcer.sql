-- 执法者（管理员）账号表
CREATE TABLE IF NOT EXISTS `enforcer` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '执法者ID',
  `uid` bigint NOT NULL COMMENT '执法者UID',
  `name` varchar(64) NOT NULL COMMENT '显示名',
  `password` varchar(255) NOT NULL COMMENT '密码MD5',
  `phone` varchar(20) NOT NULL COMMENT '手机号',
  `email` varchar(255) NULL DEFAULT NULL COMMENT '邮箱',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_enforcer_uid` (`uid`),
  UNIQUE KEY `uk_enforcer_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='执法者账号';

-- 默认账号：手机号 13800000001，密码 123456
INSERT INTO `enforcer` (`uid`, `name`, `password`, `phone`, `email`)
SELECT 90000001, '执法者', 'e10adc3949ba59abbe56e057f20f883e', '13800000001', 'enforcer@smartdiary.local'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `enforcer` WHERE `phone` = '13800000001');
