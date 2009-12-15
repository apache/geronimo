/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.geronimo.kernel.osgi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class to parse standard OSGi headers.
 *
 * @version $Rev$, $Date$
 */
public class HeaderParser  {

    /**
     * Parse a given OSGi header into a list of header elements.
     *
     * @param header the OSGi header to parse
     * @return the list of header elements extracted from this header
     */
    public static List<HeaderElement> parseHeader(String header) {
        List<HeaderElement> elements = new ArrayList<HeaderElement>();
        if (header == null || header.trim().length() == 0) {
            return elements;
        }
        List<String> clauses = parseDelimitedString(header, ",");
        for (String clause : clauses) {
            String[] tokens = clause.split(";");
            if (tokens.length < 1) {
                throw new IllegalArgumentException("Invalid header clause: " + clause);
            }
            HeaderElement elem = new HeaderElement(tokens[0].trim());
            elements.add(elem);
            for (int i = 1; i < tokens.length; i++) {
                int pos = tokens[i].indexOf('=');
                if (pos != -1) {
                    if (pos > 0 && tokens[i].charAt(pos - 1) == ':') {
                        String name = tokens[i].substring(0, pos - 1).trim();
                        String value = tokens[i].substring(pos + 1).trim();
                        elem.addDirective(name, value);
                    } else {
                        String name = tokens[i].substring(0, pos).trim();
                        String value = tokens[i].substring(pos + 1).trim();
                        elem.addAttribute(name, value);
                    }
                } else {
                    elem = new HeaderElement(tokens[i].trim());
                    elements.add(elem);
                }
            }
        }
        return elements;
    }

    private static List<String> parseDelimitedString(String value, String delim) {   
        if (value == null) {       
            value = "";
        }

        List<String> list = new ArrayList<String>();

        int CHAR = 1;
        int DELIMITER = 2;
        int STARTQUOTE = 4;
        int ENDQUOTE = 8;

        StringBuffer sb = new StringBuffer();

        int expecting = (CHAR | DELIMITER | STARTQUOTE);

        for (int i = 0; i < value.length(); i++) {        
            char c = value.charAt(i);

            boolean isDelimiter = (delim.indexOf(c) >= 0);
            boolean isQuote = (c == '"');

            if (isDelimiter && ((expecting & DELIMITER) > 0)) {            
                list.add(sb.toString().trim());
                sb.delete(0, sb.length());
                expecting = (CHAR | DELIMITER | STARTQUOTE);
            } else if (isQuote && ((expecting & STARTQUOTE) > 0)) {            
                sb.append(c);
                expecting = CHAR | ENDQUOTE;
            } else if (isQuote && ((expecting & ENDQUOTE) > 0)) {           
                sb.append(c);
                expecting = (CHAR | STARTQUOTE | DELIMITER);
            } else if ((expecting & CHAR) > 0) {            
                sb.append(c);
            } else {
                throw new IllegalArgumentException("Invalid delimited string: " + value);
            }
        }

        if (sb.length() > 0) {        
            list.add(sb.toString().trim());
        }

        return list;
    }
    
    public static class HeaderElement {
        
        private String path;
        private Map<String, String> attributes;
        private Map<String, String> directives;
        
        public HeaderElement(String path) {
            this.path = path;
            this.attributes = new HashMap<String, String>();
            this.directives = new HashMap<String, String>();
        }
        
        public String getName() {
            return this.path;
        }
        
        public Map<String, String> getAttributes() {
            return attributes;
        }
        
        public String getAttribute(String name) {
            return attributes.get(name);
        }
        
        public void addAttribute(String name, String value) {
            attributes.put(name, value);
        }
        
        public Map<String, String> getDirectives() {
            return directives;
        }
        
        public String getDirective(String name) {
            return directives.get(name);
        }
        
        public void addDirective(String name, String value) {
            directives.put(name, value);
        }        
        
    }
}
