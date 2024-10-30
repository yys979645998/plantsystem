package com.example.neo4japp.util;

public class Utils {

    // 格式化日期
    public static String formatDate(Object dateObject) {
        if (dateObject instanceof String) {
            String dateStr = (String) dateObject;
            if (dateStr.length() >= 10) {
                return dateStr.substring(0, 10);
            }
        }
        // 根据需要扩展其他类型
        return "";
    }

    // 生成标签
    public static String generateLabel(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        // 移除空格和非字母数字字符
        String cleaned = name.replaceAll("\\s+", "")
                .replaceAll("[^A-Za-z0-9_]", "");
        // 将首字母转换为大写
        return cleaned.substring(0, 1).toUpperCase() + cleaned.substring(1);
    }
}
