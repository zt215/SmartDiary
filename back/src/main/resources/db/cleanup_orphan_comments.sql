-- 清理「父评论已删、子回复仍残留」的脏数据（执行一次即可）

DELETE cl FROM comment_like cl
INNER JOIN comment c ON cl.comment_id = c.id
WHERE c.parent_id IS NOT NULL
  AND c.parent_id NOT IN (SELECT id FROM (SELECT id FROM comment) AS t);

DELETE FROM comment
WHERE parent_id IS NOT NULL
  AND parent_id NOT IN (SELECT id FROM (SELECT id FROM comment) AS t);
