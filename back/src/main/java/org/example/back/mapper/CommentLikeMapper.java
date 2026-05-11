package org.example.back.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.back.pojo.CommentLike;

@Mapper
public interface CommentLikeMapper {
    
    /**
     * 插入点赞记录
     */
    int insert(CommentLike commentLike);
    
    /**
     * 删除点赞记录
     */
    int deleteByCommentIdAndUserId(@Param("commentId") Integer commentId, @Param("userId") Integer userId);
    
    /**
     * 查询用户是否已点赞某评论
     */
    CommentLike selectByCommentIdAndUserId(@Param("commentId") Integer commentId, @Param("userId") Integer userId);
    
    /**
     * 根据评论 ID 查询所有点赞记录
     */
    java.util.List<CommentLike> selectByCommentId(@Param("commentId") Integer commentId);
}
