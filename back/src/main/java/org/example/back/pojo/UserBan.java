package org.example.back.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class UserBan {
    private Integer id;
    private Integer userId;
    private Integer enforcerId;
    private String reason;
    private String customReason;
    private Date startTime;
    private Date endTime;
    private Date createTime;
}
