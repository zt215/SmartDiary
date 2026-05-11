package org.example.back.pojo;
import lombok.Data;

import java.util.Date;

@Data
public class User {
    private Integer id; // 用户id
    private String name; // 用户名
    private String password; // 密码
    private String phone;  // 手机号字段
    private Date birthday;  // 生日字段
    private String address;  // 地址字段
    private String avatar;  // 头像URL字段
    private String theme;  // 主题字段
}
