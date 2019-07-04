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

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("SensitiveWords.txt");
            InputStreamReader read = new InputStreamReader(is);
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt;
            while((lineTxt=bufferedReader.readLine())!=null) {
                addWord(lineTxt.trim());
            }
            read.close();
        }catch (Exception e) {
            log.error("读取敏感词文件失败"+e.getMessage());
        }
    }

    // 添加敏感词到词典树中
    private void addWord(String lineTxt) {
        TrieNode tempNode = rootNode;
        for(int i=0;i<lineTxt.length();i++) {
            Character c = lineTxt.charAt(i);

            if(isSymbol(c)) {
                continue;
            }

            TrieNode node = tempNode.getSubNode(c);
            if(node==null) {
                node = new TrieNode();
                tempNode.addSubNode(c,node);
            }

            tempNode = node;

            if(i==lineTxt.length()-1) {
                tempNode.setKeywordEnd(true);
            }
        }
    }

    // TrieNode意为前缀树
    private class TrieNode {
        // 当前结点是否到达某个敏感词的结尾
        private boolean end = false;

        private Map<Character,TrieNode> subNodes = new HashMap<>();

        public void addSubNode(Character key,TrieNode node) {
            subNodes.put(key,node);
        }

        public TrieNode getSubNode(Character key) {
            return subNodes.get(key);
        }

        boolean isKeywordEnd() {
            return end;
        }

        void setKeywordEnd(boolean end) {
            this.end = end;
        }
    }

    // 词典树的根
    private TrieNode rootNode = new TrieNode();

    // 对过滤算法进行增强
    // 没有这个方法之前,无法对"你好色 情"进行过滤;增加这个方法后,能够顺利地将"你好色 情"过滤出来
    private boolean isSymbol(char c) {
        int ic = (int)c;
        // 东亚文字 0x2E80 - 0x9FFF
        return !CharUtils.isAsciiAlphanumeric(c) && (ic< 0x2E80 || ic>0x9FFF);
    }

    // 核心方法
    public String filter(String text) {
        if(StringUtils.isBlank(text)) {
            return text;
        }

        StringBuilder result = new StringBuilder();

        // 如果遇到敏感词,则以"***"代替
        String replacement = "***";
        // 3个变量
        TrieNode tempNode = rootNode;
        int begin = 0;
        int position = 0;

        while (position < text.length()) {
            char c = text.charAt(position);

            if(isSymbol(c)) {
                if(tempNode == rootNode) {
                    result.append(c);
                    begin++;
                }
                position++;
                continue;
            }

            tempNode = tempNode.getSubNode(c);

            if(tempNode == null) {
                result.append(text.charAt(begin));
                position = begin+1;
                begin = position;
                tempNode = rootNode;
            } else if(tempNode.isKeywordEnd()) {
                // 发现敏感词
                result.append(replacement);
                position = position+1;
                begin = position;
                tempNode = rootNode;
            } else {
                position++;
            }
        }

        result.append(text.substring(begin));
        return result.toString();

    }

    public static void main(String[] args){
        // 测试filter()函数是否正确实现
        SensitiveService s = new SensitiveService();
        s.addWord("色情");
        s.addWord("赌博");
        System.out.println(s.filter("你好色 情"));
    }
}
