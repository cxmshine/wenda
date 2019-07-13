package com.nowcoder.controller;

import com.nowcoder.async.EventModel;
import com.nowcoder.async.EventProducer;
import com.nowcoder.async.EventType;
import com.nowcoder.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Controller
@Slf4j
public class LoginController {

    @Autowired
    private UserService userService;

//    @Autowired
//    EventProducer eventProducer;

    /**
     * 注册功能
     * @param model
     * @param username
     * @param password
     * @return
     */
    @RequestMapping(path = {"/reg"},method = RequestMethod.POST)
    public String reg(Model model,
                      @RequestParam("username") String username,
                      @RequestParam("password") String password,
                      @RequestParam(value = "next",required = false) String next,
                      HttpServletResponse response) {
        try {
            Map<String, String> map = userService.register(username, password);
            // 如果map中包含"msg"这个key,则说明 用户名/密码为空 或者 用户名已被注册
            if(map.containsKey("ticket")) {
                Cookie cookie = new Cookie("ticket",map.get("ticket"));
                cookie.setPath("/");
                response.addCookie(cookie);
                if(StringUtils.isNotBlank(next)) {
                    return "redirect:"+next;
                }
                return "redirect:/";
            }else {
                model.addAttribute("msg",map.get("msg"));
                // 重新返回到注册登录页面
                return "login";
            }


        } catch (Exception e) {
            log.error("注册异常"+e.getMessage());
            // 重新返回到注册登录页面
            return "login";
        }
    }

    /**
     * 登录功能
     * @param model
     * @param username
     * @param password
     * @return
     */
    @RequestMapping(path = {"/login"},method = RequestMethod.POST)
    public String login(Model model,
                        @RequestParam("username") String username,
                        @RequestParam("password") String password,
                        @RequestParam(value = "next",required = false) String next,
                        @RequestParam(value = "rememberme",defaultValue = "false") boolean rememberme,
                        HttpServletResponse response) {
        try {
            Map<String, Object> map = userService.login(username, password);
            if(map.containsKey("ticket")) {
                Cookie cookie = new Cookie("ticket",map.get("ticket").toString());
                cookie.setPath("/");
                response.addCookie(cookie);

//                eventProducer.fireEvent(new EventModel(EventType.LOGIN)
//                        .setExt("username", username).setExt("email", "zjuyxy@qq.com")
//                        .setActorId((int)map.get("userId")));

                if(StringUtils.isNotBlank(next)) {
                    return "redirect:"+next;
                }
                return "redirect:/";
            }else {
                model.addAttribute("msg",map.get("msg"));
                // 重新返回到注册登录页面
                return "login";
            }

        } catch (Exception e) {
            log.error("注册异常"+e.getMessage());
            // 重新返回到注册登录页面
            return "login";
        }
    }

    // @CookieValue("ticket") String ticket的作用:从Cookie中读取"ticket"所对应的值,
    // 保存到String类型的变量ticket中
    @RequestMapping(path = {"/logout"},method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        // 用户退出后,重定向到首页
        return "redirect:/";
    }

    @RequestMapping(path = {"/reglogin"},method = RequestMethod.GET)
    public String reglogin(Model model,
                           @RequestParam(value = "next",required = false) String next) {
        model.addAttribute("next",next);
        return "login";
    }
}
