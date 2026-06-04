package org.example.back.service.impl;

import org.example.back.mapper.DiaryDraftMapper;
import org.example.back.pojo.DiaryDraft;
import org.example.back.service.DiaryDraftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class DiaryDraftServiceImpl implements DiaryDraftService {

    private static final Pattern HTML_TAG = Pattern.compile("<[^>]+>");

    @Autowired
    private DiaryDraftMapper diaryDraftMapper;

    @Override
    public Map<String, Object> getDraft(Integer userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            DiaryDraft draft = diaryDraftMapper.findByUserId(userId);
            if (draft != null && hasDraftContent(draft)) {
                result.put("success", true);
                result.put("message", "查询成功");
                result.put("data", draft);
            } else {
                if (draft != null) {
                    diaryDraftMapper.deleteByUserId(userId);
                }
                result.put("success", true);
                result.put("message", "暂无草稿");
                result.put("data", null);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询草稿异常: " + e.getMessage());
        }
        return result;
    }

    @Override
    public Map<String, Object> saveDraft(DiaryDraft draft) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (draft.getUserId() == null) {
                result.put("success", false);
                result.put("message", "用户ID不能为空");
                return result;
            }

            if (!hasDraftContent(draft)) {
                diaryDraftMapper.deleteByUserId(draft.getUserId());
                result.put("success", true);
                result.put("message", "草稿已清空");
                result.put("data", null);
                return result;
            }

            draft.setTitle(draft.getTitle() == null ? "" : draft.getTitle().trim());
            draft.setUpdateTime(new Date());
            diaryDraftMapper.upsert(draft);

            result.put("success", true);
            result.put("message", "草稿保存成功");
            result.put("data", diaryDraftMapper.findByUserId(draft.getUserId()));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "保存草稿异常: " + e.getMessage());
        }
        return result;
    }

    @Override
    public Map<String, Object> deleteDraft(Integer userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            diaryDraftMapper.deleteByUserId(userId);
            result.put("success", true);
            result.put("message", "草稿已删除");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除草稿异常: " + e.getMessage());
        }
        return result;
    }

    private boolean hasDraftContent(DiaryDraft draft) {
        if (StringUtils.hasText(draft.getTitle())) {
            return true;
        }
        String content = draft.getContent();
        if (!StringUtils.hasText(content)) {
            return false;
        }
        String text = HTML_TAG.matcher(content).replaceAll("").trim();
        return StringUtils.hasText(text);
    }
}
