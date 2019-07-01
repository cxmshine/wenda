package com.nowcoder.dao;

import com.nowcoder.model.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
public interface LoginTicketDAO {

    @Insert("insert into login_ticket(user_id,expired,status,ticket) " +
            "values(#{userId},#{expired},#{status},#{ticket})")
    int addTicket(LoginTicket ticket);

    @Select("select id,user_id,expired,status,ticket from login_ticket " +
            "where ticket=#{ticket}")
    LoginTicket selectByTicket(String ticket);

    @Update("update login_ticket set status=#{status} where ticket=#{ticket}")
    void updateStatus(@Param("ticket") String ticket,@Param("status") int status);

}
