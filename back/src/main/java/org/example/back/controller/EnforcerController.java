package org.example.back.controller;

import org.example.back.pojo.AdminUserUpdateRequest;
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
            @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
            @RequestParam(value = "keyword", required = false) String keyword) {
        return enforcerService.listDiaryCircles(enforcerId, page, pageSize, keyword);
    }

    @PostMapping("/enforcer/admin/users/{userId}/cancel")
    public Map<String, Object> cancelUser(
            @RequestParam("enforcerId") Integer enforcerId,
            @PathVariable("userId") Integer userId) {
        return enforcerService.cancelUser(enforcerId, userId);
    }

    @PostMapping("/enforcer/admin/users/{userId}/ban")
    public Map<String, Object> banUser(
            @PathVariable("userId") Integer userId,
            @RequestBody Map<String, Object> body) {
        Integer enforcerId = body.get("enforcerId") instanceof Number n ? n.intValue() : null;
        Integer durationDays = body.get("durationDays") instanceof Number n ? n.intValue() : null;
        String reason = (String) body.get("reason");
        String customReason = (String) body.get("customReason");
        return enforcerService.banUser(enforcerId, userId, durationDays, reason, customReason);
    }

    @PostMapping("/enforcer/admin/users/{userId}/unban")
    public Map<String, Object> unbanUser(
            @PathVariable("userId") Integer userId,
            @RequestParam("enforcerId") Integer enforcerId) {
        return enforcerService.unbanUser(enforcerId, userId);
    }

    @PostMapping("/enforcer/admin/diary-circles/{circleId}/delete")
    public Map<String, Object> deleteDiaryCircle(
            @PathVariable("circleId") Integer circleId,
            @RequestParam("enforcerId") Integer enforcerId) {
        return enforcerService.deleteDiaryCircle(enforcerId, circleId);
    }

    @PostMapping("/enforcer/admin/diary-circles/{circleId}/hide")
    public Map<String, Object> hideDiaryCircle(
            @PathVariable("circleId") Integer circleId,
            @RequestParam("enforcerId") Integer enforcerId) {
        return enforcerService.hideDiaryCircle(enforcerId, circleId);
    }

    @PostMapping("/enforcer/admin/diary-circles/{circleId}/show")
    public Map<String, Object> showDiaryCircle(
            @PathVariable("circleId") Integer circleId,
            @RequestParam("enforcerId") Integer enforcerId) {
        return enforcerService.showDiaryCircle(enforcerId, circleId);
    }

    @PutMapping("/enforcer/admin/users/{userId}")
    public Map<String, Object> updateUser(
            @PathVariable("userId") Integer userId,
            @RequestBody Map<String, Object> body) {
        Integer enforcerId = body.get("enforcerId") instanceof Number n ? n.intValue() : null;
        AdminUserUpdateRequest req = new AdminUserUpdateRequest();
        req.setName((String) body.get("name"));
        req.setEmail((String) body.get("email"));
        if (body.get("birthday") != null) {
            req.setBirthday(java.sql.Date.valueOf(String.valueOf(body.get("birthday"))));
        }
        req.setAddress((String) body.get("address"));
        req.setAllowPhoneSearch((Boolean) body.get("allowPhoneSearch"));
        req.setAllowEmailSearch((Boolean) body.get("allowEmailSearch"));
        req.setHidePhone((Boolean) body.get("hidePhone"));
        req.setHideEmail((Boolean) body.get("hideEmail"));
        return enforcerService.updateUser(enforcerId, userId, req);
    }
}
