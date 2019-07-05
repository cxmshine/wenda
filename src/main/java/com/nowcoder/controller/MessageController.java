package com.nowcoder.controller;

import com.nowcoder.model.HostHolder;
import com.nowcoder.model.Message;
import com.nowcoder.model.User;
import com.nowcoder.model.ViewObject;
import com.nowcoder.service.MessageService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.WendaUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@Slf4j
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;


    @RequestMapping(path = "msg/list",method = RequestMethod.GET)
    public String getConversationList() {
        return "letter";
    }


    @RequestMapping(path = "msg/detail",method = RequestMethod.GET)
    public String getConversationDetail(Model model,@RequestParam("conversationId") String conversationId) {
        try {
            List<Message> messageList = messageService.getMessageDetail(conversationId, 0, 10);
            List<ViewObject> messages = new ArrayList<>();
            for (Message message : messageList) {
                ViewObject vo = new ViewObject();
                vo.set("message",message);
                vo.set("user",userService.getUser(message.getFromId()));
                messages.add(vo);
            }
            model.addAttribute("messages",messages);
        } catch (Exception e) {
            log.error("发送消息失败"+e.getMessage());
        }
        return "letterDetail";
    }

    @RequestMapping(path = "msg/addMessage",method = RequestMethod.POST)
    @ResponseBody
    public String addMessage(@RequestParam("toName") String toName,
                          @RequestParam("content") String content) {
        try {
            if(hostHolder.getUser() == null) {
                return WendaUtil.getJSONString(999,"未登录");
            }

            User user = userService.selectUserByName(toName);
            if( user== null) {
                return WendaUtil.getJSONString(1,"用户不存在");
            }

            Message message = new Message();
            message.setCreatedDate(new Date());
            message.setContent(content);
            message.setFromId(hostHolder.getUser().getId());
            message.setToId(user.getId());
            messageService.addMessage(message);
            // 0表示成功
            return WendaUtil.getJSONString(0);
        } catch (Exception e) {
            log.error("发送消息失败"+e.getMessage());
            return WendaUtil.getJSONString(1,"发送信息失败");
        }
    }

}
