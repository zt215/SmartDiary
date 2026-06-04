package org.example.back.controller;

import org.example.back.pojo.DiaryCircle;
import org.example.back.service.DiaryCircleService;
import org.example.back.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/diary-circle")
@CrossOrigin(origins = "*")
public class DiaryCircleController {

    @Autowired
    private DiaryCircleService diaryCircleService;

    @Autowired
    private FriendService friendService;

    /**
     * 发布新动态
     */
    @PostMapping("/publish")
    public Map<String, Object> publish(@RequestBody DiaryCircle diaryCircle) {
        Map<String, Object> result = new HashMap<>();
        try {
            int rows = diaryCircleService.publish(diaryCircle);
            if (rows > 0) {
                result.put("success", true);
                result.put("message", "发布成功");
                result.put("data", diaryCircle);
            } else {
                result.put("success", false);
                result.put("message", "发布失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "发布异常：" + e.getMessage());
        }
        return result;
    }

    /**
     * 删除动态
     */
    @DeleteMapping("/delete/{id}")
    public Map<String, Object> delete(@PathVariable("id") Integer id,
                                      @RequestParam(value = "userId", required = false) Integer userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            DiaryCircle diaryCircle = diaryCircleService.getById(id);
            if (diaryCircle == null) {
                result.put("success", false);
                result.put("message", "动态不存在");
                return result;
            }
            if (userId == null || diaryCircle.getUserId() == null || !diaryCircle.getUserId().equals(userId)) {
                result.put("success", false);
                result.put("message", "无删除权限");
                return result;
            }
            int rows = diaryCircleService.delete(id);
            if (rows > 0) {
                result.put("success", true);
                result.put("message", "删除成功");
            } else {
                result.put("success", false);
                result.put("message", "删除失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除异常：" + e.getMessage());
        }
        return result;
    }

    /**
     * 分页查询所有动态
     */
    @GetMapping("/list")
    public Map<String, Object> getList(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "userId", required = false) Integer userId,
            @RequestParam(value = "filter", defaultValue = "all") String filter) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<DiaryCircle> list = diaryCircleService.getAllWithLikeStatus(page, pageSize, userId, filter);
            result.put("success", true);
            result.put("data", list);
            result.put("total", list.size());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询异常：" + e.getMessage());
        }
        return result;
    }

    /**
     * 根据 ID 查询动态
     */
    @GetMapping("/{id}")
    public Map<String, Object> getById(
            @PathVariable("id") Integer id,
            @RequestParam(value = "userId", required = false) Integer userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            DiaryCircle diaryCircle = diaryCircleService.getByIdWithLikeStatus(id, userId);
            if (diaryCircle != null) {
                result.put("success", true);
                result.put("data", diaryCircle);
            } else {
                result.put("success", false);
                result.put("message", "动态不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询异常：" + e.getMessage());
        }
        return result;
    }

    /**
     * 查询用户的动态
     */
    @GetMapping("/user/{userId}")
    public Map<String, Object> getByUserId(@PathVariable("userId") Integer userId,
                                           @RequestParam(value = "viewerId", required = false) Integer viewerId) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (viewerId != null && !viewerId.equals(userId)
                    && !friendService.areFriends(viewerId, userId)) {
                result.put("success", false);
                result.put("message", "仅可查看好友的字迹");
                return result;
            }
            List<DiaryCircle> list = diaryCircleService.getByUserId(userId);
            result.put("success", true);
            result.put("data", list);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询异常：" + e.getMessage());
        }
        return result;
    }

    /**
     * 点赞/取消点赞
     */
    @PostMapping("/like/{id}")
    public Map<String, Object> toggleLike(
            @PathVariable("id") Integer id,
            @RequestParam("isLike") boolean isLike,
            @RequestParam("userId") Integer userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = diaryCircleService.toggleLike(id, isLike, userId);
            if (success) {
                result.put("success", true);
                result.put("message", isLike ? "点赞成功" : "取消点赞成功");
            } else {
                result.put("success", false);
                result.put("message", "操作失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "操作异常：" + e.getMessage());
        }
        return result;
    }
}
