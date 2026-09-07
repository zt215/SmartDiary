package org.example.back.controller;

import org.example.back.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/friends")
@CrossOrigin(origins = "http://localhost:5173")
public class FriendController {

    @Autowired
    private FriendService friendService;

    // 发送好友请求
    @PostMapping("/request")
    public Map<String, Object> request(@RequestBody Map<String, Integer> body) {
        Integer fromUserId = body.get("fromUserId");
        Integer toUserId = body.get("toUserId");
        return friendService.sendRequest(fromUserId, toUserId);
    }

    // 接受好友请求
    @PostMapping("/accept")
    public Map<String, Object> accept(@RequestBody Map<String, Integer> body) {
        return friendService.accept(body.get("userId"), body.get("requestId"));
    }

    // 拒绝好友请求
    @PostMapping("/reject")
    public Map<String, Object> reject(@RequestBody Map<String, Integer> body) {
        return friendService.reject(body.get("userId"), body.get("requestId"));
    }

    // 获取好友列表
    @GetMapping("/list")
    public Map<String, Object> list(@RequestParam("userId") Integer userId) {
        return friendService.listFriends(userId);
    }

    // 获取 incoming 好友请求列表
    @GetMapping("/incoming")
    public Map<String, Object> incoming(@RequestParam("userId") Integer userId) {
        return friendService.listIncoming(userId);
    }

    // 获取 outgoing 好友请求列表
    @GetMapping("/outgoing")
    public Map<String, Object> outgoing(@RequestParam("userId") Integer userId) {
        return friendService.listOutgoing(userId);
    }

    // 删除好友
    @DeleteMapping("/remove")
    public Map<String, Object> remove(@RequestParam("userId") Integer userId,
                                      @RequestParam("friendUserId") Integer friendUserId) {
        return friendService.removeFriend(userId, friendUserId);
    }

    // 根据关键词搜索用户
    @GetMapping("/search-user")
    public Map<String, Object> searchUser(@RequestParam("userId") Integer userId,
                                          @RequestParam(value = "keyword", required = false) String keyword,
                                          @RequestParam(value = "phone", required = false) String phone) {
        String q = StringUtils.hasText(keyword) ? keyword : phone;
        return friendService.searchUser(userId, q);
    }

    // 获取好友资料
    @GetMapping("/profile")
    public Map<String, Object> profile(@RequestParam("userId") Integer userId,
                                       @RequestParam("friendUserId") Integer friendUserId) {
        return friendService.getFriendProfile(userId, friendUserId);
    }
}
