package com.nowcoder.dao;

import com.nowcoder.model.Message;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MessageDAO {

    @Insert("insert into message(from_id,to_id,content,has_read,conversation_id,created_date) " +
            "values(#{fromId},#{toId},#{content},#{hasRead},#{conversationId},#{createdDate})")
    int addMessage(Message message);

    @Select("select id,from_id,to_id,content,has_read,conversation_id,created_date from message " +
            "where conversation_id=#{conversationId} order by id desc limit #{offset},#{limit}")
    List<Message> getConversationDetail(@Param("conversationId") String conversationId,
                                        @Param("offset") int offset,
                                        @Param("limit") int limit);
}
