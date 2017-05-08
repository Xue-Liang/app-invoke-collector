package com.gooagoo.monitor.common;

/**
 * Created by xue on 2017-04-07.
 */
public class StringHelper {
    public static String[] split(String input, String separator, int limit) {
        if (input == null) {
            return null;
        } else if (input.length() < 1) {
            return null;
        } else if (separator == null) {
            return null;
        } else if (separator.length() < 1) {
            return null;
        } else if (limit < 1) {
            return null;
        }
        String[] parts = new String[limit];
        for (
                int i = 0, pos = 0, mark = 0;//i 数组下标;
                i < limit && pos > -1;
                i++, mark = pos + 1) {
            pos = input.indexOf(separator, mark);
            parts[i] = pos < 0 ? input.substring(mark) : input.substring(mark, pos);
        }
        return parts;
    }
}
