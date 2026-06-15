package org.example.back.service.impl;

import org.example.back.mapper.UserBanMapper;
import org.example.back.pojo.UserBan;
import org.example.back.service.UserBanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserBanServiceImpl implements UserBanService {

    @Autowired
    private UserBanMapper userBanMapper;

    @Override
    public UserBan getActiveBan(Integer userId) {
        if (userId == null) {
            return null;
        }
        return userBanMapper.findActiveByUserId(userId);
    }

    @Override
    public Map<String, Object> buildBanBlockResult(UserBan ban) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("code", "USER_BANNED");
        result.put("message", "账号已被封禁，暂无法发布动态或评论");

        Map<String, Object> banInfo = new HashMap<>();
        banInfo.put("endTime", ban.getEndTime());
        banInfo.put("reason", ban.getReason());
        banInfo.put("customReason", ban.getCustomReason());
        String detail = ban.getReason();
        if (StringUtils.hasText(ban.getCustomReason())) {
            detail = detail + "：" + ban.getCustomReason();
        }
        banInfo.put("reasonText", detail);
        result.put("banInfo", banInfo);
        return result;
    }

    @Override
    public Map<String, Object> buildBanBlockResultIfBanned(Integer userId) {
        UserBan ban = getActiveBan(userId);
        if (ban == null) {
            return null;
        }
        return buildBanBlockResult(ban);
    }
}
