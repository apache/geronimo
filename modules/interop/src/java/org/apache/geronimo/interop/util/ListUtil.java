/**
 *
 *  Copyright 2004-2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.interop.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


public class ListUtil {
    // -----------------------------------------------------------------------
    // public methods
    // -----------------------------------------------------------------------

    public static void addIfNotPresent(List list, Object item) {
        if (!list.contains(item)) {
            list.add(item);
        }
    }

    public static ArrayList getCommaSeparatedList(String arg) {
        return getListWithSeparator(arg, ",");
    }

    public static ArrayList getPathList(String arg) {
        return getListWithSeparator(arg, java.io.File.pathSeparator);
    }

    public static ArrayList getSpaceSeparatedList(String arg) {
        return getListWithSeparator(arg.replace('\t', ' ').replace('\r', ' ').replace('\n', ' '), " ");
    }

    public static ArrayList getListWithSeparator(String text, String separator) {
        ArrayList list = new ArrayList();
        int n = text.length();
        StringBuffer item = new StringBuffer();
        char endQuote = 0;
        for (int i = 0; i < n; i++) {
            if (endQuote == 0 && text.startsWith(separator, i)) {
                add(list, item);
                i += separator.length() - 1;
            } else {
                char c = text.charAt(i);
                item.append(c);
                if (endQuote != 0) {
                    if (c == endQuote) {
                        endQuote = 0;
                    }
                } else if (c == '\'' || c == '\"') {
                    endQuote = c;
                }
            }
        }
        add(list, item);
        return list;
    }

    public static String formatCommaSeparatedList(Collection list) {
        return formatListWithSeparator(list, ",");
    }

    public static String formatSpaceSeparatedList(Collection list) {
        return formatListWithSeparator(list, " ");
    }

    public static String formatListWithSeparator(Collection list, String separator) {
        StringBuffer buffer = new StringBuffer();
        for (Iterator i = list.iterator(); i.hasNext();) {
            Object item = i.next();
            if (buffer.length() != 0) {
                buffer.append(separator);
            }
            buffer.append(item.toString());
        }
        return buffer.toString();
    }

    public static ArrayList getArrayList(Object[] array) {
        int n = array.length;
        ArrayList list = new ArrayList(n);
        for (int i = 0; i < n; i++) {
            list.add(array[i]);
        }
        return list;
    }

    public static String[] getStringArray(List list) {
        int n = list.size();
        String[] array = new String[n];
        int i = 0;
        for (Iterator j = list.iterator(); j.hasNext(); i++) {
            String s = (String) j.next();
            array[i] = s;
        }
        return array;
    }

    public static void printAll(java.io.PrintStream out, String rowPrefix, Collection values) {
        for (Iterator i = values.iterator(); i.hasNext();) {
            Object value = i.next();
            if (rowPrefix != null) {
                out.print(rowPrefix);
            }
            out.println(value);
        }
    }

    // -----------------------------------------------------------------------
    // private methods
    // -----------------------------------------------------------------------

    private static void add(List list, StringBuffer itemBuffer) {
        String item = itemBuffer.toString().trim();
        if (item.length() != 0) {
            list.add(item);
        }
        itemBuffer.setLength(0);
    }
}
