package org.apache.geronimo.console.web.util;

import java.util.Comparator;

/*
 * Sort Attribute Strings while ignoring case.  Later may be changed to
 * better handle the JSR-77 attributes.
 *
 */

public class MBeanAttributesComparator implements Comparator {
    private static final int LEFT_GREATER = 1;
    private static final int RIGHT_GREATER = -1;
    private static final int EQUAL = 0;

    public int compare(Object o1, Object o2) {
        String s1 = (String) o1;
        String s2 = (String) o2;
        return s1.compareToIgnoreCase(s2);
    }
}
