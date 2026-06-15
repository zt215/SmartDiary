package org.example.back.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class AdminUserUpdateRequest {
    private String name;
    private String email;
    private Date birthday;
    private String address;
    private Boolean allowPhoneSearch;
    private Boolean allowEmailSearch;
    private Boolean hidePhone;
    private Boolean hideEmail;
}
