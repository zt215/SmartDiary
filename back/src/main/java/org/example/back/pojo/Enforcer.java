package org.example.back.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class Enforcer {
    private Integer id;
    private Long uid;
    private String name;
    private String password;
    private String phone;
    private String email;
    private Date createTime;
    private Date updateTime;
}
