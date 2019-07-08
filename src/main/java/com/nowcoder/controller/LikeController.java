package com.nowcoder.controller;

import com.nowcoder.model.EntityType;
import com.nowcoder.model.HostHolder;
import com.nowcoder.service.LikeService;
import com.nowcoder.util.WendaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 点赞点踩的业务.不管是给问题点赞点踩还是给评论点赞点踩,都用它.
 */
@Controller
public class LikeController {
    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/like",method = RequestMethod.POST)
    @ResponseBody
    public String like(@RequestParam("commentId") int commentId) {
        if(hostHolder.getUser() == null ) {
            return WendaUtil.getJSONString(999);
        }
        long likeCount = likeService.like(hostHolder.getUser().getId(), EntityType.ENTITY_COMMENT,commentId);
        // 状态码为0表示成功
        return WendaUtil.getJSONString(0,String.valueOf(likeCount));
    }

    @RequestMapping(path = "/dislike",method = RequestMethod.POST)
    @ResponseBody
    public String dislike(@RequestParam("commentId") int commentId) {
        if(hostHolder.getUser() == null ) {
            return WendaUtil.getJSONString(999);
        }
        long likeCount = likeService.dislike(hostHolder.getUser().getId(), EntityType.ENTITY_COMMENT,commentId);
        // 状态码为0表示成功
        return WendaUtil.getJSONString(0,String.valueOf(likeCount));
    }

}
