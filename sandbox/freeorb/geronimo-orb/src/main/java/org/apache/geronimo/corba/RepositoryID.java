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
package org.apache.geronimo.corba;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.corba.util.StringUtil;


public abstract class RepositoryID {

    static final Log log = LogFactory.getLog(RepositoryID.class);

    public static String idToClassName(String repid) {
        // debug
        if (log.isDebugEnabled()) {
            log.debug("idToClassName " + repid);
        }

        if (repid.startsWith("IDL:")) {

            String id = repid;

            try {
                int end = id.lastIndexOf(':');
                String s = end < 0
                           ? id.substring(4)
                           : id.substring(4, end);

                StringBuffer bb = new StringBuffer();

                //
                // reverse order of dot-separated name components up
                // till the first slash.
                //
                int firstSlash = s.indexOf('/');
                if (firstSlash > 0) {
                    String prefix = s.substring(0, firstSlash);
                    String[] elems = StringUtil.split(prefix, '.');

                    for (int i = elems.length - 1; i >= 0; i--) {
                        bb.append(fixName(elems[i]));
                        bb.append('.');
                    }

                    s = s.substring(firstSlash + 1);
                }

                //
                // Append slash-separated name components ...
                //
                String[] elems = StringUtil.split(s, '/');
                for (int i = 0; i < elems.length; i++) {
                    bb.append(fixName(elems[i]));
                    if (i != elems.length - 1)
                        bb.append('.');
                }

                String result = bb.toString();

                if (log.isDebugEnabled()) {
                    log.debug("idToClassName " + repid + " => " + result);
                }

                return result;
            }
            catch (IndexOutOfBoundsException ex) {
                log.error("idToClass", ex);
                return null;
            }

        } else if (repid.startsWith("RMI:")) {
            int end = repid.indexOf(':', 4);
            return end < 0
                   ? repid.substring(4)
                   : repid.substring(4, end);
        }

        return null;
    }

    static String fixName(String name) {

        if (keyWords.contains(name)) {
            StringBuffer buf = new StringBuffer();
            buf.append('_');
            buf.append(name);
            return buf.toString();
        }

        String result = name;
        String current = name;

        boolean match = true;
        while (match) {

            int len = current.length();
            match = false;

            for (int i = 0; i < reservedPostfixes.length; i++) {
                if (current.endsWith(reservedPostfixes[i])) {
                    StringBuffer buf = new StringBuffer();
                    buf.append('_');
                    buf.append(result);
                    result = buf.toString();

                    int resultLen = reservedPostfixes[i].length();
                    if (len > resultLen)
                        current = current.substring(0,
                                                    len - resultLen);
                    else
                        current = "";

                    match = true;
                    break;
                }
            }

        }

        return name;
    }

    static final java.util.Set keyWords = new java.util.HashSet();
    static final String[] reservedPostfixes = new String[]{
            "Helper",
            "Holder",
            "Operations",
            "POA",
            "POATie",
            "Package",
            "ValueFactory"
    };

    static {
        String[] words = {
                "abstract", "boolean", "break", "byte", "case", "catch",
                "char", "class", "clone", "const", "continue", "default",
                "do", "double", "else", "equals", "extends", "false",
                "final", "finalize", "finally", "float", "for",
                "getClass", "goto", "hashCode", "if", "implements",
                "import", "instanceof", "int", "interface", "long",
                "native", "new", "notify", "notifyAll", "null", "package",
                "private", "protected", "public", "return", "short",
                "static", "super", "switch", "synchronized", "this",
                "throw", "throws", "toString", "transient", "true", "try",
                "void", "volatile", "wait", "while"
        };

        for (int i = 0; i < words.length; i++) {
            keyWords.add(words[i]);
        }
    }


}
