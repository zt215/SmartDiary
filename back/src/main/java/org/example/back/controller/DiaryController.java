package org.example.back.controller;

import org.example.back.pojo.Diary;
import org.example.back.service.DiaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/diary")
@CrossOrigin(origins = "http://localhost:5173")
public class DiaryController {

    @Autowired
    private DiaryService diaryService;
    // 创建日记
    @PostMapping("/create")
    public Map<String, Object> createDiary(@RequestBody Diary diary) {
        return diaryService.createDiary(diary);
    }
    // 根据ID获取日记
    @GetMapping("/{id}")
    public Map<String, Object> getDiaryById(@PathVariable("id") Integer id) {
        return diaryService.getDiaryById(id);
    }
    // 根据用户ID获取日记
    @GetMapping("/user/{userId}")
    public Map<String, Object> getDiariesByUserId(@PathVariable("userId") Integer userId) {
        return diaryService.getDiariesByUserId(userId);
    }
    
    // 更新日记
    @PutMapping("/update")
    public Map<String, Object> updateDiary(@RequestBody Diary diary) {
        return diaryService.updateDiary(diary);
    }
    // 删除日记
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteDiary(@PathVariable("id") Integer id) {
        return diaryService.deleteDiary(id);
    }
    // 根据关键词搜索日记 
    @GetMapping("/search")
    public Map<String, Object> searchDiaries(@RequestParam("userId") Integer userId, @RequestParam("keyword") String keyword) {
        return diaryService.searchDiaries(userId, keyword);
    }
}
