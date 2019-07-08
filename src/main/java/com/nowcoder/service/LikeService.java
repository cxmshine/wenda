package com.nowcoder.service;

import com.nowcoder.util.JedisAdapter;
import com.nowcoder.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 点赞点踩业务
 */
@Service
public class LikeService {
    @Autowired
    private JedisAdapter jedisAdapter;

    public long getLikeCount(int entityType,int entityId) {
        String likeKey = RedisKeyUtil.getLikeKey(entityType, entityId);
        return jedisAdapter.scard(likeKey);
    }

    // 如果我点赞,返回1;如果点踩,返回-1;既没点赞又没点踩,则返回0.
    public int getLikeStatus(int userId,int entityType,int entityId) {
        String likeKey = RedisKeyUtil.getLikeKey(entityType, entityId);
        if(jedisAdapter.sismember(likeKey,String.valueOf(userId))) {
            return 1;
        }
        String disLikeKey = RedisKeyUtil.getDisLikeKey(entityType, entityId);
        return jedisAdapter.sismember(disLikeKey,String.valueOf(userId)) ? -1 : 0;
    }

    /**
     * 点赞
     * @param userId 是谁点赞
     * @param entityType 是给 question 点赞还是给 comment 点赞
     * @param entityId question/comment 的id
     * @return
     * entityType和entityId共同决定到底是哪个问题/评论
     */
    public long like(int userId,int entityType,int entityId) {
        String likeKey = RedisKeyUtil.getLikeKey(entityType, entityId);
        jedisAdapter.sadd(likeKey,String.valueOf(userId));

        // 如果原先是点踩的,现在改为赞了,要去除之前的踩
        String disLikeKey = RedisKeyUtil.getDisLikeKey(entityType, entityId);
        jedisAdapter.srem(disLikeKey,String.valueOf(userId));
        // 统计总的点赞数
        return jedisAdapter.scard(likeKey);
    }

    public long dislike(int userId,int entityType,int entityId) {
        String disLikeKey = RedisKeyUtil.getDisLikeKey(entityType, entityId);
        jedisAdapter.sadd(disLikeKey,String.valueOf(userId));

        // 如果原先是点赞的,现在改为踩了,要去除之前的赞
        String likeKey = RedisKeyUtil.getLikeKey(entityType, entityId);
        jedisAdapter.srem(likeKey,String.valueOf(userId));

        return jedisAdapter.scard(likeKey);
    }
}
