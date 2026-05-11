package org.example.back.service;

import org.example.back.pojo.Diary;
import java.util.Map;

public interface DiaryService {
    Map<String, Object> createDiary(Diary diary);
    
    Map<String, Object> getDiaryById(Integer id);
    
    Map<String, Object> getDiariesByUserId(Integer userId);
    
    Map<String, Object> updateDiary(Diary diary);
    
    Map<String, Object> deleteDiary(Integer id);
    
    Map<String, Object> searchDiaries(Integer userId, String keyword);
}
