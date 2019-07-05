package com.nowcoder.service;

import com.nowcoder.dao.QuestionDAO;
import com.nowcoder.model.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class QuestionService {

    @Autowired
    private QuestionDAO questionDAO;

    @Autowired
    private SensitiveService sensitiveService;

    // 发起提问
    public int addQuestion(Question question) {
        // html过滤,不让用户输入的<script>alert("hello");</script>起作用
        question.setContent(HtmlUtils.htmlEscape(question.getContent()));
        question.setTitle(HtmlUtils.htmlEscape(question.getTitle()));
        // 敏感词过滤
        question.setContent(sensitiveService.filter(question.getContent()));
        question.setTitle(sensitiveService.filter(question.getTitle()));


        // questionDAO.addQuestion(question)>0 ?用于判断修改的行数
        // 如果大于0,则说明增加问题成功,返回该问题的id;否则返回0
        return questionDAO.addQuestion(question)>0 ? question.getId() : 0;
    }

    public List<Question> selectLatestQuestions(int userId,int offset,int limit) {
        return questionDAO.selectLatestQuestions(userId,offset,limit);
    }

    public Question selectQuestionById(int id) {
        return questionDAO.selectQuestionById(id);
    }

    public int updateCommentCount(int id,int count) {
        return questionDAO.updateCommentCount(id,count);
    }
}
