package org.example.back.service.impl;

import org.example.back.mapper.FriendMapper;
import org.example.back.mapper.UserMapper;
import org.example.back.pojo.FriendRequest;
import org.example.back.pojo.FriendUserBrief;
import org.example.back.pojo.User;
import org.example.back.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.example.back.util.UserPrivacyUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FriendServiceImpl implements FriendService {

    private static final int STATUS_PENDING = 0;
    private static final int STATUS_ACCEPTED = 1;
    private static final int STATUS_REJECTED = 2;

    @Autowired
    private FriendMapper friendMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public Map<String, Object> sendRequest(Integer fromUserId, Integer toUserId) {
        Map<String, Object> result = new HashMap<>();
        if (fromUserId == null || toUserId == null) {
            result.put("success", false);
            result.put("message", "参数不完整");
            return result;
        }
        if (fromUserId.equals(toUserId)) {
            result.put("success", false);
            result.put("message", "不能添加自己为好友");
            return result;
        }
        User target = userMapper.findById(toUserId);
        if (target == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }

        FriendRequest forward = friendMapper.findByFromAndTo(fromUserId, toUserId);
        FriendRequest reverse = friendMapper.findByFromAndTo(toUserId, fromUserId);

        if (forward != null && forward.getStatus() == STATUS_ACCEPTED
                || reverse != null && reverse.getStatus() == STATUS_ACCEPTED) {
            result.put("success", false);
            result.put("message", "你们已经是好友");
            return result;
        }

        if (reverse != null && reverse.getStatus() == STATUS_PENDING) {
            friendMapper.updateStatus(reverse.getId(), STATUS_ACCEPTED);
            result.put("success", true);
            result.put("message", "已同意对方的好友申请，已成为好友");
            return result;
        }

        if (forward != null && forward.getStatus() == STATUS_PENDING) {
            result.put("success", false);
            result.put("message", "好友申请已发送，请等待对方处理");
            return result;
        }

        if (forward != null && forward.getStatus() == STATUS_REJECTED) {
            friendMapper.updateStatus(forward.getId(), STATUS_PENDING);
            result.put("success", true);
            result.put("message", "已重新发送好友申请");
            return result;
        }

        FriendRequest row = new FriendRequest();
        row.setFromUserId(fromUserId);
        row.setToUserId(toUserId);
        row.setStatus(STATUS_PENDING);
        friendMapper.insert(row);
        result.put("success", true);
        result.put("message", "好友申请已发送");
        return result;
    }

    @Override
    public Map<String, Object> accept(Integer userId, Integer requestId) {
        Map<String, Object> result = new HashMap<>();
        if (userId == null || requestId == null) {
            result.put("success", false);
            result.put("message", "参数不完整");
            return result;
        }
        FriendRequest req = friendMapper.findById(requestId);
        if (req == null || req.getStatus() != STATUS_PENDING) {
            result.put("success", false);
            result.put("message", "申请不存在或已处理");
            return result;
        }
        if (!userId.equals(req.getToUserId())) {
            result.put("success", false);
            result.put("message", "无权处理该申请");
            return result;
        }
        friendMapper.updateStatus(requestId, STATUS_ACCEPTED);
        result.put("success", true);
        result.put("message", "已同意好友申请");
        return result;
    }

    @Override
    public Map<String, Object> reject(Integer userId, Integer requestId) {
        Map<String, Object> result = new HashMap<>();
        if (userId == null || requestId == null) {
            result.put("success", false);
            result.put("message", "参数不完整");
            return result;
        }
        FriendRequest req = friendMapper.findById(requestId);
        if (req == null || req.getStatus() != STATUS_PENDING) {
            result.put("success", false);
            result.put("message", "申请不存在或已处理");
            return result;
        }
        if (!userId.equals(req.getToUserId())) {
            result.put("success", false);
            result.put("message", "无权处理该申请");
            return result;
        }
        friendMapper.updateStatus(requestId, STATUS_REJECTED);
        result.put("success", true);
        result.put("message", "已拒绝");
        return result;
    }

    @Override
    public Map<String, Object> listFriends(Integer userId) {
        Map<String, Object> result = new HashMap<>();
        if (userId == null) {
            result.put("success", false);
            result.put("message", "缺少用户");
            return result;
        }
        List<FriendUserBrief> list = friendMapper.listAcceptedFriends(userId);
        result.put("success", true);
        result.put("data", list);
        return result;
    }

    @Override
    public Map<String, Object> listIncoming(Integer userId) {
        Map<String, Object> result = new HashMap<>();
        if (userId == null) {
            result.put("success", false);
            result.put("message", "缺少用户");
            return result;
        }
        result.put("success", true);
        result.put("data", friendMapper.listIncomingPending(userId));
        return result;
    }

    @Override
    public Map<String, Object> listOutgoing(Integer userId) {
        Map<String, Object> result = new HashMap<>();
        if (userId == null) {
            result.put("success", false);
            result.put("message", "缺少用户");
            return result;
        }
        result.put("success", true);
        result.put("data", friendMapper.listOutgoingPending(userId));
        return result;
    }

    @Override
    public Map<String, Object> removeFriend(Integer userId, Integer friendUserId) {
        Map<String, Object> result = new HashMap<>();
        if (userId == null || friendUserId == null) {
            result.put("success", false);
            result.put("message", "参数不完整");
            return result;
        }
        if (userId.equals(friendUserId)) {
            result.put("success", false);
            result.put("message", "参数无效");
            return result;
        }
        int n = friendMapper.deleteAcceptedBetween(userId, friendUserId);
        if (n > 0) {
            result.put("success", true);
            result.put("message", "已解除好友关系");
        } else {
            result.put("success", false);
            result.put("message", "未找到好友关系");
        }
        return result;
    }

    @Override
    public Map<String, Object> searchUser(Integer currentUserId, String keyword) {
        Map<String, Object> result = new HashMap<>();
        if (currentUserId == null) {
            result.put("success", false);
            result.put("message", "缺少用户");
            return result;
        }
        if (!StringUtils.hasText(keyword)) {
            result.put("success", false);
            result.put("message", "请输入 UID、邮箱或手机号");
            return result;
        }

        String trimmed = keyword.trim();
        User u = resolveUserByKeyword(trimmed);
        if (u == null) {
            result.put("success", false);
            result.put("message", "未找到匹配的用户");
            return result;
        }
        if (currentUserId.equals(u.getId())) {
            result.put("success", false);
            result.put("message", "不能添加自己");
            return result;
        }

        if (isPhoneKeyword(trimmed) && !UserPrivacyUtil.allowPhoneSearch(u)) {
            result.put("success", false);
            result.put("message", "该用户未开放手机号搜索");
            return result;
        }
        if (trimmed.contains("@") && !UserPrivacyUtil.allowEmailSearch(u)) {
            result.put("success", false);
            result.put("message", "该用户未开放邮箱搜索");
            return result;
        }

        result.put("success", true);
        result.put("data", UserPrivacyUtil.maskForViewer(u));
        return result;
    }

    @Override
    public Map<String, Object> searchByPhone(Integer currentUserId, String phone) {
        return searchUser(currentUserId, phone);
    }

    private User resolveUserByKeyword(String keyword) {
        if (keyword.contains("@")) {
            return userMapper.findByEmail(keyword);
        }
        if (isPhoneKeyword(keyword)) {
            return userMapper.findByPhone(keyword);
        }
        if (keyword.matches("^\\d+$")) {
            try {
                return userMapper.findByUid(Long.parseLong(keyword));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private boolean isPhoneKeyword(String keyword) {
        return keyword.matches("^1[3-9]\\d{9}$");
    }

    @Override
    public Map<String, Object> getFriendProfile(Integer userId, Integer friendUserId) {
        Map<String, Object> result = new HashMap<>();
        if (userId == null || friendUserId == null) {
            result.put("success", false);
            result.put("message", "参数不完整");
            return result;
        }
        if (userId.equals(friendUserId)) {
            result.put("success", false);
            result.put("message", "参数无效");
            return result;
        }
        if (friendMapper.countAcceptedBetweenUsers(userId, friendUserId) <= 0) {
            result.put("success", false);
            result.put("message", "对方不是您的好友");
            return result;
        }
        User u = userMapper.findById(friendUserId);
        if (u == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }
        result.put("success", true);
        result.put("data", UserPrivacyUtil.maskForViewer(u));
        return result;
    }

    @Override
    public boolean areFriends(Integer userId, Integer friendUserId) {
        if (userId == null || friendUserId == null || userId.equals(friendUserId)) {
            return false;
        }
        return friendMapper.countAcceptedBetweenUsers(userId, friendUserId) > 0;
    }
}
