package org.example.back.service.impl;

import org.example.back.mapper.DiaryCircleMapper;
import org.example.back.mapper.EnforcerMapper;
import org.example.back.mapper.UserMapper;
import org.example.back.pojo.DiaryCircle;
import org.example.back.pojo.Enforcer;
import org.example.back.service.EnforcerService;
import org.example.back.service.SmsService;
import org.example.back.util.AccountResolveUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EnforcerServiceImpl implements EnforcerService {

    @Autowired
    private EnforcerMapper enforcerMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiaryCircleMapper diaryCircleMapper;

    @Autowired
    private SmsService smsService;

    private String md5(String raw) {
        return DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));
    }

    private Enforcer resolveByAccount(String account) {
        if (!StringUtils.hasText(account)) {
            return null;
        }
        String key = account.trim();
        switch (AccountResolveUtil.classify(key)) {
            case EMAIL -> {
                return enforcerMapper.findByEmail(key);
            }
            case PHONE -> {
                return enforcerMapper.findByPhone(key);
            }
            case UID -> {
                try {
                    return enforcerMapper.findByUid(Long.parseLong(key));
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            default -> {
                Enforcer byPhone = enforcerMapper.findByPhone(key);
                if (byPhone != null) {
                    return byPhone;
                }
                Enforcer byEmail = enforcerMapper.findByEmail(key);
                if (byEmail != null) {
                    return byEmail;
                }
                if (key.matches("^\\d+$")) {
                    try {
                        return enforcerMapper.findByUid(Long.parseLong(key));
                    } catch (NumberFormatException ignored) {
                        return null;
                    }
                }
                return null;
            }
        }
    }

    private Map<String, Object> fail(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        return result;
    }

    private boolean verifyEnforcerSession(Integer enforcerId) {
        return enforcerId != null && enforcerMapper.findById(enforcerId) != null;
    }

    @Override
    public Map<String, Object> login(String account, String password) {
        Map<String, Object> result = new HashMap<>();
        try {
            Enforcer enforcer = resolveByAccount(account);
            if (enforcer == null) {
                return fail("账号不存在");
            }
            if (!enforcer.getPassword().equals(md5(password))) {
                return fail("密码错误");
            }
            enforcer.setPassword(null);
            result.put("success", true);
            result.put("message", "登录成功");
            result.put("data", enforcer);
        } catch (Exception e) {
            return fail("登录异常: " + e.getMessage());
        }
        return result;
    }

    @Override
    public Map<String, Object> forgotPassword(String phone, String newPassword, String verificationCode) {
        Map<String, Object> result = new HashMap<>();
        try {
            Enforcer enforcer = enforcerMapper.findByPhone(phone);
            if (enforcer == null) {
                return fail("该手机号未绑定执法者账号");
            }
            if (!smsService.verifyCode(phone, verificationCode)) {
                return fail("验证码错误或已过期");
            }
            int rows = enforcerMapper.updatePassword(enforcer.getId(), md5(newPassword));
            if (rows > 0) {
                smsService.removeCode(phone);
                result.put("success", true);
                result.put("message", "密码重置成功");
            } else {
                return fail("密码重置失败");
            }
        } catch (Exception e) {
            return fail("重置异常: " + e.getMessage());
        }
        return result;
    }

    @Override
    public Map<String, Object> sendVerificationCode(String phone) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (enforcerMapper.findByPhone(phone) == null) {
                return fail("该手机号未绑定执法者账号");
            }
            String code = smsService.sendVerificationCode(phone);
            result.put("success", true);
            result.put("message", "验证码发送成功");
            result.put("debug", code);
        } catch (IllegalStateException e) {
            return fail(e.getMessage());
        } catch (Exception e) {
            return fail("发送验证码异常: " + e.getMessage());
        }
        return result;
    }

    @Override
    public Map<String, Object> checkPhoneExists(String phone) {
        Map<String, Object> result = new HashMap<>();
        boolean exists = enforcerMapper.findByPhone(phone) != null;
        result.put("success", exists);
        result.put("message", exists ? "手机号已绑定执法者" : "该手机号未绑定执法者账号");
        return result;
    }

    @Override
    public Map<String, Object> verifyCode(String phone, String verificationCode) {
        Map<String, Object> result = new HashMap<>();
        boolean ok = smsService.verifyCode(phone, verificationCode);
        result.put("success", ok);
        result.put("message", ok ? "验证成功" : "验证码错误或已过期");
        return result;
    }

    @Override
    public Map<String, Object> listUsers(Integer enforcerId) {
        Map<String, Object> result = new HashMap<>();
        if (!verifyEnforcerSession(enforcerId)) {
            return fail("请先登录执法者账号");
        }
        try {
            result.put("success", true);
            result.put("message", "查询成功");
            result.put("data", userMapper.selectAllForAdmin());
        } catch (Exception e) {
            return fail("查询异常: " + e.getMessage());
        }
        return result;
    }

    @Override
    public Map<String, Object> listDiaryCircles(Integer enforcerId, Integer page, Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        if (!verifyEnforcerSession(enforcerId)) {
            return fail("请先登录执法者账号");
        }
        try {
            int p = page == null || page < 1 ? 1 : page;
            int size = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 100);
            int offset = (p - 1) * size;
            List<DiaryCircle> list = diaryCircleMapper.selectAllWithUser(offset, size);
            int total = diaryCircleMapper.countAll();
            Map<String, Object> data = new HashMap<>();
            data.put("list", list);
            data.put("total", total);
            data.put("page", p);
            data.put("pageSize", size);
            for (DiaryCircle dc : list) {
                if (dc.getContent() != null && dc.getContent().length() > 200) {
                    dc.setContent(dc.getContent().substring(0, 200) + "…");
                }
            }
            result.put("success", true);
            result.put("message", "查询成功");
            result.put("data", data);
        } catch (Exception e) {
            return fail("查询异常: " + e.getMessage());
        }
        return result;
    }
}
