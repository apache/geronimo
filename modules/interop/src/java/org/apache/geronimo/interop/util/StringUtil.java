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

public abstract class StringUtil {
    public static boolean endsWithIgnoreCase(String name, String suffix) {
        return name.toLowerCase().endsWith(suffix.toLowerCase());
    }

    public static String afterFirst(String separator, String str) {
        int pos = str.indexOf(separator);
        if (pos != -1) {
            return str.substring(pos + separator.length());
        } else {
            return str;
        }
    }

    public static String afterLast(String separator, String str) {
        int pos = str.lastIndexOf(separator);
        if (pos != -1) {
            return str.substring(pos + separator.length());
        } else {
            return str;
        }
    }

    public static String beforeFirst(String separator, String str) {
        int pos = str.indexOf(separator);
        if (pos != -1) {
            return str.substring(0, pos);
        } else {
            return str;
        }
    }

    public static String beforeLast(String separator, String str) {
        int pos = str.lastIndexOf(separator);
        if (pos != -1) {
            return str.substring(0, pos);
        } else {
            return str;
        }
    }

    public static String afterLastSlashOrDot(String str) {
        int pos = str.lastIndexOf('/');
        int dot = str.lastIndexOf('.');
        if (pos != -1 && dot > pos) {
            pos = dot;
        }
        if (pos == -1) {
            return "";
        } else {
            return str.substring(pos + 1);
        }
    }

    public static boolean equalOrBothNull(String a, String b) {
        if (a == null) {
            return b == null;
        } else {
            return b != null && a.equals(b);
        }
    }

    /**
     * * Mangle an arbitrary string to produce a valid Java identifier.
     */
    public static String getJavaIdentifier(String str) {
        // TODO: revise mangling
        int n = str.length();
        StringBuffer s = new StringBuffer(n);
        for (int i = 0; i < n; i++) {
            char c = str.charAt(i);
            if (c == '_') {
                s.append("__");
            } else if (i == 0) {
                if (!Character.isJavaIdentifierStart(c)) {
                    s.append("_");
                } else {
                    s.append(c);
                }
            } else {
                if (!Character.isJavaIdentifierPart(c)) {
                    s.append('_');
                    s.append((int) c);
                    s.append('_');
                } else {
                    s.append(c);
                }
            }
        }
        return s.toString();
    }

    public static String getLowerFirst(String name) {
        if (name.length() == 0) {
            return name;
        } else {
            return Character.toLowerCase(name.charAt(0)) + name.substring(1);
        }
    }

    public static String getLowerFirstIfFirst2NotUpper(String name) {
        if (name.length() >= 2) {
            char c1 = name.charAt(0);
            char c2 = name.charAt(1);
            if (Character.isUpperCase(c1) && Character.isUpperCase(c2)) {
                return name;
            }
        }
        return getLowerFirst(name);
    }

    public static String getUpperFirst(String name) {
        if (name.length() == 0) {
            return name;
        } else {
            return Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }
    }

    public static String padLeft(String s, char c, int n) {
        if (s.length() < n) {
            StringBuffer sb = new StringBuffer(n);
            padLeftAppend(sb, s, c, n);
            return sb.toString();
        } else {
            return s;
        }
    }

    public static void padLeftAppend(StringBuffer sb, String s, char c, int n) {
        int p = n - s.length();
        for (int i = 0; i < p; i++) {
            sb.append(c);
        }
        sb.append(s);
    }

    public static String padRight(String s, char c, int n) {
        if (s.length() < n) {
            StringBuffer sb = new StringBuffer(n);
            padRightAppend(sb, s, c, n);
            return sb.toString();
        } else {
            return s;
        }
    }

    public static void padRightAppend(StringBuffer sb, String s, char c, int n) {
        sb.append(s);
        int p = n - s.length();
        for (int i = 0; i < p; i++) {
            sb.append(c);
        }
    }

    public static boolean startsWithIgnoreCase(String name, String prefix) {
        return name.toLowerCase().startsWith(prefix.toLowerCase());
    }

    public static String removePrefix(String name, String prefix) {
        if (name.startsWith(prefix)) {
            return name.substring(prefix.length());
        } else {
            return name;
        }
    }

    public static String removePrefixIgnoreCase(String name, String prefix) {
        if (startsWithIgnoreCase(name, prefix)) {
            return name.substring(prefix.length());
        } else {
            return name;
        }
    }

    public static String removeSuffix(String name, String suffix) {
        if (name.endsWith(suffix)) {
            return name.substring(0, name.length() - suffix.length());
        } else {
            return name;
        }
    }

    public static String removeSuffixIgnoreCase(String name, String suffix) {
        if (endsWithIgnoreCase(name, suffix)) {
            return name.substring(0, name.length() - suffix.length());
        } else {
            return name;
        }
    }

    /**
     * * Replace all occurrences of a substring with another substring.
     */
    public static String replace(String str, String what, String with) {
        int pos = str.indexOf(what);
        if (pos == -1) {
            return str;
        }
        return str.substring(0, pos) + with + replace(str.substring(pos + what.length()), what, with);
    }

    /**
     * * Replace first occurrence of a substring with another substring.
     */
    public static String replaceFirst(String str, String what, String with) {
        int pos = str.indexOf(what);
        if (pos == -1) {
            return str;
        }
        return str.substring(0, pos) + with + str.substring(pos + what.length());
    }

    public static String reverse(String str) {
        int n = str.length();
        char[] chars = new char[n];
        for (int i = 0; i < n; i++) {
            chars[i] = str.charAt(n - i - 1);
        }
        return new String(chars);
    }

    public static String trimIfNotNull(String str) {
        return str == null ? null : str.trim();
    }
}
