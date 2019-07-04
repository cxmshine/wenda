package com.nowcoder.controller;

import com.nowcoder.model.HostHolder;
import com.nowcoder.model.Question;
import com.nowcoder.service.QuestionService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.WendaUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Controller
@Slf4j
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

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
        model.addAttribute("user",userService.getUser(question.getUserId()));
        return "detail";
    }
}
