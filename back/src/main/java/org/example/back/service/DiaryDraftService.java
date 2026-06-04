package org.example.back.service;

import org.example.back.pojo.DiaryDraft;

import java.util.Map;

public interface DiaryDraftService {

    Map<String, Object> getDraft(Integer userId);

    Map<String, Object> saveDraft(DiaryDraft draft);

    Map<String, Object> deleteDraft(Integer userId);
}
