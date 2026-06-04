package org.example.back.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class DiaryDraft {
    private Integer id;
    private Integer userId;
    private String title;
    private String content;
    /** 用户选择的日记日期，格式 YYYY-MM-DD */
    private String diaryDate;
    private Date updateTime;
}
