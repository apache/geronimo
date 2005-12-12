/**
 *
 * Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
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
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.corba.util;

import java.util.ArrayList;


/**
 * @author Jeppe Sommer (jso@eos.dk)
 * @author Kim Harding Christensen (khc@eos.dk)
 */
public class StringUtil {

    public static String capitalize(String str) {
        StringBuffer sb = new StringBuffer(str);
        sb.setCharAt(0, Character.toUpperCase(str.charAt(0)));
        return sb.toString();
    }

    public static String join(String[] strings, String delimiter) {
        StringBuffer sb = new StringBuffer();
        for (int n = 0; n < strings.length; n++) {
            sb.append(strings[n]);
            if (n < strings.length - 1) {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }

    public static String[] split(String str, char splitChar) {
        ArrayList al = new ArrayList(8);
        int i = -1;
        String s;
        String rest = str;
        while ((i = rest.indexOf(splitChar)) != -1) {
            s = rest.substring(0, i);
            al.add(s);
            rest = rest.substring(i + 1);
        }
        al.add(rest);
        String[] result = new String[al.size()];
        al.toArray(result);
        return result;
    }

    public static String replace(String str, String oldStr, String newStr) {
        int prevIndex = 0, nextIndex;
        StringBuffer result = new StringBuffer();

        while ((nextIndex = str.indexOf(oldStr, prevIndex)) != -1) {
            result.append(str.substring(prevIndex, nextIndex));
            result.append(newStr);
            prevIndex = nextIndex + oldStr.length();
        }

        result.append(str.substring(prevIndex));

        return result.toString();
    }
}

