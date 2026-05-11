
package org.example.back.service;

public interface SmsService {
    /**
     * 发送短信验证码
     * @param phone 手机号
     * @return 验证码
     */
    String sendVerificationCode(String phone);

    /**
     * 校验验证码
     * @param phone 手机号
     * @param code 用户输入验证码
     * @return 是否校验通过
     */
    boolean verifyCode(String phone, String code);

    /**
     * 移除指定手机号的验证码（验证通过后清理）
     */
    void removeCode(String phone);
}