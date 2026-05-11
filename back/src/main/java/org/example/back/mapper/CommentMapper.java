package org.example.back.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.back.pojo.Comment;

import java.util.List;

@Mapper
public interface CommentMapper {
    
    /**
     * 添加评论
     */
    int insert(Comment comment);
    
    /**
     * 删除评论
     */
    int deleteById(@Param("id") Integer id);
    
    /**
     * 根据 ID 查询评论
     */
    Comment selectById(@Param("id") Integer id);
    
    /**
     * 根据动态 ID 查询所有评论（带用户信息）
     */
    List<Comment> selectByCircleId(@Param("circleId") Integer circleId);
    
    /**
     * 根据父评论 ID 查询回复
     */
    List<Comment> selectByParentId(@Param("parentId") Integer parentId);
    
    /**
     * 增加点赞数
     */
    int incrementLikeCount(@Param("id") Integer id);
    
    /**
     * 减少点赞数
     */
    int decrementLikeCount(@Param("id") Integer id);
}
