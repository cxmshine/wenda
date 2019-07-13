package com.nowcoder.service;


import com.nowcoder.dao.CommentDAO;
import com.nowcoder.model.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentDAO commentDAO;

    @Autowired
    private SensitiveService sensitiveService;

    public List<Comment> selectCommentByEntity(int entityId,int entityType) {
        return commentDAO.selectCommentByEntity(entityId,entityType);
    }

    public int addComment(Comment comment) {
        // 过滤掉<script>alert("hi")</script>
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        // 添加评论时,对敏感词进行过滤
        comment.setContent(sensitiveService.filter(comment.getContent()));
        return commentDAO.addComment(comment) > 0 ? comment.getId() : 0;
    }

    public int getCommentCount(int entityId,int entityType) {
        return commentDAO.getCommentCount(entityId,entityType);
    }

    public boolean deleteComment(int commentId) {
        // status的状态为1时,表示删除评论
        return commentDAO.updateStatus(commentId,1) > 0;
    }

    public Comment getCommentById(int id) {
        return commentDAO.getCommentById(id);
    }
}
