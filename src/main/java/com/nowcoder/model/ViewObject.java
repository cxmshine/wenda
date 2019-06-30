package com.nowcoder.model;

import java.util.HashMap;
import java.util.Map;

/**
 * vo是非常重要的,将后台数据组装完成后返回给页面,在页面中取出展示.
 * 感受一下$!{vo.user.xx}、$!{vo.question.xx},可以将不同对象的数据组装后返回.
 */
public class ViewObject {
    private Map<String,Object> objs = new HashMap<>();

    public void set(String key,Object value) {
        objs.put(key,value);
    }

    public Object get(String key) {
        return objs.get(key);
    }
}
