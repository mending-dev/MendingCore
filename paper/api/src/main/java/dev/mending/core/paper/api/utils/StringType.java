package dev.mending.core.paper.api.utils;

public class StringType {
    
    public static boolean isBoolean(String str) {
        if (str == null) return false;
        
        String lowerStr = str.toLowerCase();
        return lowerStr.equals("true") || 
               lowerStr.equals("false") ||
               lowerStr.equals("yes") || 
               lowerStr.equals("no") ||
               str.equals("1") || 
               str.equals("0");
    }
    
    public static boolean isInteger(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        
        // Handle negative numbers
        int start = 0;
        if (str.charAt(0) == '-') {
            if (str.length() == 1) return false;
            start = 1;
        }
        
        // Check each character is a digit
        for (int i = start; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        
        return true;
    }
    
    public static Boolean parseBooleanSafe(String str) {
        if (!isBoolean(str)) return null;
        
        String lowerStr = str.toLowerCase();
        return lowerStr.equals("true") || 
               lowerStr.equals("yes") || 
               str.equals("1");
    }
    
    public static Integer parseIntegerSafe(String str) {
        if (!isInteger(str)) return null;
        
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}