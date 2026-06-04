-- 日记草稿表（每用户一条，登录后跨设备恢复）
CREATE TABLE IF NOT EXISTS `diary_draft` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '草稿ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '标题',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '内容',
  `diary_date` date NULL DEFAULT NULL COMMENT '用户选择的日记日期',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '日记草稿表' ROW_FORMAT = DYNAMIC;
