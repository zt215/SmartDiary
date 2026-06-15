package org.example.back.service.impl;

import org.example.back.mapper.CommentLikeMapper;
import org.example.back.mapper.CommentMapper;
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

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private CommentLikeMapper commentLikeMapper;

    @Override
    @Transactional
    public int publish(DiaryCircle diaryCircle) {
        int rows = diaryCircleMapper.insert(diaryCircle);
        if (rows > 0 && diaryCircle.getId() != null) {
            DiaryCircle saved = diaryCircleMapper.selectById(diaryCircle.getId());
            if (saved != null) {
                diaryCircle.setLikeCount(saved.getLikeCount());
                diaryCircle.setCommentCount(saved.getCommentCount());
                diaryCircle.setCreateTime(saved.getCreateTime());
                diaryCircle.setUpdateTime(saved.getUpdateTime());
            }
        }
        return rows;
    }

    @Override
    @Transactional
    public int delete(Integer id) {
        commentLikeMapper.deleteByCircleId(id);
        commentMapper.deleteByCircleId(id);
        diaryCircleLikeMapper.deleteByDiaryCircleId(id);
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
    public DiaryCircle getByIdWithLikeStatus(Integer id, Integer currentUserId) {
        DiaryCircle diaryCircle = diaryCircleMapper.selectById(id);
        if (diaryCircle == null) {
            return null;
        }
        if (currentUserId != null) {
            DiaryCircleLike likeRecord = diaryCircleLikeMapper.selectByDiaryCircleIdAndUserId(id, currentUserId);
            diaryCircle.setIsLiked(likeRecord != null);
        } else {
            diaryCircle.setIsLiked(false);
        }
        return diaryCircle;
    }

    @Override
    public List<DiaryCircle> getByUserId(Integer userId) {
        return diaryCircleMapper.selectByUserId(userId);
    }

    @Override
    @Transactional
    public boolean toggleLike(Integer id, boolean isLike, Integer userId) {
        if (userId == null) {
            return false;
        }

        if (isLike) {
            DiaryCircleLike existing = diaryCircleLikeMapper.selectByDiaryCircleIdAndUserId(id, userId);
            if (existing != null) {
                return true;
            }

            DiaryCircleLike diaryCircleLike = new DiaryCircleLike();
            diaryCircleLike.setDiaryCircleId(id);
            diaryCircleLike.setUserId(userId);
            return diaryCircleLikeMapper.insert(diaryCircleLike) > 0;
        }

        return diaryCircleLikeMapper.deleteByDiaryCircleIdAndUserId(id, userId) > 0;
    }

    @Override
    public List<DiaryCircle> getAllWithLikeStatus(int page, int pageSize, Integer currentUserId, String filter) {
        int offset = (page - 1) * pageSize;
        List<DiaryCircle> list;
        if ("friends".equalsIgnoreCase(filter)) {
            if (currentUserId == null) {
                return List.of();
            }
            list = diaryCircleMapper.selectFriendsWithUser(offset, pageSize, currentUserId);
        } else {
            list = diaryCircleMapper.selectAllWithUser(offset, pageSize);
        }
        list.removeIf(dc -> dc.getHidden() != null && dc.getHidden() == 1);

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
}
