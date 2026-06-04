package org.example.back.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.back.pojo.Comment;

import java.util.List;

@Mapper
public interface CommentMapper {

    int insert(Comment comment);

    int deleteById(@Param("id") Integer id);

    /** 删除某条评论下的所有回复（parent_id = parentId） */
    int deleteByParentId(@Param("parentId") Integer parentId);

    int deleteByCircleId(@Param("circleId") Integer circleId);

    Comment selectById(@Param("id") Integer id);

    List<Comment> selectByCircleId(@Param("circleId") Integer circleId);

    List<Comment> selectByParentId(@Param("parentId") Integer parentId);
}
