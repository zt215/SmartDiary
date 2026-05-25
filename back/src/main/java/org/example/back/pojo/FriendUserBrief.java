package org.example.back.pojo;

import java.util.Date;

/**
 * 好友/申请人简要信息（不含密码）
 */
public class FriendUserBrief {
    private Integer id;
    private String name;
    private String phone;
    private String avatar;
    /** 关联的好友申请 id（列表接口使用） */
    private Integer requestId;
    /** 好友最新字迹圈发布时间 */
    private Date latestDiaryTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    public Date getLatestDiaryTime() {
        return latestDiaryTime;
    }

    public void setLatestDiaryTime(Date latestDiaryTime) {
        this.latestDiaryTime = latestDiaryTime;
    }
}
