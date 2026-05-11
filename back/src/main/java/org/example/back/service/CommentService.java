package org.example.back.service;

import org.example.back.pojo.Comment;

import java.util.List;

public interface CommentService {
    
    /**
     * 添加评论
     */
    int addComment(Comment comment);
    
    /**
     * 根据 ID 查询评论
     */
    Comment getCommentById(Integer id);
    
    /**
     * 删除评论
     */
    int deleteComment(Integer id);
    
    /**
     * 根据动态 ID 查询评论列表
     */
    List<Comment> getCommentsByCircleId(Integer circleId);
    
    /**
     * 点赞评论
     */
    boolean toggleLike(Integer id, boolean isLike, Integer userId);
    
    /**
     * 根据动态 ID 查询评论列表（带点赞状态）
     */
    java.util.List<Comment> getCommentsByCircleIdWithUserStatus(Integer circleId, Integer currentUserId);
}
