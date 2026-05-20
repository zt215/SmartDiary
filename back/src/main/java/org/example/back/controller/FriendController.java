package org.example.back.controller;

import org.example.back.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/friends")
@CrossOrigin(origins = "http://localhost:5173")
public class FriendController {

    @Autowired
    private FriendService friendService;

    @PostMapping("/request")
    public Map<String, Object> request(@RequestBody Map<String, Integer> body) {
        Integer fromUserId = body.get("fromUserId");
        Integer toUserId = body.get("toUserId");
        return friendService.sendRequest(fromUserId, toUserId);
    }

    @PostMapping("/accept")
    public Map<String, Object> accept(@RequestBody Map<String, Integer> body) {
        return friendService.accept(body.get("userId"), body.get("requestId"));
    }

    @PostMapping("/reject")
    public Map<String, Object> reject(@RequestBody Map<String, Integer> body) {
        return friendService.reject(body.get("userId"), body.get("requestId"));
    }

    @GetMapping("/list")
    public Map<String, Object> list(@RequestParam("userId") Integer userId) {
        return friendService.listFriends(userId);
    }

    @GetMapping("/incoming")
    public Map<String, Object> incoming(@RequestParam("userId") Integer userId) {
        return friendService.listIncoming(userId);
    }

    @GetMapping("/outgoing")
    public Map<String, Object> outgoing(@RequestParam("userId") Integer userId) {
        return friendService.listOutgoing(userId);
    }

    @DeleteMapping("/remove")
    public Map<String, Object> remove(@RequestParam("userId") Integer userId,
                                      @RequestParam("friendUserId") Integer friendUserId) {
        return friendService.removeFriend(userId, friendUserId);
    }

    @GetMapping("/search-user")
    public Map<String, Object> searchUser(@RequestParam("userId") Integer userId,
                                          @RequestParam("phone") String phone) {
        return friendService.searchByPhone(userId, phone);
    }
}
