package org.example.back.util;

import org.example.back.pojo.User;

/**
 * 根据用户隐私设置，对返回给其他人的资料做脱敏。
 */
public final class UserPrivacyUtil {

    private UserPrivacyUtil() {
    }

    public static boolean allowPhoneSearch(User user) {
        if (user == null) {
            return false;
        }
        return user.getAllowPhoneSearch() == null || Boolean.TRUE.equals(user.getAllowPhoneSearch());
    }

    public static User maskForViewer(User user) {
        if (user == null) {
            return null;
        }
        user.setPassword(null);
        if (Boolean.TRUE.equals(user.getHidePhone())) {
            user.setPhone(null);
        }
        if (Boolean.TRUE.equals(user.getHideEmail())) {
            user.setEmail(null);
        }
        return user;
    }

    public static String maskPhoneInBrief(String phone, Boolean hidePhone) {
        if (Boolean.TRUE.equals(hidePhone)) {
            return null;
        }
        return phone;
    }
}
