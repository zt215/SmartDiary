package org.example.back.controller;

import org.example.back.pojo.User;
import org.example.back.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    @Autowired
    private UserService userService;
    
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        return userService.register(user);
    }
    // 登录
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> credentials) {
        String phone = credentials.get("account");
        String password = credentials.get("password");
        return userService.login(phone, password);
    }

    // 忘记密码
    @PostMapping("/forgotpassword")
    public Map<String, Object> forgotPassword(@RequestBody Map<String, String> requestData) {
        String phone = requestData.get("phone");
        String newPassword = requestData.get("newPassword");
        String verificationCode = requestData.get("verificationCode");
        return userService.forgotPassword(phone, newPassword, verificationCode);
    }

    // 发送验证码
    @PostMapping("/send-verification-code")
    public Map<String, Object> sendVerificationCode(@RequestBody Map<String, String> requestData) {
        String phone = requestData.get("phone");
        return userService.sendVerificationCode(phone);
    }

    // 检查手机号是否存在
    @PostMapping("/check-phone")
    public Map<String, Object> checkPhoneExists(@RequestBody Map<String, String> requestData) {
        String phone = requestData.get("phone");
        return userService.checkPhoneExists(phone);
    }

    // 验证验证码
    @PostMapping("/verify-code")
    public Map<String, Object> verifyCode(@RequestBody Map<String, String> requestData) {
        String phone = requestData.get("phone");
        String code = requestData.get("verificationCode");
        return userService.verifyCode(phone, code);
    }
    
    // 更新用户信息
    @PutMapping("/update")
    public Map<String, Object> updateUser(@RequestBody User user) {
        return userService.updateUser(user);
    }

    // 修改密码
    @PutMapping("/change-password")
    public Map<String, Object> changePassword(@RequestBody Map<String, Object> requestData) {
        Object userIdObj = requestData.get("userId");
        Integer userId = userIdObj instanceof Number ? ((Number) userIdObj).intValue() : null;
        String oldPassword = (String) requestData.get("oldPassword");
        String newPassword = (String) requestData.get("newPassword");
        return userService.changePassword(userId, oldPassword, newPassword);
    }
    
    // 更新主题
    @PutMapping("/update-theme")
    public Map<String, Object> updateTheme(@RequestBody Map<String, Object> data) {
        Integer userId = (Integer) data.get("userId");
        String theme = (String) data.get("theme");
        return userService.updateTheme(userId, theme);
    }
    
    // 删除用户
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteUser(@PathVariable("id") Integer id) {
        return userService.deleteUser(id);
    }
}
