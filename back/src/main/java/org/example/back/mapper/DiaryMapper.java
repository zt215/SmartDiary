package org.example.back.mapper;

import org.example.back.pojo.Diary;
import java.util.Date;
import java.util.List;

public interface DiaryMapper {
    
    /**
     * 插入日记
     */
    int insert(Diary diary);
    
    /**
     * 根据ID查询日记
     */
    Diary findById(Integer id);
    
    /**
     * 根据用户ID查询所有日记
     */
    List<Diary> findByUserId(Integer userId);
    
    /**
     * 根据用户ID和日期范围查询日记
     */
    List<Diary> findByUserIdAndDateRange(Integer userId, Date startDate, Date endDate);
    
    /**
     * 更新日记
     */
    int update(Diary diary);
    
    /**
     * 删除日记
     */
    int deleteById(Integer id);
    
    /**
     * 根据用户ID和关键词搜索日记
     */
    List<Diary> searchByKeyword(Integer userId, String keyword);
}
