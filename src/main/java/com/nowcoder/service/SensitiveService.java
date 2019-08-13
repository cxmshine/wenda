package com.nowcoder.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

// 过滤敏感词用
@Service
@Slf4j
public class SensitiveService implements InitializingBean {

    // 3.已有敏感词词库,应该在初始化bean的时候,就去读取该词库,并将前缀树构建出来
    // 于是,实现相应的接口,重写指定方法
    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("SensitiveWords.txt");
            InputStreamReader reader = new InputStreamReader(is);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String lineTxt;
            while ((lineTxt = bufferedReader.readLine()) != null) {
                addWord(lineTxt.trim());
            }
            // 关闭输入流
            reader.close();
        } catch (Exception e) {
            log.error("读取敏感词词库失败" + e.getMessage());
        }
    }

    // 2.根据传入的敏感词,将这棵前缀树构建出来
    public void addWord(String lineTxt) {
        TrieNode tempNode = rootNode;
        for (int i = 0; i < lineTxt.length(); i++) {
            // 取得每个字符
            Character c = lineTxt.charAt(i);

            // 当前结点下,是否有跟取得的字符(上方的变量c)对应的子结点?
            // 如果有,则tempNode下移即可;若没有,需要新创建一个结点,将其挂在当前结点下
            TrieNode node = tempNode.getSubNode(c);
            if (node == null) {
                // 复用node
                node = new TrieNode();
                tempNode.addSubNode(c, node);
            }
            tempNode = node;

            // 如果到达敏感词的最后一个字符,需要将end标识为true
            if (i == lineTxt.length() - 1) {
                tempNode.setKeywordEnd(true);
            }
        }
    }

    // 1.定义前缀树的结构
    private class TrieNode {
        // 该结点是否为某敏感词的结尾
        private boolean end = false;
        // 当前结点的子结点
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public void addSubNode(Character key, TrieNode node) {
            subNodes.put(key, node);
        }

        public TrieNode getSubNode(Character key) {
            return subNodes.get(key);
        }

        public void setKeywordEnd(boolean end) {
            this.end = end;
        }

        public boolean isKeywordEnd() {
            return end;
        }
    }

    // 将前缀树的结构定义好之后,紧接着直接创建根结点
    TrieNode rootNode = new TrieNode();

    // 5.对filter方法的增强
    public boolean isSymbol(char c) {
        int ic = (int)c;
        // 如果不是Ascii的字母、数字,也不是东亚文字,则判定为特殊字符
        return !CharUtils.isAsciiAlphanumeric(c) && (ic < 0x2E80 || ic > 0x9FFF);
    }

    /**
     * 4.敏感词过滤的核心方法
     *
     * @param text 待过滤文本
     * @return 过滤后所得结果
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return text;
        }
        StringBuilder result = new StringBuilder();
        // 若发现敏感词,则以"***"代替
        String replacement = "***";
        // 仨指针
        TrieNode tempNode = rootNode;
        int begin = 0;
        int position = 0;

        while (position < text.length()) {
            char c = text.charAt(position);

            if (isSymbol(c)) {
                if (tempNode == rootNode) {
                    result.append(c);
                    begin++;
                }
                position++;
                continue;
            }

            // 复用tempNode
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
                // 敏感词中没有以该字符开头的,直接添加进result中
                result.append(text.charAt(begin)); // 若写result.append(c);则部分测试用例不通过
                position = begin + 1;
                begin = position;
                tempNode = rootNode;
            } else if (tempNode.isKeywordEnd()) {
                // 发现敏感词
                result.append(replacement);
                position = position + 1;
                begin = position;
                tempNode = rootNode;
            } else {
                // 部分匹配,暂无法判断是否为敏感词,3号指针继续往下走
                position++;
            }
        }
        // 处理最后一部分 (这一步别漏了)
        result.append(text.substring(begin));
        return result.toString();
    }

    public static void main(String[] args) {
        SensitiveService s = new SensitiveService();
        s.addWord("色情");
        s.addWord("赌博");
        System.out.println(s.filter("色情读物"));
        System.out.println(s.filter("赌博害人不浅"));
        System.out.println(s.filter("点击色#情链接"));
        System.out.println(s.filter("进入赌@博窗口"));
        System.out.println(s.filter("你好色 情"));
    }
}
