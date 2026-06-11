package org.example.back.controller;

import org.example.back.service.EnforcerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class EnforcerController {

    @Autowired
    private EnforcerService enforcerService;

    @PostMapping("/enforcer/auth/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        return enforcerService.login(body.get("account"), body.get("password"));
    }

    @PostMapping("/enforcer/auth/forgotpassword")
    public Map<String, Object> forgotPassword(@RequestBody Map<String, String> body) {
        return enforcerService.forgotPassword(
                body.get("phone"),
                body.get("newPassword"),
                body.get("verificationCode"));
    }

    @PostMapping("/enforcer/auth/send-verification-code")
    public Map<String, Object> sendVerificationCode(@RequestBody Map<String, String> body) {
        return enforcerService.sendVerificationCode(body.get("phone"));
    }

    @PostMapping("/enforcer/auth/check-phone")
    public Map<String, Object> checkPhone(@RequestBody Map<String, String> body) {
        return enforcerService.checkPhoneExists(body.get("phone"));
    }

    @PostMapping("/enforcer/auth/verify-code")
    public Map<String, Object> verifyCode(@RequestBody Map<String, String> body) {
        return enforcerService.verifyCode(body.get("phone"), body.get("verificationCode"));
    }

    @GetMapping("/enforcer/admin/users")
    public Map<String, Object> listUsers(@RequestParam("enforcerId") Integer enforcerId) {
        return enforcerService.listUsers(enforcerId);
    }

    @GetMapping("/enforcer/admin/diary-circles")
    public Map<String, Object> listDiaryCircles(
            @RequestParam("enforcerId") Integer enforcerId,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
        return enforcerService.listDiaryCircles(enforcerId, page, pageSize);
    }
}
