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

package javax.activation;

import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * @version $Rev$ $Date$
 */
public class MimeTypeParameterList {
    private final static String PARAMETER_SEPARATOR = ";";
    private final static String NAME_VALUE_SEPARATOR = "=";

    Map _mimeTypeParameterMap = new HashMap();

    public MimeTypeParameterList() {

    }

    public MimeTypeParameterList(String parameterList) throws MimeTypeParseException {
        parse(parameterList);
    }

    protected void parse(String parameterList) throws MimeTypeParseException {
        if (parameterList == null) {
            return;
        }

        StringTokenizer tokenizer = new StringTokenizer(parameterList, PARAMETER_SEPARATOR);
        while (tokenizer.hasMoreTokens()) {
            String parameter = tokenizer.nextToken();
            if (parameter.length() == 0) {
                continue;
            }
            int eq = parameter.indexOf(NAME_VALUE_SEPARATOR);
            String name = null;
            if (eq > -1) {
                name = parseToken(parameter.substring(0, eq));
            }
            String value = parseToken(parameter.substring(eq + 1));
            if ((name == null || name.length() == 0) && value.length() == 0) {
                continue;
            }
            if (name.length() == 0 || value.length() == 0) {
                throw new MimeTypeParseException("Name or value is Missing");
            }
            set(name, value);

        }

    }

    public int size() {
        return _mimeTypeParameterMap.size();
    }

    public boolean isEmpty() {
        return _mimeTypeParameterMap.isEmpty();
    }

    public String get(String name) {
        return (String) _mimeTypeParameterMap.get(name);
    }

    public void set(String name, String value) {
        name = parseToken(name);
        value = parseToken(value);
        _mimeTypeParameterMap.put(name, value);
    }

    public void remove(String name) {
        _mimeTypeParameterMap.remove(name);
    }

    public Enumeration getNames() {
        return Collections.enumeration(_mimeTypeParameterMap.keySet());
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        for (Enumeration enum = getNames(); enum.hasMoreElements();) {
            buf.append(PARAMETER_SEPARATOR);
            String name = (String) enum.nextElement();
            buf.append(name).append(NAME_VALUE_SEPARATOR).append(get(name));
        }
        return buf.toString();
    }

    private String parseToken(String token) {
        // TODO it seems to have unauthorized chars
        return removeBlank(token);
    }

    private String removeBlank(String str) {
        StringBuffer buf = new StringBuffer();
        StringTokenizer tokenizer = new StringTokenizer(str);
        while (tokenizer.hasMoreTokens()) {
            buf.append(tokenizer.nextToken());
        }
        return buf.toString();
    }
}