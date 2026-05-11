package org.example.back.pojo;

import lombok.Data;
import java.util.Date;

/**
 * 字迹圈动态实体类
 */
@Data
public class DiaryCircle {
    private Integer id; // 动态 ID
    private Integer userId; // 用户 ID
    private String content; // 内容
    private Date createTime; // 创建时间
    private Date updateTime; // 更新时间
    private Integer likeCount; // 点赞数
    private Integer commentCount; // 评论数
    
    // 关联用户信息（非数据库字段）
    private String userName; // 用户名
    private String userAvatar; // 用户头像
    
    // 当前用户是否已点赞（非数据库字段）
    private Boolean isLiked; // 当前用户是否已点赞
}
