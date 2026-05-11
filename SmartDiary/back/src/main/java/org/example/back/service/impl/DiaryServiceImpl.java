package org.example.back.service.impl;

import org.example.back.mapper.DiaryMapper;
import org.example.back.pojo.Diary;
import org.example.back.service.DiaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class DiaryServiceImpl implements DiaryService {

    @Autowired
    private DiaryMapper diaryMapper;

    @Override
    public Map<String, Object> createDiary(Diary diary) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Date now = new Date();
            // 如果前端传了 createTime，就使用传入的日期；否则默认用当前时间
            if (diary.getCreateTime() == null) {
                diary.setCreateTime(now);
            }
            diary.setUpdateTime(now);
            
            int rowsAffected = diaryMapper.insert(diary);
            if (rowsAffected > 0) {
                result.put("success", true);
                result.put("message", "日记创建成功");
                result.put("data", diary);
            } else {
                result.put("success", false);
                result.put("message", "日记创建失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "日记创建异常: " + e.getMessage());
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getDiaryById(Integer id) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Diary diary = diaryMapper.findById(id);
            if (diary != null) {
                result.put("success", true);
                result.put("message", "查询成功");
                result.put("data", diary);
            } else {
                result.put("success", false);
                result.put("message", "日记不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询异常: " + e.getMessage());
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getDiariesByUserId(Integer userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            java.util.List<Diary> diaries = diaryMapper.findByUserId(userId);
            result.put("success", true);
            result.put("message", "查询成功");
            result.put("data", diaries);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询异常: " + e.getMessage());
        }
        
        return result;
    }

    @Override
    public Map<String, Object> updateDiary(Diary diary) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            diary.setUpdateTime(new Date());
            int rowsAffected = diaryMapper.update(diary);
            if (rowsAffected > 0) {
                result.put("success", true);
                result.put("message", "日记更新成功");
                result.put("data", diary);
            } else {
                result.put("success", false);
                result.put("message", "日记更新失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "日记更新异常: " + e.getMessage());
        }
        
        return result;
    }

    @Override
    public Map<String, Object> deleteDiary(Integer id) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            int rowsAffected = diaryMapper.deleteById(id);
            if (rowsAffected > 0) {
                result.put("success", true);
                result.put("message", "日记删除成功");
            } else {
                result.put("success", false);
                result.put("message", "日记删除失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "日记删除异常: " + e.getMessage());
        }
        
        return result;
    }

    @Override
    public Map<String, Object> searchDiaries(Integer userId, String keyword) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            java.util.List<Diary> diaries = diaryMapper.searchByKeyword(userId, keyword);
            result.put("success", true);
            result.put("message", "搜索成功");
            result.put("data", diaries);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "搜索异常: " + e.getMessage());
        }
        
        return result;
    }
}
