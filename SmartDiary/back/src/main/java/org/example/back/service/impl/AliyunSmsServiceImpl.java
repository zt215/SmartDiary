package org.example.back.service.impl;

import org.example.back.service.SmsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 模拟阿里云短信服务实现
 * 实际项目中可以集成真实的阿里云短信服务SDK
 */
@Service
public class AliyunSmsServiceImpl implements SmsService {

    /**
     * 验证码存储结构
     */
    private static class CodeEntry {
        private final String code;
        private final long expireAt;
        private final long lastSendAt;

        CodeEntry(String code, long expireAt, long lastSendAt) {
            this.code = code;
            this.expireAt = expireAt;
            this.lastSendAt = lastSendAt;
        }
    }

    // 存储验证码的临时缓存（生产环境建议使用 Redis 等带过期时间的缓存）
    private static final Map<String, CodeEntry> verificationCodes = new ConcurrentHashMap<>();

    @Value("${sms.debug:true}")
    private boolean debugMode;

    @Value("${sms.debug-code:123456}")
    private String debugCode;

    @Value("${sms.code.expire-seconds:300}")
    private long codeExpireSeconds;

    @Value("${sms.code.send-interval-seconds:60}")
    private long sendIntervalSeconds;

    // 真实短信SDK配置（留空则提示未配置）
    @Value("${sms.access-key-id:}")
    private String accessKeyId;

    @Value("${sms.access-key-secret:}")
    private String accessKeySecret;

    @Value("${sms.sign-name:}")
    private String signName;

    @Value("${sms.template-code:}")
    private String templateCode;

    @Value("${sms.region-id:cn-hangzhou}")
    private String regionId;

    /**
     * 发送短信验证码（包含调试模式、频率限制、过期时间、随机验证码）
     */
    @Override
    public String sendVerificationCode(String phone) {
        long now = Instant.now().toEpochMilli();

        // 频率限制
        CodeEntry existing = verificationCodes.get(phone);
        if (existing != null && now - existing.lastSendAt < sendIntervalSeconds * 1000) {
            throw new IllegalStateException("发送过于频繁，请稍后再试");
        }

        // 生成验证码：调试模式固定，非调试模式随机6位
        String code = debugMode ? debugCode : generateRandomCode(6);

        // 计算过期时间
        long expireAt = now + codeExpireSeconds * 1000;
        verificationCodes.put(phone, new CodeEntry(code, expireAt, now));

        if (debugMode) {
            // 调试模式不真实发送，直接日志输出
            System.out.println("【调试模式】验证码未发送，手机号 " + phone + " ，验证码: " + code);
        } else {
            // 真实短信发送前的配置校验
            if (!isSdkConfigured()) {
                throw new IllegalStateException("短信服务未配置，请在 application.properties 填写 sms.access-key-id / sms.access-key-secret / sms.sign-name / sms.template-code");
            }

            // TODO: 在此集成真实短信服务 SDK（阿里云/腾讯云等）
            // 示例代码（需替换为实际 SDK 调用）：
            // SendSmsRequest request = new SendSmsRequest();
            // request.setSignName(signName);
            // request.setTemplateCode(templateCode);
            // request.setPhoneNumbers(phone);
            // request.setTemplateParam("{\"code\":\"" + code + "\"}");
            // request.setRegionId(regionId);
            // smsClient.sendSms(request);

            // 目前先日志输出，确认配置后再接入 SDK
            System.out.println("【待接入真实短信】向手机号 " + phone + " 发送验证码: " + code +
                    "，signName=" + signName + "，templateCode=" + templateCode + "，regionId=" + regionId);
        }
        return code;
    }

    /**
     * 验证验证码是否正确且未过期，通过后移除
     */
    @Override
    public boolean verifyCode(String phone, String code) {
        long now = Instant.now().toEpochMilli();
        CodeEntry entry = verificationCodes.get(phone);

        if (entry == null) {
            return false;
        }
        // 过期判断
        if (now > entry.expireAt) {
            verificationCodes.remove(phone);
            return false;
        }
        // 校验验证码
        boolean ok = entry.code.equals(code);
        if (ok) {
            // 验证成功后移除，防止复用
            verificationCodes.remove(phone);
        }
        return ok;
    }

    /**
     * 移除验证码，供密码重置等流程复用
     */
    @Override
    public void removeCode(String phone) {
        verificationCodes.remove(phone);
    }

    private boolean isSdkConfigured() {
        return isNotBlank(accessKeyId) && isNotBlank(accessKeySecret)
                && isNotBlank(signName) && isNotBlank(templateCode);
    }

    private boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }

    private String generateRandomCode(int length) {
        int bound = (int) Math.pow(10, length);
        int codeNum = ThreadLocalRandom.current().nextInt(bound);
        return String.format("%0" + length + "d", codeNum);
    }
}