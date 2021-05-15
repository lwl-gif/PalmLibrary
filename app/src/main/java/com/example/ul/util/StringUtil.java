package com.example.ul.util;

/**
 * @Author: Wallace
 * @Description: 与字符串有关的操作
 * @Date: 2021/5/15 11:56
 * @Modified: By yyyy-MM-dd
 */
public class StringUtil {

    /**
     * @Author: Wallace
     * @Description: 对字符串处理:将指定位置到指定位置的字符以星号代替
     * @Date: Created 11:57 2021/5/15
     * @Modified: by who yyyy-MM-dd
     * @param content 传入的字符串
     * @param begin 开始位置
     * @param end 结束位置
     * @return: java.lang.String 带星号的字符串
     */
    public static String getStarString(String content, int begin, int end) {
        if (begin >= content.length() || begin < 0) {
            return content;
        }
        if (end >= content.length() || end < 0) {
            return content;
        }
        if (begin >= end) {
            return content;
        }
        StringBuilder starStr = new StringBuilder();
        for (int i = begin; i < end; i++) {
            starStr.append("*");
        }
        return content.substring(0, begin) + starStr + content.substring(end);
    }

    /**
     * @Author: Wallace
     * @Description: 对字符加星号处理：除前面几位和后面几位外，其他的字符以星号代替
     * @Date: Created 11:58 2021/5/15
     * @Modified: by who yyyy-MM-dd
     * @param content 传入的字符串
     * @param frontNum 保留前面字符的位数
     * @param endNum 保留后面字符的位数
     * @return: java.lang.String 带星号的字符串
     */
    public static String getStarString2(String content, int frontNum, int endNum) {
        if (frontNum >= content.length() || frontNum < 0) {
            return content;
        }
        if (endNum >= content.length() || endNum < 0) {
            return content;
        }
        if (frontNum + endNum >= content.length()) {
            return content;
        }
        StringBuilder starStr = new StringBuilder();
        for (int i = 0; i < (content.length() - frontNum - endNum); i++) {
            starStr.append("*");
        }
        return content.substring(0, frontNum) + starStr
                + content.substring(content.length() - endNum);
    }
}
