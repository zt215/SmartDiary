package org.example.back.pojo;

import lombok.Data;
import java.util.Date;

/**
 * 动态点赞记录实体类
 */
@Data
public class DiaryCircleLike {
    private Integer id; // 记录 ID
    private Integer diaryCircleId; // 动态 ID
    private Integer userId; // 用户 ID
    private Date createTime; // 点赞时间
}
