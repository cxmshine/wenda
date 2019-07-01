package com.nowcoder.model;

import org.springframework.stereotype.Component;

@Component
public class HostHolder {
    // 线程本地变量,看起来只有一个变量,但实际上它为每个线程都分配了一个对象
    private static ThreadLocal<User> users = new ThreadLocal<>();

    // 调用此方法时,它会找到与当前线程关联的对象并返回
    public User getUser() {
        return users.get();
    }

    public void setUser(User user) {
        users.set(user);
    }

    public void clear() {
        users.remove();
    }
}
