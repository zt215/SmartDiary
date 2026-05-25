package org.example.back.service;

import org.example.back.pojo.FriendUserBrief;

import java.util.List;
import java.util.Map;

public interface FriendService {

    Map<String, Object> sendRequest(Integer fromUserId, Integer toUserId);

    Map<String, Object> accept(Integer userId, Integer requestId);

    Map<String, Object> reject(Integer userId, Integer requestId);

    Map<String, Object> listFriends(Integer userId);

    Map<String, Object> listIncoming(Integer userId);

    Map<String, Object> listOutgoing(Integer userId);

    Map<String, Object> removeFriend(Integer userId, Integer friendUserId);

    Map<String, Object> searchByPhone(Integer currentUserId, String phone);

    Map<String, Object> getFriendProfile(Integer userId, Integer friendUserId);

    boolean areFriends(Integer userId, Integer friendUserId);
}
