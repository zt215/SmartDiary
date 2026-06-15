package org.example.back.service;

import org.example.back.pojo.AdminUserUpdateRequest;

import java.util.Map;

public interface EnforcerService {

    Map<String, Object> login(String account, String password);

    Map<String, Object> forgotPassword(String phone, String newPassword, String verificationCode);

    Map<String, Object> sendVerificationCode(String phone);

    Map<String, Object> checkPhoneExists(String phone);

    Map<String, Object> verifyCode(String phone, String verificationCode);

    Map<String, Object> listUsers(Integer enforcerId);

    Map<String, Object> listDiaryCircles(Integer enforcerId, Integer page, Integer pageSize, String keyword);

    Map<String, Object> cancelUser(Integer enforcerId, Integer userId);

    Map<String, Object> banUser(Integer enforcerId, Integer userId, Integer durationDays,
                                String reason, String customReason);

    Map<String, Object> updateUser(Integer enforcerId, Integer userId, AdminUserUpdateRequest request);

    Map<String, Object> unbanUser(Integer enforcerId, Integer userId);

    Map<String, Object> deleteDiaryCircle(Integer enforcerId, Integer circleId);

    Map<String, Object> hideDiaryCircle(Integer enforcerId, Integer circleId);

    Map<String, Object> showDiaryCircle(Integer enforcerId, Integer circleId);
}
