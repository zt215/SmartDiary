package org.example.back.service;

import org.example.back.pojo.User;
import java.util.Map;

public interface UserService {
    Map<String, Object> register(User user);
    
    Map<String, Object> login(String account, String password);

    Map<String, Object> forgotPassword(String phone, String newPassword, String verificationCode);

    Map<String, Object> sendVerificationCode(String phone);
    
    Map<String, Object> checkPhoneExists(String phone);
    
    Map<String, Object> verifyCode(String phone, String verificationCode);
    
    Map<String, Object> updateUser(User user);

    Map<String, Object> changePassword(Integer userId, String oldPassword, String newPassword);
    
    Map<String, Object> updateTheme(Integer userId, String theme);
    
    Map<String, Object> deleteUser(Integer id);
}