package org.example.back.service.impl;

import org.example.back.mapper.DiaryCircleLikeMapper;
import org.example.back.mapper.DiaryCircleMapper;
import org.example.back.pojo.DiaryCircle;
import org.example.back.pojo.DiaryCircleLike;
import org.example.back.service.DiaryCircleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DiaryCircleServiceImpl implements DiaryCircleService {

    @Autowired
    private DiaryCircleMapper diaryCircleMapper;
    
    @Autowired
    private DiaryCircleLikeMapper diaryCircleLikeMapper;

    @Override
    @Transactional
    public int publish(DiaryCircle diaryCircle) {
        return diaryCircleMapper.insert(diaryCircle);
    }

    @Override
    @Transactional
    public int delete(Integer id) {
        return diaryCircleMapper.deleteById(id);
    }

    @Override
    public List<DiaryCircle> getAll(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return diaryCircleMapper.selectAllWithUser(offset, pageSize);
    }

    @Override
    public DiaryCircle getById(Integer id) {
        return diaryCircleMapper.selectById(id);
    }

    @Override
    public List<DiaryCircle> getByUserId(Integer userId) {
        return diaryCircleMapper.selectByUserId(userId);
    }

    @Override
    @Transactional
    public boolean toggleLike(Integer id, boolean isLike, Integer userId) {
        if (userId == null) {
            return false; // 未登录用户不能点赞
        }
        
        if (isLike) {
            // 检查是否已经点赞过
            DiaryCircleLike existing = diaryCircleLikeMapper.selectByDiaryCircleIdAndUserId(id, userId);
            if (existing != null) {
                return true; // 已经点赞过，直接返回
            }
            
            // 插入点赞记录
            DiaryCircleLike diaryCircleLike = new DiaryCircleLike();
            diaryCircleLike.setDiaryCircleId(id);
            diaryCircleLike.setUserId(userId);
            int rows = diaryCircleLikeMapper.insert(diaryCircleLike);
            
            if (rows > 0) {
                // 增加动态点赞数
                return diaryCircleMapper.incrementLikeCount(id) > 0;
            }
            return false;
        } else {
            // 取消点赞
            int rows = diaryCircleLikeMapper.deleteByDiaryCircleIdAndUserId(id, userId);
            
            if (rows > 0) {
                // 减少动态点赞数
                return diaryCircleMapper.decrementLikeCount(id) > 0;
            }
            return false;
        }
    }
    
    @Override
    public List<DiaryCircle> getAllWithLikeStatus(int page, int pageSize, Integer currentUserId) {
        int offset = (page - 1) * pageSize;
        List<DiaryCircle> list = diaryCircleMapper.selectAllWithUser(offset, pageSize);
        
        // 设置当前用户的点赞状态
        if (currentUserId != null) {
            for (DiaryCircle diaryCircle : list) {
                DiaryCircleLike likeRecord = diaryCircleLikeMapper.selectByDiaryCircleIdAndUserId(
                    diaryCircle.getId(), 
                    currentUserId
                );
                diaryCircle.setIsLiked(likeRecord != null);
            }
        } else {
            for (DiaryCircle diaryCircle : list) {
                diaryCircle.setIsLiked(false);
            }
        }
        
        return list;
    }

    @Override
    @Transactional
    public void incrementCommentCount(Integer id) {
        diaryCircleMapper.incrementCommentCount(id);
    }

    @Override
    @Transactional
    public void decrementCommentCount(Integer id) {
        diaryCircleMapper.decrementCommentCount(id);
    }
}
