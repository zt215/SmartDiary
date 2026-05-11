package org.example.back.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Date;

/**
 * 评论实体类
 */
@Data
public class Comment {
    private Integer id; // 评论 ID
    private Integer circleId; // 动态 ID
    private Integer userId; // 用户 ID
    private String content; // 评论内容
    private Integer parentId; // 父评论 ID（用于回复）
    private Integer likeCount; // 点赞数
    private Date createTime; // 创建时间
    private Date updateTime; // 更新时间
    
    // 关联用户信息（非数据库字段）
    private String userName; // 用户名
    private String userAvatar; // 用户头像
    
    // 当前用户是否已点赞（非数据库字段）
    @JsonProperty("isLiked")
    private Boolean isLiked; // 当前用户是否已点赞
    
    // 回复列表（用于嵌套评论）
    private java.util.List<Comment> replies;
}
