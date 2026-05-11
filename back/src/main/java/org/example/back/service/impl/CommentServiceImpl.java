package org.example.back.service.impl;

import org.example.back.mapper.CommentLikeMapper;
import org.example.back.mapper.CommentMapper;
import org.example.back.pojo.Comment;
import org.example.back.pojo.CommentLike;
import org.example.back.service.CommentService;
import org.example.back.service.DiaryCircleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;
    
    @Autowired
    private DiaryCircleService diaryCircleService;
    
    @Autowired
    private CommentLikeMapper commentLikeMapper;

    @Override
    @Transactional
    public int addComment(Comment comment) {
        return commentMapper.insert(comment);
    }

    @Override
    public Comment getCommentById(Integer id) {
        return commentMapper.selectById(id);
    }

    @Override
    @Transactional
    public int deleteComment(Integer id) {
        // 先查询评论信息，获取 circleId
        Comment comment = commentMapper.selectById(id);
        
        // 如果是回复评论，需要找到其父评论获取 circleId
        if (comment != null && comment.getParentId() != null) {
            Comment parentComment = commentMapper.selectById(comment.getParentId());
            if (parentComment != null) {
                comment.setCircleId(parentComment.getCircleId());
            }
        }
        
        int rows = commentMapper.deleteById(id);
        if (rows > 0 && comment != null && comment.getCircleId() != null) {
            // 减少动态的评论数
            diaryCircleService.decrementCommentCount(comment.getCircleId());
        }
        return rows;
    }

    @Override
    public List<Comment> getCommentsByCircleId(Integer circleId) {
        // 先查询所有评论
        List<Comment> comments = commentMapper.selectByCircleId(circleId);
        
        // 查询回复并组装到父评论下
        for (Comment comment : comments) {
            if (comment.getParentId() == null) {
                // 这是父评论，查询其回复
                List<Comment> replies = commentMapper.selectByParentId(comment.getId());
                comment.setReplies(replies);
            }
        }
        
        // 只返回父评论（没有 parentId 的）
        return comments.stream()
                .filter(c -> c.getParentId() == null)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    public boolean toggleLike(Integer id, boolean isLike, Integer userId) {
        if (userId == null) {
            return false; // 未登录用户不能点赞
        }
        
        if (isLike) {
            // 检查是否已经点赞过
            CommentLike existing = commentLikeMapper.selectByCommentIdAndUserId(id, userId);
            if (existing != null) {
                return true; // 已经点赞过，直接返回
            }
            
            // 插入点赞记录
            CommentLike commentLike = new CommentLike();
            commentLike.setCommentId(id);
            commentLike.setUserId(userId);
            int rows = commentLikeMapper.insert(commentLike);
            
            if (rows > 0) {
                // 增加评论点赞数
                return commentMapper.incrementLikeCount(id) > 0;
            }
            return false;
        } else {
            // 取消点赞
            int rows = commentLikeMapper.deleteByCommentIdAndUserId(id, userId);
            
            if (rows > 0) {
                // 减少评论点赞数
                return commentMapper.decrementLikeCount(id) > 0;
            }
            return false;
        }
    }
    
    @Override
    public List<Comment> getCommentsByCircleIdWithUserStatus(Integer circleId, Integer currentUserId) {
        // 先查询所有评论
        List<Comment> comments = commentMapper.selectByCircleId(circleId);
        
        // 查询回复并组装到父评论下
        for (Comment comment : comments) {
            if (comment.getParentId() == null) {
                // 这是父评论，查询其回复
                List<Comment> replies = commentMapper.selectByParentId(comment.getId());
                comment.setReplies(replies);
                
                // 设置当前用户的点赞状态
                if (currentUserId != null) {
                    CommentLike likeRecord = commentLikeMapper.selectByCommentIdAndUserId(comment.getId(), currentUserId);
                    comment.setIsLiked(likeRecord != null);
                } else {
                    comment.setIsLiked(false);
                }
            }
        }
        
        // 只返回父评论（没有 parentId 的）
        return comments.stream()
                .filter(c -> c.getParentId() == null)
                .collect(java.util.stream.Collectors.toList());
    }
}
