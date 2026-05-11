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
    
    @PostMapping("/create")
    public Map<String, Object> createDiary(@RequestBody Diary diary) {
        return diaryService.createDiary(diary);
    }
    
    @GetMapping("/{id}")
    public Map<String, Object> getDiaryById(@PathVariable("id") Integer id) {
        return diaryService.getDiaryById(id);
    }
    
    @GetMapping("/user/{userId}")
    public Map<String, Object> getDiariesByUserId(@PathVariable("userId") Integer userId) {
        return diaryService.getDiariesByUserId(userId);
    }
    
    @PutMapping("/update")
    public Map<String, Object> updateDiary(@RequestBody Diary diary) {
        return diaryService.updateDiary(diary);
    }
    
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteDiary(@PathVariable("id") Integer id) {
        return diaryService.deleteDiary(id);
    }
    
    @GetMapping("/search")
    public Map<String, Object> searchDiaries(@RequestParam("userId") Integer userId, @RequestParam("keyword") String keyword) {
        return diaryService.searchDiaries(userId, keyword);
    }
}
