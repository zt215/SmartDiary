package org.example.back.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.back.pojo.DiaryCircle;

import java.util.List;

@Mapper
public interface DiaryCircleMapper {

    int insert(DiaryCircle diaryCircle);

    int deleteById(@Param("id") Integer id);

    int update(DiaryCircle diaryCircle);

    DiaryCircle selectById(@Param("id") Integer id);

    List<DiaryCircle> selectAllWithUser(@Param("offset") Integer offset, @Param("limit") Integer limit);

    List<DiaryCircle> selectFriendsWithUser(@Param("offset") Integer offset,
                                            @Param("limit") Integer limit,
                                            @Param("userId") Integer userId);

    List<DiaryCircle> selectByUserId(@Param("userId") Integer userId);
}
