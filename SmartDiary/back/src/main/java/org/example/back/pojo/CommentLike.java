package org.example.back.pojo;

import lombok.Data;
import java.util.Date;

/**
 * 评论点赞记录实体类
 */
@Data
public class CommentLike {
    private Integer id; // 记录 ID
    private Integer commentId; // 评论 ID
    private Integer userId; // 用户 ID
    private Date createTime; // 点赞时间
}
