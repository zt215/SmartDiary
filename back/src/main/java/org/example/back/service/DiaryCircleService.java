package org.example.back.service;

import org.example.back.pojo.DiaryCircle;

import java.util.List;

public interface DiaryCircleService {

    int publish(DiaryCircle diaryCircle);

    int delete(Integer id);

    List<DiaryCircle> getAll(int page, int pageSize);

    DiaryCircle getById(Integer id);

    DiaryCircle getByIdWithLikeStatus(Integer id, Integer currentUserId);

    List<DiaryCircle> getByUserId(Integer userId);

    boolean toggleLike(Integer id, boolean isLike, Integer userId);

    List<DiaryCircle> getAllWithLikeStatus(int page, int pageSize, Integer currentUserId, String filter);
}
