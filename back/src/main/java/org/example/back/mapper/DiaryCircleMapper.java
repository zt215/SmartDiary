package org.example.back.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.back.pojo.DiaryCircle;

import java.util.List;

@Mapper
public interface DiaryCircleMapper {
    
    /**
     * 发布新动态
     */
    int insert(DiaryCircle diaryCircle);
    
    /**
     * 删除动态
     */
    int deleteById(@Param("id") Integer id);
    
    /**
     * 更新动态
     */
    int update(DiaryCircle diaryCircle);
    
    /**
     * 根据 ID 查询动态
     */
    DiaryCircle selectById(@Param("id") Integer id);
    
    /**
     * 分页查询所有动态（带用户信息）
     */
    List<DiaryCircle> selectAllWithUser(@Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 分页查询好友的动态（带用户信息）
     */
    List<DiaryCircle> selectFriendsWithUser(@Param("offset") Integer offset,
                                            @Param("limit") Integer limit,
                                            @Param("userId") Integer userId);
    
    /**
     * 查询用户的动态
     */
    List<DiaryCircle> selectByUserId(@Param("userId") Integer userId);
    
    /**
     * 增加点赞数
     */
    int incrementLikeCount(@Param("id") Integer id);
    
    /**
     * 减少点赞数
     */
    int decrementLikeCount(@Param("id") Integer id);
    
    /**
     * 增加评论数
     */
    int incrementCommentCount(@Param("id") Integer id);
    
    /**
     * 减少评论数
     */
    int decrementCommentCount(@Param("id") Integer id);
}
