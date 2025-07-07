package com.odoo.common.core.utils;

import cn.hutool.core.util.StrUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 字符串工具类
 * 基于Hutool扩展，添加一些业务常用方法
 *
 * @author odoo
 */
public class StringUtils extends StrUtil {

    /**
     * 手机号脱敏显示
     */
    public static String maskMobile(String mobile) {
        if (isBlank(mobile) || mobile.length() < 11) {
            return mobile;
        }
        return mobile.substring(0, 3) + "****" + mobile.substring(7);
    }

    /**
     * 邮箱脱敏显示
     */
    public static String maskEmail(String email) {
        if (isBlank(email) || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];
        
        if (username.length() <= 2) {
            return "*".repeat(username.length()) + "@" + domain;
        }
        
        return username.charAt(0) + "*".repeat(username.length() - 2) + 
               username.charAt(username.length() - 1) + "@" + domain;
    }

    /**
     * 身份证号脱敏显示
     */
    public static String maskIdCard(String idCard) {
        if (isBlank(idCard) || idCard.length() < 8) {
            return idCard;
        }
        return idCard.substring(0, 6) + "********" + idCard.substring(idCard.length() - 4);
    }

    /**
     * 银行卡号脱敏显示
     */
    public static String maskBankCard(String bankCard) {
        if (isBlank(bankCard) || bankCard.length() < 8) {
            return bankCard;
        }
        return bankCard.substring(0, 4) + " **** **** " + bankCard.substring(bankCard.length() - 4);
    }

    /**
     * 生成随机字符串
     * @param length 长度
     * @param includeNumbers 是否包含数字
     * @param includeLetters 是否包含字母
     * @param includeSymbols 是否包含符号
     */
    public static String generateRandom(int length, boolean includeNumbers, 
                                      boolean includeLetters, boolean includeSymbols) {
        StringBuilder chars = new StringBuilder();
        if (includeNumbers) {
            chars.append("0123456789");
        }
        if (includeLetters) {
            chars.append("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
        }
        if (includeSymbols) {
            chars.append("!@#$%^&*()_+-=[]{}|;:,.<>?");
        }
        
        if (chars.length() == 0) {
            throw new IllegalArgumentException("至少要包含一种字符类型");
        }
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            result.append(chars.charAt(index));
        }
        return result.toString();
    }

    /**
     * 生成随机数字字符串
     */
    public static String generateRandomNumbers(int length) {
        return generateRandom(length, true, false, false);
    }

    /**
     * 生成随机字母字符串
     */
    public static String generateRandomLetters(int length) {
        return generateRandom(length, false, true, false);
    }

    /**
     * 生成随机字母数字字符串
     */
    public static String generateRandomAlphanumeric(int length) {
        return generateRandom(length, true, true, false);
    }

    /**
     * 校验手机号格式
     */
    public static boolean isValidMobile(String mobile) {
        return isNotBlank(mobile) && Pattern.matches("^1[3-9]\\d{9}$", mobile);
    }

    /**
     * 校验邮箱格式
     */
    public static boolean isValidEmail(String email) {
        return isNotBlank(email) && 
               Pattern.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", email);
    }

    /**
     * 校验身份证号格式
     */
    public static boolean isValidIdCard(String idCard) {
        return isNotBlank(idCard) && 
               Pattern.matches("^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$", idCard);
    }

    /**
     * 驼峰转下划线
     */
    public static String camelToUnderscore(String camelCase) {
        if (isBlank(camelCase)) {
            return camelCase;
        }
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < camelCase.length(); i++) {
            char ch = camelCase.charAt(i);
            if (Character.isUpperCase(ch)) {
                if (i > 0) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(ch));
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    /**
     * 下划线转驼峰
     */
    public static String underscoreToCamel(String underscore) {
        if (isBlank(underscore)) {
            return underscore;
        }
        
        StringBuilder result = new StringBuilder();
        boolean nextUpperCase = false;
        
        for (int i = 0; i < underscore.length(); i++) {
            char ch = underscore.charAt(i);
            if (ch == '_') {
                nextUpperCase = true;
            } else {
                if (nextUpperCase) {
                    result.append(Character.toUpperCase(ch));
                    nextUpperCase = false;
                } else {
                    result.append(ch);
                }
            }
        }
        return result.toString();
    }

    /**
     * 分割字符串并去除空白
     */
    public static List<String> splitAndTrim(String str, String separator) {
        List<String> result = new ArrayList<>();
        if (isBlank(str)) {
            return result;
        }
        
        String[] parts = str.split(separator);
        for (String part : parts) {
            String trimmed = trim(part);
            if (isNotEmpty(trimmed)) {
                result.add(trimmed);
            }
        }
        return result;
    }

    /**
     * 首字母大写
     */
    public static String capitalize(String str) {
        if (isBlank(str)) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * 首字母小写
     */
    public static String uncapitalize(String str) {
        if (isBlank(str)) {
            return str;
        }
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * 安全截取字符串
     */
    public static String safeSub(String str, int start, int end) {
        if (isBlank(str)) {
            return str;
        }
        int len = str.length();
        start = Math.max(0, start);
        end = Math.min(len, end);
        if (start >= end) {
            return "";
        }
        return str.substring(start, end);
    }

    /**
     * 判断字符串是否为纯数字
     */
    public static boolean isNumeric(String str) {
        return isNotBlank(str) && str.matches("\\d+");
    }

    /**
     * 判断字符串是否为纯字母
     */
    public static boolean isAlpha(String str) {
        return isNotBlank(str) && str.matches("[a-zA-Z]+");
    }

    /**
     * 判断字符串是否为字母数字组合
     */
    public static boolean isAlphanumeric(String str) {
        return isNotBlank(str) && str.matches("[a-zA-Z0-9]+");
    }
} 