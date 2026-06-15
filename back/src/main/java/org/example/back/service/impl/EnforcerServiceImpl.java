package org.example.back.service.impl;

import org.example.back.mapper.DiaryCircleMapper;
import org.example.back.mapper.EnforcerMapper;
import org.example.back.mapper.UserMapper;
import org.example.back.pojo.DiaryCircle;
import org.example.back.pojo.Enforcer;
import org.example.back.pojo.AdminUserUpdateRequest;
import org.example.back.pojo.User;
import org.example.back.pojo.UserBan;
import org.example.back.mapper.DiaryDraftMapper;
import org.example.back.mapper.DiaryMapper;
import org.example.back.mapper.FriendMapper;
import org.example.back.mapper.UserBanMapper;
import org.example.back.service.EnforcerService;
import org.example.back.service.SmsService;
import org.example.back.util.AccountResolveUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
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
    private DiaryMapper diaryMapper;

    @Autowired
    private DiaryDraftMapper diaryDraftMapper;

    @Autowired
    private FriendMapper friendMapper;

    @Autowired
    private UserBanMapper userBanMapper;

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
    public Map<String, Object> listDiaryCircles(Integer enforcerId, Integer page, Integer pageSize, String keyword) {
        Map<String, Object> result = new HashMap<>();
        if (!verifyEnforcerSession(enforcerId)) {
            return fail("请先登录执法者账号");
        }
        try {
            int p = page == null || page < 1 ? 1 : page;
            int size = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 100);
            int offset = (p - 1) * size;
            boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
            List<DiaryCircle> list;
            int total;
            if (hasKeyword) {
                String kw = keyword.trim();
                list = diaryCircleMapper.selectAllWithUserByKeyword(offset, size, kw);
                total = diaryCircleMapper.countByKeyword(kw);
            } else {
                list = diaryCircleMapper.selectAllWithUser(offset, size);
                total = diaryCircleMapper.countAll();
            }
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

    @Override
    @Transactional
    public Map<String, Object> cancelUser(Integer enforcerId, Integer userId) {
        if (!verifyEnforcerSession(enforcerId)) {
            return fail("请先登录执法者账号");
        }
        User user = userMapper.findById(userId);
        if (user == null) {
            return fail("用户不存在");
        }
        if (user.getAccountStatus() != null && user.getAccountStatus() == 1) {
            return fail("该用户已注销");
        }
        try {
            diaryMapper.deleteByUserId(userId);
            diaryDraftMapper.deleteByUserId(userId);
            friendMapper.deleteByUserId(userId);
            String invalidPassword = md5("cancelled_" + userId + "_" + System.currentTimeMillis());
            userMapper.anonymizeUser(userId, invalidPassword);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "用户已注销，字迹圈动态保留");
            return result;
        } catch (Exception e) {
            return fail("注销异常: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> banUser(Integer enforcerId, Integer userId, Integer durationDays,
                                       String reason, String customReason) {
        if (!verifyEnforcerSession(enforcerId)) {
            return fail("请先登录执法者账号");
        }
        if (userId == null) {
            return fail("用户ID不能为空");
        }
        if (durationDays == null || durationDays < 1 || durationDays > 3650) {
            return fail("封禁时长无效（1天~10年）");
        }
        if (!StringUtils.hasText(reason)) {
            return fail("请选择封禁理由");
        }
        User user = userMapper.findById(userId);
        if (user == null) {
            return fail("用户不存在");
        }
        if (user.getAccountStatus() != null && user.getAccountStatus() == 1) {
            return fail("已注销用户无法封禁");
        }
        try {
            UserBan existingBan = userBanMapper.findActiveByUserId(userId);
            Date now = new Date();
            if (existingBan != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(existingBan.getEndTime());
                cal.add(Calendar.DAY_OF_YEAR, durationDays);
                existingBan.setEndTime(cal.getTime());
                userBanMapper.updateEndTime(existingBan.getId(), existingBan.getEndTime());
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "封禁时间已叠加");
                result.put("data", existingBan);
                return result;
            }
            Calendar cal = Calendar.getInstance();
            cal.setTime(now);
            cal.add(Calendar.DAY_OF_YEAR, durationDays);
            UserBan ban = new UserBan();
            ban.setUserId(userId);
            ban.setEnforcerId(enforcerId);
            ban.setReason(reason.trim());
            ban.setCustomReason(StringUtils.hasText(customReason) ? customReason.trim() : null);
            ban.setStartTime(now);
            ban.setEndTime(cal.getTime());
            ban.setCreateTime(now);
            userBanMapper.insert(ban);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "封禁成功");
            result.put("data", ban);
            return result;
        } catch (Exception e) {
            return fail("封禁异常: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> unbanUser(Integer enforcerId, Integer userId) {
        if (!verifyEnforcerSession(enforcerId)) {
            return fail("请先登录执法者账号");
        }
        if (userId == null) {
            return fail("用户ID不能为空");
        }
        User user = userMapper.findById(userId);
        if (user == null) {
            return fail("用户不存在");
        }
        try {
            UserBan activeBan = userBanMapper.findActiveByUserId(userId);
            if (activeBan == null) {
                return fail("该用户当前未被封禁");
            }
            userBanMapper.deleteActiveByUserId(userId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "已成功解封");
            return result;
        } catch (Exception e) {
            return fail("解封异常: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> updateUser(Integer enforcerId, Integer userId, AdminUserUpdateRequest req) {
        if (!verifyEnforcerSession(enforcerId)) {
            return fail("请先登录执法者账号");
        }
        User old = userMapper.findById(userId);
        if (old == null) {
            return fail("用户不存在");
        }
        if (old.getAccountStatus() != null && old.getAccountStatus() == 1) {
            return fail("已注销用户无法修改");
        }
        if (req == null || !StringUtils.hasText(req.getName())) {
            return fail("昵称不能为空");
        }
        try {
            User existing = userMapper.findByName(req.getName().trim());
            if (existing != null && !existing.getId().equals(userId)) {
                return fail("昵称已被使用");
            }
            User update = new User();
            update.setId(userId);
            update.setName(req.getName().trim());
            update.setEmail(req.getEmail());
            update.setBirthday(req.getBirthday());
            update.setAddress(req.getAddress());
            update.setAllowPhoneSearch(req.getAllowPhoneSearch() != null ? req.getAllowPhoneSearch() : old.getAllowPhoneSearch());
            update.setAllowEmailSearch(req.getAllowEmailSearch() != null ? req.getAllowEmailSearch() : old.getAllowEmailSearch());
            update.setHidePhone(req.getHidePhone() != null ? req.getHidePhone() : old.getHidePhone());
            update.setHideEmail(req.getHideEmail() != null ? req.getHideEmail() : old.getHideEmail());
            userMapper.updateByAdmin(update);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "用户信息已更新");
            return result;
        } catch (Exception e) {
            return fail("修改异常: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> deleteDiaryCircle(Integer enforcerId, Integer circleId) {
        if (!verifyEnforcerSession(enforcerId)) {
            return fail("请先登录执法者账号");
        }
        if (circleId == null) {
            return fail("动态ID不能为空");
        }
        try {
            DiaryCircle dc = diaryCircleMapper.selectById(circleId);
            if (dc == null) {
                return fail("动态不存在");
            }
            diaryCircleMapper.deleteById(circleId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "动态已删除");
            return result;
        } catch (Exception e) {
            return fail("删除异常: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> hideDiaryCircle(Integer enforcerId, Integer circleId) {
        if (!verifyEnforcerSession(enforcerId)) {
            return fail("请先登录执法者账号");
        }
        if (circleId == null) {
            return fail("动态ID不能为空");
        }
        try {
            diaryCircleMapper.hideById(circleId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "动态已隐藏");
            return result;
        } catch (Exception e) {
            return fail("隐藏异常: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> showDiaryCircle(Integer enforcerId, Integer circleId) {
        if (!verifyEnforcerSession(enforcerId)) {
            return fail("请先登录执法者账号");
        }
        if (circleId == null) {
            return fail("动态ID不能为空");
        }
        try {
            diaryCircleMapper.showById(circleId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "动态已恢复显示");
            return result;
        } catch (Exception e) {
            return fail("恢复异常: " + e.getMessage());
        }
    }
}
