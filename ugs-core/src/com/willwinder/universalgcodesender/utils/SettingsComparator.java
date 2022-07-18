package com.willwinder.universalgcodesender.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;

/**
 * A comparator that is useful when sorting a list of settings.
 * It can sort GRBL settings using number comparisons and a map based setting using ordinary string comparisons.
 *
 * @author Joacim Breiler
 */
public class SettingsComparator implements Comparator<String> {

    @Override
    public int compare(String o1, String o2) {
        // This is probably a GRBL setting:
        if (o1.startsWith("$") && o2.startsWith("$")) {
            // Try to extract only numbers from the strings
            String num1 = o1.replaceAll("\\D", "");
            String num2 = o2.replaceAll("\\D", "");

            if (StringUtils.isNumeric(num1) && StringUtils.isNumeric(num2)) {
                return Integer.parseInt(num1) - Integer.parseInt(num2);
            } else {
                return o1.compareTo(o2);
            }
        } else {
            return StringUtils.compareIgnoreCase(o1, o2);
        }
    }
}
