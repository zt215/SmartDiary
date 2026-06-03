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
    
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> credentials) {
        String phone = credentials.get("account");
        String password = credentials.get("password");
        return userService.login(phone, password);
    }

    @PostMapping("/forgotpassword")
    public Map<String, Object> forgotPassword(@RequestBody Map<String, String> requestData) {
        String phone = requestData.get("phone");
        String newPassword = requestData.get("newPassword");
        String verificationCode = requestData.get("verificationCode");
        return userService.forgotPassword(phone, newPassword, verificationCode);
    }

    @PostMapping("/send-verification-code")
    public Map<String, Object> sendVerificationCode(@RequestBody Map<String, String> requestData) {
        String phone = requestData.get("phone");
        return userService.sendVerificationCode(phone);
    }

    @PostMapping("/check-phone")
    public Map<String, Object> checkPhoneExists(@RequestBody Map<String, String> requestData) {
        String phone = requestData.get("phone");
        return userService.checkPhoneExists(phone);
    }

    @PostMapping("/verify-code")
    public Map<String, Object> verifyCode(@RequestBody Map<String, String> requestData) {
        String phone = requestData.get("phone");
        String code = requestData.get("verificationCode");
        return userService.verifyCode(phone, code);
    }
    
    @PutMapping("/update")
    public Map<String, Object> updateUser(@RequestBody User user) {
        return userService.updateUser(user);
    }

    @PutMapping("/change-password")
    public Map<String, Object> changePassword(@RequestBody Map<String, Object> requestData) {
        Object userIdObj = requestData.get("userId");
        Integer userId = userIdObj instanceof Number ? ((Number) userIdObj).intValue() : null;
        String oldPassword = (String) requestData.get("oldPassword");
        String newPassword = (String) requestData.get("newPassword");
        return userService.changePassword(userId, oldPassword, newPassword);
    }
    
    @PutMapping("/update-theme")
    public Map<String, Object> updateTheme(@RequestBody Map<String, Object> data) {
        Integer userId = (Integer) data.get("userId");
        String theme = (String) data.get("theme");
        return userService.updateTheme(userId, theme);
    }
    
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteUser(@PathVariable("id") Integer id) {
        return userService.deleteUser(id);
    }
}
