package org.example.back.service;

import org.example.back.pojo.DiaryCircle;

import java.util.List;

public interface DiaryCircleService {
    
    /**
     * 发布新动态
     */
    int publish(DiaryCircle diaryCircle);
    
    /**
     * 删除动态
     */
    int delete(Integer id);
    
    /**
     * 分页查询所有动态
     */
    List<DiaryCircle> getAll(int page, int pageSize);
    
    /**
     * 根据 ID 查询动态
     */
    DiaryCircle getById(Integer id);
    
    /**
     * 查询用户的动态
     */
    List<DiaryCircle> getByUserId(Integer userId);
    
    /**
     * 点赞动态
     */
    boolean toggleLike(Integer id, boolean isLike, Integer userId);
    
    /**
     * 查询动态列表（带点赞状态）
     */
    java.util.List<DiaryCircle> getAllWithLikeStatus(int page, int pageSize, Integer currentUserId);
    
    /**
     * 增加评论数
     */
    void incrementCommentCount(Integer id);
    
    /**
     * 减少评论数
     */
    void decrementCommentCount(Integer id);
}
