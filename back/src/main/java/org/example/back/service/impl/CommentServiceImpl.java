package org.example.back.service.impl;

import org.example.back.mapper.CommentLikeMapper;
import org.example.back.mapper.CommentMapper;
import org.example.back.pojo.Comment;
import org.example.back.pojo.CommentLike;
import org.example.back.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private CommentLikeMapper commentLikeMapper;

    @Override
    @Transactional
    public int addComment(Comment comment) {
        int rows = commentMapper.insert(comment);
        if (rows > 0 && comment.getId() != null) {
            Comment saved = commentMapper.selectById(comment.getId());
            if (saved != null) {
                comment.setLikeCount(saved.getLikeCount());
                comment.setCreateTime(saved.getCreateTime());
                comment.setUpdateTime(saved.getUpdateTime());
                comment.setUserName(saved.getUserName());
                comment.setUserAvatar(saved.getUserAvatar());
                comment.setReplyToUserName(saved.getReplyToUserName());
            }
        }
        return rows;
    }

    @Override
    public Comment getCommentById(Integer id) {
        return commentMapper.selectById(id);
    }

    @Override
    @Transactional
    public int deleteComment(Integer id) {
        // 先删子回复及其点赞，再删本条评论
        commentLikeMapper.deleteByParentCommentId(id);
        commentMapper.deleteByParentId(id);
        commentLikeMapper.deleteByCommentId(id);
        return commentMapper.deleteById(id);
    }

    @Override
    public List<Comment> getCommentsByCircleId(Integer circleId) {
        List<Comment> comments = commentMapper.selectByCircleId(circleId);

        for (Comment comment : comments) {
            if (comment.getParentId() == null) {
                List<Comment> replies = commentMapper.selectByParentId(comment.getId());
                comment.setReplies(replies);
                setRepliesLikedStatus(replies, null);
            }
        }

        return comments.stream()
                .filter(c -> c.getParentId() == null)
                .collect(java.util.stream.Collectors.toList());
    }

    private void setRepliesLikedStatus(List<Comment> replies, Integer currentUserId) {
        if (replies == null) {
            return;
        }
        for (Comment reply : replies) {
            if (currentUserId != null) {
                CommentLike likeRecord = commentLikeMapper.selectByCommentIdAndUserId(reply.getId(), currentUserId);
                reply.setIsLiked(likeRecord != null);
            } else {
                reply.setIsLiked(false);
            }
        }
    }

    @Override
    @Transactional
    public boolean toggleLike(Integer id, boolean isLike, Integer userId) {
        if (userId == null) {
            return false;
        }

        if (isLike) {
            CommentLike existing = commentLikeMapper.selectByCommentIdAndUserId(id, userId);
            if (existing != null) {
                return true;
            }

            CommentLike commentLike = new CommentLike();
            commentLike.setCommentId(id);
            commentLike.setUserId(userId);
            return commentLikeMapper.insert(commentLike) > 0;
        }

        return commentLikeMapper.deleteByCommentIdAndUserId(id, userId) > 0;
    }

    @Override
    public List<Comment> getCommentsByCircleIdWithUserStatus(Integer circleId, Integer currentUserId) {
        List<Comment> comments = commentMapper.selectByCircleId(circleId);

        for (Comment comment : comments) {
            if (comment.getParentId() == null) {
                List<Comment> replies = commentMapper.selectByParentId(comment.getId());
                comment.setReplies(replies);

                if (currentUserId != null) {
                    CommentLike likeRecord = commentLikeMapper.selectByCommentIdAndUserId(comment.getId(), currentUserId);
                    comment.setIsLiked(likeRecord != null);
                } else {
                    comment.setIsLiked(false);
                }
                setRepliesLikedStatus(replies, currentUserId);
            }
        }

        return comments.stream()
                .filter(c -> c.getParentId() == null)
                .collect(java.util.stream.Collectors.toList());
    }
}
