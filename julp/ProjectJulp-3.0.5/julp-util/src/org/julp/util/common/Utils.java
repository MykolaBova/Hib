package org.julp.util.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Utils {

    public enum CASE {
        UPPER, LOWER
    };

    public enum SPACE {
        NO_SPACE, ONE_SPACE
    };
    protected static final Object[] EMPTY_READ_ARG = new Object[0];

    private Utils() {
    }

    /**
     * Breaking long String to lines. Line does not break words, only white space Lines could be separated with System.getProperty(\"line.separator\") - default or \"<br>\" or any given String.
     */
    public static String breakString(String source, String separator, int offset, boolean preserveWhiteSpace) {
        StringBuilder sb = new StringBuilder();
        String[] z = source.split(" ");
        String line = "";
        for (int i = 0; i < z.length; i++) {
            if (preserveWhiteSpace) {
                line = line + z[i] + " ";
            } else {
                line = line + z[i].trim();
                if (!z[i].equals("")) {
                    line = line + " ";
                }
            }
            if (line.length() >= offset) {
                sb.append(line).append(separator);
                line = "";
            }
            if (i == z.length - 1) { // last line
                if (line.length() < offset) {
                    sb.append(line);
                }
            }
        }
        return sb.toString();
    }

    public static String breakString(String source, String separator, int offset) {
        return breakString(source, separator, offset, false);
    }

    public static String breakString(String source, int offset) {
        String lineSeparator;
        try {
            lineSeparator = System.getProperty("line.separator");
        } catch (SecurityException se) {
            lineSeparator = "\n"; //??
        }
        return breakString(source, lineSeparator, offset, false);
    }

    public static String normalizeString(String source, CASE caseType, SPACE spaceType, char replaceWith) {
        if (source == null || source.trim().length() == 0) {
            return "";
        }
        source = source.trim();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            if (Character.isWhitespace(c)) {
                if (spaceType == SPACE.NO_SPACE) {
                    if (!Character.isWhitespace(replaceWith)) {
                        sb.append(replaceWith);
                    }
                } else if (spaceType == SPACE.ONE_SPACE) {
                    if (i > 0 && !Character.isWhitespace(source.charAt(i - 1))) {
                        if (!Character.isWhitespace(replaceWith)) {
                            sb.append(replaceWith);
                        } else {
                            sb.append(c);
                        }
                    }
                }
            } else {
                sb.append(c);
            }
        }
        String result = null;
        if (caseType == CASE.UPPER) {
            result = sb.toString().toUpperCase();
        }
        if (caseType == CASE.LOWER) {
            result = sb.toString().toLowerCase();
        }
        return result;
    }

    /**
     * This is convenient method to use with JSP. Make sure not to call this method from another method of this object (or subclass) started with "get", or you will get endless loop
     */
    public static String getValuesAsHtml(Object dataOject, String charset) {
        if (charset == null || charset.trim().length() == 0) {
            charset = "UTF-8";
        }
        StringBuilder sb = new StringBuilder();
        Object value = null;
        Method[] methods = dataOject.getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            String methodName = methods[i].getName();
            if (methodName.equals("getValuesAsHtml") || methodName.equals("getClass")) {
                continue;
            }
            if ((methodName.startsWith("get") || methodName.startsWith("is")) && methods[i].getParameterTypes().length == 0) {
                try {
                    value = methods[i].invoke(dataOject, EMPTY_READ_ARG);
                } catch (Throwable t) {
                    if (t instanceof InvocationTargetException) {
                        throw new RuntimeException(((InvocationTargetException) t).getTargetException());
                    } else {
                        throw new RuntimeException(t);
                    }
                }
                String fieldFirstChar = "";
                if (methodName.startsWith("is")) {
                    fieldFirstChar = methodName.substring(2, 3).toLowerCase();
                    sb.append(fieldFirstChar);
                    sb.append(methodName.substring(3));
                } else if (methodName.startsWith("get")) {
                    fieldFirstChar = methodName.substring(3, 4).toLowerCase();
                    sb.append(fieldFirstChar);
                    sb.append(methodName.substring(4));
                }
                sb.append("=");
                if (value == null) {
                    sb.append("");
                } else {
                    try {
                        sb.append(java.net.URLEncoder.encode(value.toString(), charset));
                    } catch (java.io.UnsupportedEncodingException uee) {
                        throw new RuntimeException(uee);
                    }
                }
                sb.append("&");
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public static Throwable getCause(Throwable t) {
        if (t instanceof InvocationTargetException) {
            t = ((InvocationTargetException) t).getTargetException();
        }
        Throwable cause;
        do {
            cause = t;
        } while ((t = t.getCause()) != null);
        return cause;
    }
}
