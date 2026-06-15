package org.example.back.service;

import org.example.back.pojo.UserBan;

import java.util.Map;

public interface UserBanService {

    UserBan getActiveBan(Integer userId);

    Map<String, Object> buildBanBlockResult(UserBan ban);

    Map<String, Object> buildBanBlockResultIfBanned(Integer userId);
}
