package com.nowcoder.async;

/**
 * @Author xuming
 * @Date 2019/7/13 16:40
 */
public enum EventType {
    // 点赞事件
    LIKE(0),
    // 评论事件
    COMMENT(1),
    // 登录事件
    LOGIN(2),
    // 发送邮件
    MAIL(3);

    private int value;
    EventType(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}
