package com.willwinder.universalgcodesender.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;

/**
 * A comparator that is useful when sorting a list of numbers in a string.
 *
 * @author Joacim Breiler
 */
public class StringNumberComparator implements Comparator<String> {

    @Override
    public int compare(String o1, String o2) {
        // Try to extract only numbers from the strings
        String num1 = o1.replaceFirst("$", "");
        String num2 = o2.replaceFirst("$", "");

        if (StringUtils.isNumeric(num1) && StringUtils.isNumeric(num2)) {
            return Integer.parseInt(num1) - Integer.parseInt(num2);
        } else {
            return o1.compareTo(o2);
        }
    }
}
