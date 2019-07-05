package com.nowcoder.service;

import com.nowcoder.dao.LoginTicketDAO;
import com.nowcoder.dao.UserDAO;
import com.nowcoder.model.LoginTicket;
import com.nowcoder.model.User;
import com.nowcoder.util.WendaUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private LoginTicketDAO loginTicketDAO;

    /**
     * 用户注册功能
     * @param username 用户名
     * @param password 密码
     * @return
     */
    public Map<String,String> register(String username,String password) {
        Map<String,String> map = new HashMap<>();
        // 判断注册的用户名和密码是否有问题
        if(StringUtils.isBlank(username)) {
            map.put("msg","用户名不能为空");
            return map;
        }

        if(StringUtils.isBlank(password)) {
            map.put("msg","密码不能为空");
            return map;
        }

        // 判断该用户是否已被注册
        User user = userDAO.selectByName(username);
        if(user!=null) {
            map.put("msg","用户名已被注册");
            return map;
        }

        // 至此,用户名和密码都没问题,正式给用户进行注册
        // new一个User对象,然后通过setter完成赋值,调用dao层的方法完成注册
        user = new User();
        user.setName(username);
        user.setSalt(UUID.randomUUID().toString().substring(0,5));
        user.setHeadUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        // 要针对密码做一些处理,然后才传过去.因为数据库中永远不能存放明文密码,有风险
        // 将用户密码和盐值拼接成一个新的字符串,通过工具类进行MD5加密,然后存放到数据库中
        user.setPassword(WendaUtil.MD5(password+user.getSalt()));
        userDAO.addUser(user);

        // 用户注册成功后,在数据库中记下1条ticket
        String ticket = addLoginTicket(user.getId());
        // ticket最终还是要下发浏览器的,所以要放到map中
        map.put("ticket",ticket);

        return map;
    }

    /**
     * 用户登录功能
     * @param username
     * @param password
     * @return
     */
    public Map<String,String> login(String username,String password) {
        Map<String,String> map = new HashMap<>();
        // 判断登录的用户名和密码是否有问题
        if(StringUtils.isBlank(username)) {
            map.put("msg","用户名不能为空");
            return map;
        }

        if(StringUtils.isBlank(password)) {
            map.put("msg","密码不能为空");
            return map;
        }

        // 下面还是需要进行一些必要的验证
        User user = userDAO.selectByName(username);
        if(user==null) {
            // 因为是selectByName,如果为空的话,就说用户名不存在而不提示 "用户名或密码错误"
            map.put("msg","用户名不存在");
            return map;
        }

        if(!WendaUtil.MD5(password+user.getSalt()).equals(user.getPassword())) {
            map.put("msg","密码错误");
            return map;
        }

        // 用户登录成功后,在数据库中记下1条ticket
        String ticket = addLoginTicket(user.getId());
        // ticket最终还是要下发浏览器的,所以要放到map中
        map.put("ticket",ticket);

        // 注:下发ticket到浏览器,需要HttpResponse,所以在controller处,增加了该参数


        return map;
    }

    /**
     * 用户退出,只需将status由0置为1即可
     * @param ticket
     */
    public void logout(String ticket) {
        loginTicketDAO.updateStatus(ticket,1);
    }

    public String addLoginTicket(int userId) {
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(userId);
        Date now = new Date();
        // 设置登录状态时间,100天
        now.setTime(now.getTime()+3600*24*100);
        loginTicket.setExpired(now);
        // 0表示有效,1表示无效.登出的时候将status由0改为1即可
        loginTicket.setStatus(0);
        loginTicket.setTicket(UUID.randomUUID().toString().replaceAll("-",""));
        loginTicketDAO.addTicket(loginTicket);
        return loginTicket.getTicket();
    }

    public User getUser(int id) {
        return userDAO.selectById(id);
    }

    public User selectUserByName(String name) {
        return userDAO.selectByName(name);
    }
}
