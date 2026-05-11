package org.example.back.controller;

import org.example.back.pojo.Comment;
import org.example.back.service.CommentService;
import org.example.back.service.DiaryCircleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comment")
@CrossOrigin(origins = "*")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private DiaryCircleService diaryCircleService;

    /**
     * 添加评论
     */
    @PostMapping("/add")
    public Map<String, Object> addComment(@RequestBody Comment comment) {
        Map<String, Object> result = new HashMap<>();
        try {
            int rows = commentService.addComment(comment);
            if (rows > 0) {
                // 增加动态的评论数
                diaryCircleService.incrementCommentCount(comment.getCircleId());
                
                result.put("success", true);
                result.put("message", "评论成功");
                result.put("data", comment);
            } else {
                result.put("success", false);
                result.put("message", "评论失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "评论异常：" + e.getMessage());
        }
        return result;
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/delete/{id}")
    public Map<String, Object> deleteComment(
            @PathVariable("id") Integer id,
            @RequestParam(value = "userId", required = false) Integer userId,
            @RequestParam(value = "diaryId", required = false) Integer diaryId) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 查询评论信息
            Comment comment = commentService.getCommentById(id);
            if (comment == null) {
                result.put("success", false);
                result.put("message", "评论不存在");
                return result;
            }
            
            // 查询日记信息，判断是否是日记作者
            boolean isDiaryOwner = false;
            if (diaryId != null) {
                var diary = diaryCircleService.getById(diaryId);
                if (diary != null && diary.getUserId() != null && diary.getUserId().equals(userId)) {
                    isDiaryOwner = true;
                }
            }
            
            // 判断是否有删除权限：评论作者或日记作者
            if (userId == null || (!comment.getUserId().equals(userId) && !isDiaryOwner)) {
                result.put("success", false);
                result.put("message", "无删除权限");
                return result;
            }
            
            int rows = commentService.deleteComment(id);
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
     * 根据动态 ID 查询评论列表
     */
    @GetMapping("/list/{circleId}")
    public Map<String, Object> getCommentsByCircleId(
            @PathVariable("circleId") Integer circleId,
            @RequestParam(value = "userId", required = false) Integer userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            System.out.println("查询评论，circleId: " + circleId + ", userId: " + userId);
            List<Comment> comments = commentService.getCommentsByCircleIdWithUserStatus(circleId, userId);
            System.out.println("返回评论数：" + comments.size());
            if (!comments.isEmpty()) {
                System.out.println("第一条评论 isLiked: " + comments.get(0).getIsLiked());
            }
            result.put("success", true);
            result.put("data", comments);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询异常：" + e.getMessage());
        }
        return result;
    }

    /**
     * 点赞/取消点赞评论
     */
    @PostMapping("/like/{id}")
    public Map<String, Object> toggleLike(
            @PathVariable("id") Integer id,
            @RequestParam("isLike") boolean isLike,
            @RequestParam("userId") Integer userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = commentService.toggleLike(id, isLike, userId);
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
