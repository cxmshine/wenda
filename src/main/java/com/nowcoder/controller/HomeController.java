package com.nowcoder.controller;

import com.nowcoder.model.Question;
import com.nowcoder.model.ViewObject;
import com.nowcoder.service.QuestionService;
import com.nowcoder.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;


@Controller
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private QuestionService questionService;


    @RequestMapping(path = {"/user/{userId}"},method = RequestMethod.GET)
    public String userIndex(Model model, @PathVariable("userId") int userId) {
        model.addAttribute("vos",getQuestions(userId,0,10));
        return "index";
    }

    @RequestMapping(path = {"/","/index"},method = RequestMethod.GET)
    public String index(Model model) {
        // 查看QuestionDAO.xml可知,当userId的值为0时,不会加上 WHERE 子句 .
        // 等同于直接取所有数据降序排列后的前10条
        model.addAttribute("vos",getQuestions(0,0,10));
        return "index";
    }


    // 这个方法是从上面的index()方法中抽取出来的,因为我们新增userIndex()方法
    // ,也要用到下面的这些代码,于是抽取出来成为独立方法
    // 方法名为getQuestions,但实际上返回的是ViewObject的集合,还有User实体在其中.
    private List<ViewObject> getQuestions(int userId,int offset,int limit) {
        List<Question> questionList = questionService.selectLatestQuestions(userId, offset, limit);
        List<ViewObject> vos = new ArrayList<>();
        for(Question question : questionList) {
            ViewObject vo = new ViewObject();
            vo.set("question",question);
            vo.set("user",userService.getUser(question.getUserId()));
            vos.add(vo);
        }
        return vos;
    }


}
