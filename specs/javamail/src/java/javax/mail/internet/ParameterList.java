/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package javax.mail.internet;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;// Represents lists in things like
// Content-Type: text/plain;charset=klingon
//
// The ;charset=klingon is the parameter list, may have more of them with ';'

/**
 * @version $Rev$ $Date$
 */
public class ParameterList {
    private Map _parameters = new HashMap();

    public ParameterList() {
    }

    public ParameterList(String list) throws ParseException {
        if (list == null) {
            return;
        } else {
            String[] tokens = split(list,';');
            for (int i=0;tokens != null && i<tokens.length;i++) {
                String parameter = tokens[i];
                int eq = parameter.indexOf("=");
                if (eq == -1) {
                    throw new ParseException(parameter);
                } else {
                    String name = parameter.substring(0, eq);
                    String value = parameter.substring(eq + 1).trim();
                    if (value.charAt(0) == '"') {
                        int end = value.lastIndexOf('"');
                        value = value.substring(1, end);
                    }
                    set(name, value);
                }
            }
        }
    }

    public int size() {
        return _parameters.size();
    }

    public String get(String name) {
        return (String) _parameters.get(name);
    }

    public void set(String name, String value) {
        _parameters.put(name.trim(), value.trim());
    }

    public void remove(String name) {
        _parameters.remove(name);
    }

    public Enumeration getNames() {
        return Collections.enumeration(_parameters.keySet());
    }

    public String toString() {
        Iterator it = _parameters.entrySet().iterator();
        StringBuffer result = new StringBuffer();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            result.append(";");
            result.append(entry.getKey());
            result.append("=");
            result.append(entry.getValue());
        }
        return result.toString();
        // TODO Return in same list as parsed format
    }

    private static String[] split(String str, char separatorChar) {
        if (str == null) {
            return null;
        }
        int len = str.length();
        if (len == 0) {
            return new String[0];
        }
        List list = new ArrayList();
        int i = 0, start = 0;
        boolean match = false;
        while (i < len) {
            char ch = str.charAt(i);
            // Skip any separatorChar within quotes
            if(ch == '\"') {
                i++;
                while(i < len && str.charAt(i) != '\"'){
                    i++;
                }
                if(i < len){
                    i++;
                    continue;
                }
            }
            if (ch == separatorChar) {
                if (match) {
                    list.add(str.substring(start, i));
                    match = false;
                }
                start = ++i;
                continue;
            }
            match = true;
            i++;
        }
        if (match) {
            list.add(str.substring(start, i));
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    public String toString(int lineBreak) {
        // figure out where to break the line
        String answer = toString();
        if (answer.length() > lineBreak) {
            // convert it to substring
            // TODO Implement
            return "";
        } else {
            return answer;
        }
    }
}
