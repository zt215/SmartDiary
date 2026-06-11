package org.example.back.service;

import java.util.Map;

public interface EnforcerService {

    Map<String, Object> login(String account, String password);

    Map<String, Object> forgotPassword(String phone, String newPassword, String verificationCode);

    Map<String, Object> sendVerificationCode(String phone);

    Map<String, Object> checkPhoneExists(String phone);

    Map<String, Object> verifyCode(String phone, String verificationCode);

    Map<String, Object> listUsers(Integer enforcerId);

    Map<String, Object> listDiaryCircles(Integer enforcerId, Integer page, Integer pageSize);
}
