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
    public String getConversationList(Model model) {
        // 如果用户未登录,重定向到注册登录页面
        if(hostHolder.getUser()==null) {
            return "redirect:/reglogin";
        }
        int localUserId = hostHolder.getUser().getId();
        List<Message> conversationList = messageService.getConversationList(localUserId, 0, 10);
        List<ViewObject> conversations = new ArrayList<>();
        for (Message message : conversationList) {
            ViewObject vo = new ViewObject();
            vo.set("message",message);
            // 会话的另一方是谁
            int targetId = message.getFromId() == localUserId ? message.getToId() : message.getFromId();
            vo.set("user",userService.getUser(targetId));
            vo.set("unread",messageService.getConvesationUnreadCount(localUserId,message.getConversationId()));
            conversations.add(vo);
        }
        model.addAttribute("conversations",conversations);
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
