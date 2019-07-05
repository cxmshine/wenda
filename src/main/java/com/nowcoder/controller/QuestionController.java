package com.nowcoder.controller;

import com.nowcoder.model.*;
import com.nowcoder.service.CommentService;
import com.nowcoder.service.QuestionService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.WendaUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@Slf4j
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;


    @RequestMapping(value = "question/add",method = RequestMethod.POST)
    @ResponseBody
    public String addQuestion(@RequestParam("title") String title,
                              @RequestParam("content") String content) {
        try {
            Question question = new Question();
            question.setTitle(title);
            question.setContent(content);
            question.setCreatedDate(new Date());
            question.setCommentCount(0);
            if(hostHolder.getUser()==null) {
                //question.setUserId(WendaUtil.ANONYMOUS_USERID);
                return WendaUtil.getJSONString(999);
            }else {
                question.setUserId(hostHolder.getUser().getId());
            }
            if(questionService.addQuestion(question) > 0) {
                return WendaUtil.getJSONString(0);
            }
        }catch (Exception e) {
            log.error("增加提问失败"+e.getMessage());
        }

        return WendaUtil.getJSONString(1,"失败");
    }


    @RequestMapping(value = "question/{qid}")
    public String quesionDetail(Model model, @PathVariable("qid") int qid) {
        Question question = questionService.selectQuestionById(qid);
        model.addAttribute("question",question);

        List<Comment> commentList = commentService.selectCommentByEntity(qid, EntityType.ENTITY_QUESTION);
        // 每个评论体,不仅有comment,还有用户的相关信息(头像以及用户名等),所以封装成ViewObject返回
        List<ViewObject> comments = new ArrayList<>();
        for(Comment comment : commentList) {
            ViewObject vo = new ViewObject();
            vo.set("comment",comment);
            vo.set("user",userService.getUser(comment.getUserId()));
            comments.add(vo);
        }

        model.addAttribute("comments",comments);
        return "detail";
    }
}
