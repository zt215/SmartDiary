package org.example.back.controller;

import org.example.back.pojo.DiaryDraft;
import org.example.back.service.DiaryDraftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/diary/draft")
@CrossOrigin(origins = "http://localhost:5173")
public class DiaryDraftController {

    @Autowired
    private DiaryDraftService diaryDraftService;

    @GetMapping("/{userId}")
    public Map<String, Object> getDraft(@PathVariable("userId") Integer userId) {
        return diaryDraftService.getDraft(userId);
    }

    @PutMapping
    public Map<String, Object> saveDraft(@RequestBody DiaryDraft draft) {
        return diaryDraftService.saveDraft(draft);
    }

    @DeleteMapping("/{userId}")
    public Map<String, Object> deleteDraft(@PathVariable("userId") Integer userId) {
        return diaryDraftService.deleteDraft(userId);
    }
}
