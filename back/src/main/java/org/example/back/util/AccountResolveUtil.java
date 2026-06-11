package org.example.back.util;

import org.springframework.util.StringUtils;

/**
 * 登录账号解析：手机号 / UID / 邮箱
 */
public final class AccountResolveUtil {

    private AccountResolveUtil() {
    }

    public enum AccountKind {
        PHONE, UID, EMAIL, UNKNOWN
    }

    public static AccountKind classify(String account) {
        if (!StringUtils.hasText(account)) {
            return AccountKind.UNKNOWN;
        }
        String s = account.trim();
        if (s.contains("@")) {
            return AccountKind.EMAIL;
        }
        if (s.matches("^\\d{11}$")) {
            return AccountKind.PHONE;
        }
        if (s.matches("^\\d+$")) {
            return AccountKind.UID;
        }
        return AccountKind.UNKNOWN;
    }
}
