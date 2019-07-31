package com.nowcoder.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nowcoder.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import redis.clients.jedis.*;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class JedisAdapter implements InitializingBean {

    private JedisPool pool;

    public static void print(int index,Object obj) {
        System.out.println(String.format("%d,%s",index,obj.toString()));
    }

    public static void main(String[] args){
        Jedis jedis = new Jedis("redis://localhost:6379/9");
        // 每次执行前,将原来redis数据库中的内容清空
        jedis.flushDB();

        // value为String类型
        jedis.set("hello","world");
        print(1,jedis.get("hello"));
        jedis.rename("hello","newhello");
        print(1,jedis.get("newhello"));
        // 15秒后即失效
        jedis.setex("hello2",15,"world");

        // 测试加减操作   [value为String类型]
        jedis.set("pv","100");
        jedis.incr("pv");
        jedis.incrBy("pv",5);
        print(2,jedis.get("pv"));
        jedis.decrBy("pv",2);
        print(2,jedis.get("pv"));

        print(3,jedis.keys("*"));

        // value为list类型
        String listName = "list";
        jedis.del(listName);
        for (int i=0;i<10;i++) {
            // 一共插入10个元素,最先插入的"a0"会一直被"推"着往右走,它是最后一个元素 [有栈的味道]
            // 如果是rpush就反过来了.
            jedis.lpush(listName,"a"+String.valueOf(i));
        }

        print(4,jedis.lrange(listName,0,12));
        print(4,jedis.lrange(listName,0,3));
        print(5,jedis.llen(listName));
        print(6,jedis.lpop(listName));
        print(7,jedis.llen(listName));
        print(8,jedis.lrange(listName,2,6));
        print(9,jedis.lindex(listName,3));
        print(10,jedis.linsert(listName, BinaryClient.LIST_POSITION.AFTER,"a4","xx"));
        print(10,jedis.linsert(listName, BinaryClient.LIST_POSITION.BEFORE,"a4","bb"));
        print(11,jedis.lrange(listName,0,12));

        // value为hash类型
        String userKey = "userxx";
        jedis.hset(userKey,"name","jim");
        jedis.hset(userKey,"age","12");
        jedis.hset(userKey,"phone","13724591967");
        print(12,jedis.hget(userKey,"name"));
        print(13,jedis.hgetAll(userKey));
        jedis.hdel(userKey,"phone");
        print(14,jedis.hgetAll(userKey));
        print(15,jedis.hexists(userKey,"email"));
        print(16,jedis.hexists(userKey,"age"));
        print(17,jedis.hkeys(userKey));
        print(18,jedis.hvals(userKey));
        // nx表示if not exist
        jedis.hsetnx(userKey,"school","zju");
        jedis.hsetnx(userKey,"name","tom");
        print(19,jedis.hgetAll(userKey));

        // value为set类型
        String likeKey1 = "commentLike1";
        String likeKey2 = "commentLike2";
        for (int i=0;i<10;i++) {
            jedis.sadd(likeKey1,String.valueOf(i));
            jedis.sadd(likeKey2,String.valueOf(i*i));
        }
        print(20,jedis.smembers(likeKey1));
        print(21,jedis.smembers(likeKey2));
        print(22,jedis.sunion(likeKey1,likeKey2));
        print(23,jedis.sdiff(likeKey1,likeKey2));
        print(24,jedis.sinter(likeKey1,likeKey2));
        print(25,jedis.sismember(likeKey1,"12"));
        print(26,jedis.sismember(likeKey2,"16"));
        jedis.srem(likeKey1,"5");
        print(27,jedis.smembers(likeKey1));
        // 将likeKey2中的25移入likeKey1中
        jedis.smove(likeKey2,likeKey1,"25");
        print(28,jedis.smembers(likeKey1));
        print(28,jedis.smembers(likeKey2));
        // 当前集合中有多少个元素
        print(29,jedis.scard(likeKey1));

        // value为zset类型
        String rankKey = "rankKey";
        jedis.zadd(rankKey,15,"jim");
        jedis.zadd(rankKey,60,"Ben");
        jedis.zadd(rankKey,90,"Lee");
        jedis.zadd(rankKey,75,"Lucy");
        jedis.zadd(rankKey,80,"Mei");
        // 计数
        print(30,jedis.zcard(rankKey));
        // score介于61和100之间的有多少个
        print(31,jedis.zcount(rankKey,61,100));
        print(32,jedis.zscore(rankKey,"Lucy"));
        jedis.zincrby(rankKey,2,"Lucy");
        print(33,jedis.zscore(rankKey,"Lucy"));
        // "Luc"是之前没出现过的,在这里要对其进行加2操作,其默认值为0. 0加2得2
        jedis.zincrby(rankKey,2,"Luc");
        print(34,jedis.zscore(rankKey,"Luc"));
        print(35,jedis.zrange(rankKey,0,100));
        print(36,jedis.zrange(rankKey,0,10));
        // 注意这里的1,3是下标而不是第1名到第3名;实际上它是第二名到第四名.
        print(36,jedis.zrange(rankKey,1,3));
        // zrange是升序的,zrevrange是降序
        print(36,jedis.zrevrange(rankKey,1,3));
        for (Tuple tuple : jedis.zrangeByScoreWithScores(rankKey,"60","100")) {
            print(37,tuple.getElement() + ":" + String.valueOf(tuple.getScore()));
        }

        // Ben的排名,升序
        print(38,jedis.zrank(rankKey,"Ben"));
        // Ben的排名,降序
        print(39,jedis.zrevrank(rankKey,"Ben"));

        String setKey = "zset";
        jedis.zadd(setKey,1,"a");
        jedis.zadd(setKey,1,"b");
        jedis.zadd(setKey,1,"c");
        jedis.zadd(setKey,1,"d");
        jedis.zadd(setKey,1,"e");

        // 报错,找不到错因.
        //print(40, jedis.zlexcount(setKey, "-", "+"));
        //print(41, jedis.zlexcount(setKey, "(b", "[d"));
        //print(42,jedis.zlexcount(setKey,"[b","[d"));
        jedis.zrem(setKey,"b");
        print(43,jedis.zrange(setKey,0,10));
        //jedis.zremrangeByLex(setKey,"(c","+");
        print(44,jedis.zrange(setKey,0,1));
/*
        JedisPool pool = new JedisPool();
        for (int i=0;i<100;i++) {
            Jedis j = pool.getResource();
            // 空指针异常,不知道哪里错了
            //print(45,j.get("pv"));
            j.close();
        }
*/

        // value为String类型
        // 简单的缓存示例
        // 将对象缓存到redis中
        User user1 = new User();
        user1.setName("xx");
        user1.setPassword("ppp");
        user1.setHeadUrl("a.png");
        user1.setSalt("salt");
        user1.setId(1);
        print(46, JSONObject.toJSONString(user1));
        jedis.set("user1",JSONObject.toJSONString(user1));

        // 从redis中取出对象
        String value = jedis.get("user1");
        User user2 = JSON.parseObject(value,User.class);
        print(47,user2);
        //int k = 2;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        pool = new JedisPool("redis://localhost:6379/10");
    }

    public long sadd(String key,String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.sadd(key,value);
        } catch (Exception e) {
            log.error("发生异常"+e.getMessage());
        } finally {
            if(jedis!=null) {
                jedis.close();
            }
        }
        return 0;
    }

    public long srem(String key,String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.srem(key,value);
        } catch (Exception e) {
            log.error("发生异常"+e.getMessage());
        } finally {
            if(jedis!=null) {
                jedis.close();
            }
        }
        return 0;
    }

    public long scard(String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.scard(key);
        } catch (Exception e) {
            log.error("发生异常"+e.getMessage());
        } finally {
            if(jedis!=null) {
                jedis.close();
            }
        }
        return 0;
    }

    public boolean sismember(String key,String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.sismember(key,value);
        } catch (Exception e) {
            log.error("发生异常"+e.getMessage());
        } finally {
            if(jedis!=null) {
                jedis.close();
            }
        }
        return false;
    }

    // 下面2个方法为高级课第8次新增的

    public List<String> brpop(int timeout, String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.brpop(timeout, key);
        } catch (Exception e) {
            log.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    public long lpush(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.lpush(key, value);
        } catch (Exception e) {
            log.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0;
    }

    public Jedis getJedis() {
        return pool.getResource();
    }

    public Transaction multi(Jedis jedis) {
        try {
            return jedis.multi();
        } catch (Exception e) {
            log.error("发生异常" + e.getMessage());
        }
        return null;
    }

    public List<Object> exec(Transaction tx, Jedis jedis) {
        try {
            return tx.exec();
        } catch (Exception e) {
            log.error("发生异常" + e.getMessage());
            tx.discard();
        } finally {
            if (tx != null) {
                try {
                    tx.close();
                } catch (IOException ioe) {
                    log.error("发生异常" + ioe.getMessage());
                }
            }

            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    public long zadd(String key, double score, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.zadd(key, score, value);
        } catch (Exception e) {
            log.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0;
    }

    public Set<String> zrange(String key, int start, int end) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.zrange(key, start, end);
        } catch (Exception e) {
            log.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    public Set<String> zrevrange(String key, int start, int end) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.zrevrange(key, start, end);
        } catch (Exception e) {
            log.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    public long zcard(String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.zcard(key);
        } catch (Exception e) {
            log.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0;
    }

    public Double zscore(String key, String member) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.zscore(key, member);
        } catch (Exception e) {
            log.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    public List<String> lrange(String key, int start, int end) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.lrange(key, start, end);
        } catch (Exception e) {
            log.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }
}
