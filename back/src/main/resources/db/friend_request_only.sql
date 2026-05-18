-- 若数据库已存在且未执行完整 smartdiary.sql，可单独执行本段创建好友表
CREATE TABLE IF NOT EXISTS `friend_request`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `from_user_id` int NOT NULL COMMENT '申请人',
  `to_user_id` int NOT NULL COMMENT '被申请人',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0待处理 1已同意 2已拒绝',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_from_to`(`from_user_id` ASC, `to_user_id` ASC) USING BTREE,
  INDEX `idx_to_status`(`to_user_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_from_status`(`from_user_id` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '好友申请表' ROW_FORMAT = Dynamic;
