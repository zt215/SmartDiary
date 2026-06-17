package org.example.back.service.impl;

import org.example.back.mapper.UserMapper;
import org.example.back.pojo.User;
import org.example.back.service.SmsService;
import org.example.back.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private SmsService aliyunSmsService;
    
    private static final long UID_START = 10_000_000L;
    private static final java.util.regex.Pattern EMAIL_PATTERN =
            java.util.regex.Pattern.compile("^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    private static final java.util.regex.Pattern PASSWORD_PATTERN =
            java.util.regex.Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,}$");

    // MD5加密方法
    private String md5(String str) {
        return DigestUtils.md5DigestAsHex(str.getBytes(StandardCharsets.UTF_8));
    }

    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return null;
        }
        return email.trim();
    }

    private String validateEmailOptional(String email) {
        if (email == null) {
            return null;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return "邮箱格式不正确";
        }
        return null;
    }

    private synchronized Long allocateNextUid() {
        Long max = userMapper.selectMaxUid();
        if (max == null || max < UID_START - 1) {
            return UID_START;
        }
        return max + 1;
    }

    private String checkEmailAvailable(String email, Integer excludeUserId) {
        if (email == null) {
            return null;
        }
        User existing = userMapper.findByEmail(email);
        if (existing != null && (excludeUserId == null || !existing.getId().equals(excludeUserId))) {
            return "该邮箱已被使用";
        }
        return null;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> register(User user) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 检查用户名是否已存在
            User existingUser = userMapper.findByName(user.getName());
            if (existingUser != null) {
                result.put("success", false);
                result.put("message", "用户名已存在");
                return result;
            }
            
            // 检查手机号是否已存在
            User existingPhoneUser = userMapper.findByPhone(user.getPhone());
            if (existingPhoneUser != null) {
                result.put("success", false);
                result.put("message", "手机号已被注册");
                return result;
            }

            user.setEmail(normalizeEmail(user.getEmail()));
            String emailError = validateEmailOptional(user.getEmail());
            if (emailError != null) {
                result.put("success", false);
                result.put("message", emailError);
                return result;
            }
            String emailUsed = checkEmailAvailable(user.getEmail(), null);
            if (emailUsed != null) {
                result.put("success", false);
                result.put("message", emailUsed);
                return result;
            }

            user.setUid(allocateNextUid());
            if (user.getAllowPhoneSearch() == null) {
                user.setAllowPhoneSearch(true);
            }
            if (user.getHidePhone() == null) {
                user.setHidePhone(false);
            }
            if (user.getHideEmail() == null) {
                user.setHideEmail(false);
            }

            // 密码加密处理
            user.setPassword(md5(user.getPassword()));

            // 保存用户信息
            int rowsAffected = userMapper.insert(user);
            if (rowsAffected > 0) {
                result.put("success", true);
                result.put("message", "注册成功");
                result.put("data", user);
            } else {
                result.put("success", false);
                result.put("message", "注册失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "注册异常: " + e.getMessage());
        }

        return result;
    }

    private User findUserByAccount(String account) {
        if (account == null || account.trim().isEmpty()) {
            return null;
        }
        String trimmed = account.trim();
        // UID: pure digits ≥ 8 characters
        if (trimmed.matches("^\\d{8,}$")) {
            User user = userMapper.findByUid(Long.parseLong(trimmed));
            if (user != null) return user;
        }
        // Email
        if (EMAIL_PATTERN.matcher(trimmed).matches()) {
            User user = userMapper.findByEmail(trimmed);
            if (user != null) return user;
        }
        // Fallback: phone
        return userMapper.findByPhone(trimmed);
    }

    @Override
    public Map<String, Object> login(String account, String password) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            User user = findUserByAccount(account);
            
            if (user == null) {
                result.put("success", false);
                result.put("message", "账号不存在");
                return result;
            }

            if (user.getAccountStatus() != null && user.getAccountStatus() == 1) {
                result.put("success", false);
                result.put("message", "该账号已注销");
                return result;
            }
            
            // 验证密码（使用MD5加密验证）
            if (!user.getPassword().equals(md5(password))) {
                result.put("success", false);
                result.put("message", "密码错误");
                return result;
            }
            
            // 登录成功
            result.put("success", true);
            result.put("message", "登录成功");
            // 不返回密码信息
            user.setPassword(null);
            result.put("data", user);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "登录异常: " + e.getMessage());
        }
        
        return result;
    }

    @Override
    public Map<String, Object> forgotPassword(String phone, String newPassword, String verificationCode) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 根据手机号查找用户
            User user = userMapper.findByPhone(phone);

            // 检查用户是否存在
            if (user == null) {
                result.put("success", false);
                result.put("message", "手机号未注册");
                return result;
            }

            // 验证验证码
            if (!aliyunSmsService.verifyCode(phone, verificationCode)) {
                result.put("success", false);
                result.put("message", "验证码错误或已过期");
                return result;
            }

            // 更新密码
            user.setPassword(md5(newPassword));
            int rowsAffected = userMapper.update(user);

            if (rowsAffected > 0) {
                // 清除已使用的验证码
                aliyunSmsService.removeCode(phone);
                
                result.put("success", true);
                result.put("message", "密码重置成功");
            } else {
                result.put("success", false);
                result.put("message", "密码重置失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "密码重置异常: " + e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> sendVerificationCode(String phone) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 频率限制 & 生成验证码（调试模式下固定验证码）
            // 检查手机号是否已存在（用于忘记密码场景）
            User user = userMapper.findByPhone(phone);
            if (user == null) {
                // 如果是注册场景，手机号不应该已存在
                // 我们不在这里返回错误，因为这个接口也可能被忘记密码功能调用
            }

            // 调用阿里云短信服务发送验证码
            String code = aliyunSmsService.sendVerificationCode(phone);

            // 返回成功响应
            result.put("success", true);
            result.put("message", "验证码发送成功"); 
            result.put("debug", code); // 便于调试查看（生产可去掉）
        } catch (IllegalStateException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "验证码发送异常: " + e.getMessage());
        }

        return result;
    }
    
    @Override
    public Map<String, Object> checkPhoneExists(String phone) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 检查手机号是否已存在
            User user = userMapper.findByPhone(phone);
            if (user != null) {
                result.put("success", false);
                result.put("message", "手机号已被注册");
            } else {
                result.put("success", true);
                result.put("message", "手机号可以使用");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "检查手机号时发生异常: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> verifyCode(String phone, String verificationCode) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 验证验证码
            if (aliyunSmsService.verifyCode(phone, verificationCode)) {
                result.put("success", true);
                result.put("message", "验证码正确");
            } else {
                result.put("success", false);
                result.put("message", "验证码错误或已过期");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "验证验证码时发生异常: " + e.getMessage());
        }
        
        return result;
    }
    
    private String validateNewPassword(String newPassword) {
        if (!StringUtils.hasText(newPassword)) {
            return "新密码不能为空";
        }
        if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
            return "密码至少8位，且必须包含字母和数字";
        }
        return null;
    }

    @Override
    public Map<String, Object> changePassword(Integer userId, String oldPassword, String newPassword) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (userId == null) {
                result.put("success", false);
                result.put("message", "用户ID不能为空");
                return result;
            }
            if (!StringUtils.hasText(oldPassword)) {
                result.put("success", false);
                result.put("message", "请输入当前密码");
                return result;
            }
            String passwordError = validateNewPassword(newPassword);
            if (passwordError != null) {
                result.put("success", false);
                result.put("message", passwordError);
                return result;
            }
            if (oldPassword.equals(newPassword)) {
                result.put("success", false);
                result.put("message", "新密码不能与当前密码相同");
                return result;
            }

            User user = userMapper.findById(userId);
            if (user == null) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return result;
            }
            if (!user.getPassword().equals(md5(oldPassword))) {
                result.put("success", false);
                result.put("message", "当前密码错误");
                return result;
            }

            user.setPassword(md5(newPassword));
            int rowsAffected = userMapper.update(user);
            if (rowsAffected > 0) {
                result.put("success", true);
                result.put("message", "密码修改成功");
            } else {
                result.put("success", false);
                result.put("message", "密码修改失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "密码修改异常: " + e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> updateUser(User user) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 如果用户名为空，返回错误
            if (user.getName() == null || user.getName().trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "用户名不能为空");
                return result;
            }
            
            // 检查用户名是否被其他用户使用
            User existingUser = userMapper.findByName(user.getName());
            if (existingUser != null && !existingUser.getId().equals(user.getId())) {
                result.put("success", false);
                result.put("message", "用户名已被使用");
                return result;
            }
            
            // 获取原有用户信息以保留密码、UID
            User oldUser = userMapper.findById(user.getId());
            if (oldUser == null) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return result;
            }

            user.setUid(oldUser.getUid());
            user.setEmail(normalizeEmail(user.getEmail()));
            String emailError = validateEmailOptional(user.getEmail());
            if (emailError != null) {
                result.put("success", false);
                result.put("message", emailError);
                return result;
            }
            String emailUsed = checkEmailAvailable(user.getEmail(), user.getId());
            if (emailUsed != null) {
                result.put("success", false);
                result.put("message", emailUsed);
                return result;
            }
            
            if (user.getAllowPhoneSearch() == null) {
                user.setAllowPhoneSearch(oldUser.getAllowPhoneSearch());
            }
            if (user.getAllowEmailSearch() == null) {
                user.setAllowEmailSearch(oldUser.getAllowEmailSearch());
            }
            if (user.getHidePhone() == null) {
                user.setHidePhone(oldUser.getHidePhone());
            }
            if (user.getHideEmail() == null) {
                user.setHideEmail(oldUser.getHideEmail());
            }
            
            // 如果密码为空或未提供，保持原有密码
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                user.setPassword(oldUser.getPassword());
            } else {
                // 如果提供了新密码，需要加密
                user.setPassword(md5(user.getPassword()));
            }
            
            // 更新用户信息
            int rowsAffected = userMapper.update(user);
            if (rowsAffected > 0) {
                result.put("success", true);
                result.put("message", "用户信息更新成功");
                User updated = userMapper.findById(user.getId());
                if (updated != null) {
                    updated.setPassword(null);
                    result.put("data", updated);
                } else {
                    user.setPassword(null);
                    result.put("data", user);
                }
            } else {
                result.put("success", false);
                result.put("message", "用户信息更新失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "用户信息更新异常: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> deleteUser(Integer id) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            int rowsAffected = userMapper.deleteById(id);
            if (rowsAffected > 0) {
                result.put("success", true);
                result.put("message", "用户删除成功");
            } else {
                result.put("success", false);
                result.put("message", "用户删除失败，用户不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "用户删除异常: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> updateTheme(Integer userId, String theme) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 创建临时用户对象，只更新主题
            User user = new User();
            user.setId(userId);
            user.setTheme(theme);
            
            int rowsAffected = userMapper.update(user);
            if (rowsAffected > 0) {
                result.put("success", true);
                result.put("message", "主题更新成功");
            } else {
                result.put("success", false);
                result.put("message", "主题更新失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "主题更新异常: " + e.getMessage());
        }
        
        return result;
    }
}