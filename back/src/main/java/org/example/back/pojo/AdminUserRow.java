package org.example.back.pojo;

import lombok.Data;

import java.util.Date;

/** 执法端用户列表（不含密码、头像） */
@Data
public class AdminUserRow {
    private Integer id;
    private Long uid;
    private String name;
    private String phone;
    private String email;
    private Date birthday;
    private String address;
    private Integer accountStatus;
    private Boolean banned;
    private Date banEndTime;
    private String banReason;
    private String banCustomReason;
}
