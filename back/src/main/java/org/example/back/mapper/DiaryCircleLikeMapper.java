package org.example.back.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.back.pojo.DiaryCircleLike;

@Mapper
public interface DiaryCircleLikeMapper {
    
    /**
     * 插入点赞记录
     */
    int insert(DiaryCircleLike diaryCircleLike);
    
    /**
     * 删除点赞记录
     */
    int deleteByDiaryCircleIdAndUserId(@Param("diaryCircleId") Integer diaryCircleId, @Param("userId") Integer userId);
    
    /**
     * 查询用户是否已点赞某动态
     */
    DiaryCircleLike selectByDiaryCircleIdAndUserId(@Param("diaryCircleId") Integer diaryCircleId, @Param("userId") Integer userId);
}
