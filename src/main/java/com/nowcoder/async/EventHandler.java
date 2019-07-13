package com.nowcoder.async;

import java.util.List;

/**
 * @Author xuming
 * @Date 2019/7/13 17:05
 */
public interface EventHandler {
    void doHandle(EventModel model);

    // 该handler对哪个事件感兴趣
    List<EventType> getSupportEventTypes();
}
