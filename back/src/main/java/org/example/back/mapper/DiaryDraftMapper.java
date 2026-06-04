package org.example.back.mapper;

import org.example.back.pojo.DiaryDraft;

public interface DiaryDraftMapper {

    DiaryDraft findByUserId(Integer userId);

    int upsert(DiaryDraft draft);

    int deleteByUserId(Integer userId);
}
