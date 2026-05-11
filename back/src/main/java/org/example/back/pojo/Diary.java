package org.example.back.pojo;
import lombok.Data;

import java.util.Date;

@Data
public class Diary {
    private Integer id; // 日记id
    private Integer userId; // 用户id
    private String title; // 标题
    private String content; // 内容
    private Date createTime; // 创建时间
    private Date updateTime; // 更新时间
}
