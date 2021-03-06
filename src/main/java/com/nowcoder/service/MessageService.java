package com.nowcoder.service;

import com.nowcoder.dao.MessageDAO;
import com.nowcoder.model.HostHolder;
import com.nowcoder.model.Message;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageDAO messageDAO;

    @Autowired
    private SensitiveService sensitiveService;

    public int addMessage(Message message) {
        // 对敏感词进行过滤
        message.setContent(sensitiveService.filter(message.getContent()));
        return messageDAO.addMessage(message) > 0? message.getId() : 0;
    }

    public List<Message> getMessageDetail(String conversationId,int offset,int limit) {
        return messageDAO.getConversationDetail(conversationId,offset,limit);
    }

    public List<Message> getConversationList(int userId, int offset, int limit) {
        return messageDAO.getConversationList(userId, offset, limit);
    }

    public int getConvesationUnreadCount(int userId, String conversationId) {
        return messageDAO.getConvesationUnreadCount(userId,conversationId);
    }
}
