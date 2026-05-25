package org.example.back.pojo;
import lombok.Data;

import java.util.Date;

@Data
public class User {
    private Integer id; // 用户id
    private Long uid; // 账号UID，从10000000起自动分配
    private String name; // 用户名
    private String password; // 密码
    private String phone;  // 手机号字段
    private String email;  // 邮箱（可选）
    private Boolean allowPhoneSearch; // 是否允许通过手机号被搜索，默认 true
    private Boolean hidePhone; // 对好友隐藏手机号
    private Boolean hideEmail; // 对好友隐藏邮箱
    private Date birthday;  // 生日字段
    private String address;  // 地址字段
    private String avatar;  // 头像URL字段
    private String theme;  // 主题字段
}
