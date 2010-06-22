/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" 
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package org.apache.geronimo.console.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;


/**
 * Heavily modified code from Apache JetSpeed v2.1.3 -
 *     jetspeed-2.1.3/src/components/portal/src/java/org/apache/jetspeed/engine/servlet/XXSUrlAttackFilter.java
 *
 * Simple XSS Url attack protection blocking access whenever the request url
 * contains a &lt; or &quot; character.
 * Modified to include basic XSS POST parameter protection and logging.
 * 
 * @version $Rev$ $Date$
 */
public class XSSHandler {
    private static final Logger log = LoggerFactory.getLogger(XSSHandler.class);

    /**
     * Default constructor
     */
    public XSSHandler() {
    }

    /**
     * Block simple XSS attacks in GET request URIs
     * @param hreq
     * @return true if we find %lt; or &quot; in the URI or Query string, otherwise false
     */
    public boolean isInvalidURI(HttpServletRequest hreq) {
        if (isInvalidString(hreq.getRequestURI()) ||
            isInvalidString(hreq.getQueryString())) {
            return true;
        }
        return false;
    }

    /**
     * Block simple XSS attacks in POST parameters
     * Note: Portlet/webapp should perform more complex field validation
     * @param hreq
     * @return true if any session params were invalid, otherwise false
     */
    public boolean isInvalidParameters(HttpServletRequest hreq) {

        for (Enumeration<String> e = hreq.getParameterNames(); e.hasMoreElements();) {
            String name = e.nextElement();
            String name2 = name.trim().toLowerCase();
            if (name2.startsWith("noxss")) {
                log.debug("Skipping specially marked parameter=" + name);
                continue;
            }
            else if ((name2.startsWith("minxss")) || (name2.indexOf("password") != -1) || (name2.indexOf("xml") != -1) || (name2.indexOf("sql") != -1)) {
                // perform a "minimal" but more CPU intensive set of checks on
                // these parameter value(s) which can allow &lt; and &quot; usage
                String[] vals = hreq.getParameterValues(name);
                for (String value : vals) {
                    if (isInvalidParamLmt(value)) {
                        // should be safe to log the uri, as we've already run isInvalidURI() on it
                        log.warn("Blocking request due to known XSS content in parameter=" + name + " for uri=" + hreq.getRequestURI());
                        return true;
                    }
                }
            }
            else {
                String[] vals = hreq.getParameterValues(name);
                for (String value : vals) {
                    if (isInvalidParam(value)) {
                        // should be safe to log the uri, as we've already run isInvalidURI() on it
                        log.warn("Blocking request due to potential XSS content in parameter=" + name + " for uri=" + hreq.getRequestURI());
                        return true;
                    }
                }
            }

        }
        return false;
    }

    /**
     * Searches the given string for any &lt; or &quot; instances
     * @param value
     * @return true if we find &lt; or &quot; anywhere in the string, otherwise false
     */
    private boolean isInvalidString(String value) {
        if (value != null) {
            try {
                String s = URLDecoder.decode(value, "UTF-8").toLowerCase();
                if ((s.indexOf('<') != -1) || (s.indexOf('"') != -1)) {
                    return true;
                }
            }
            catch (UnsupportedEncodingException uee) {
                // should never happen
                log.error("URLDecoder.decode(UTF8) failed.", uee);
            }
        }
        return false;
    }
    
    /**
     * This is a copy of isInvalidString expect the elimination of URLDecoder.
     * Searches the given string for any &lt; or &quot; instances
     * @param value
     * @return true if we find &lt; or &quot; anywhere in the string, otherwise false
     */
    private boolean isInvalidParam(String value) {
        if (value != null) {
            String s = value.toLowerCase();
            if ((s.indexOf('<') != -1) || (s.indexOf('"') != -1)) {
                return true;
            }
        }
        return false;
    }

    /**
     * More limited version of the isInvalidParam() method, in which we only
     * check for: <script, <img, <iframe, <div and style= tags in the string.
     * @param value
     * @return true if we find:
     *      1) <script, <img, <iframe or <div or
     *      2) style= anywhere in the string
     *      else false
     */
    private boolean isInvalidParamLmt(String value) {
        if (value != null) {
            String s = value.toLowerCase();
            int offset = s.indexOf('<');
            while (offset != -1) {
                // increment past the "<"
                offset++;
                // if we found a start tag in the param, lets dig deeper...
                if (containsScript(s, offset) || containsImg(s, offset) ||
                    containsIframe(s, offset) || containsDiv(s, offset)) {
                    // we found a hit
                    return true;
                }
                else {
                    // look for another set of tags in the string
                    offset = s.indexOf('<', offset);
                }
            }
            // also need to check for style= usage
            return(containsStyle(s));
        }
        return false;
    }

    /**
     * Check for script tag at start of a URLDecoder.decode().toLowerCase() String
     * @param value
     * @param index
     * @return true if string starts with "script", else false
     */
    private boolean containsScript(String value, int index) {
        int offset = index;
        if ((value.charAt(offset) == 's') &&
            (value.charAt(++offset) == 'c') &&
            (value.charAt(++offset) == 'r') &&
            (value.charAt(++offset) == 'i') &&
            (value.charAt(++offset) == 'p') &&
            (value.charAt(++offset) == 't')) {
            log.debug("Found a '<script' tag in a HttpServletRequest parameter.");
            return true;
        }
        return false;
    }

    /**
     * Check for img tag at start of a URLDecoder.decode().toLowerCase() String
     * @param value
     * @param index
     * @return true if string starts with "img", else false
     */
    private boolean containsImg(String value, int index) {
        int offset = index;
        if ((value.charAt(offset) == 'i') &&
            (value.charAt(++offset) == 'm') &&
            (value.charAt(++offset) == 'g')) {
            log.debug("Found a '<img' tag in a HttpServletRequest parameter.");
            return true;
        }
        return false;
    }

    /**
     * Check for iframe tag at start of a URLDecoder.decode().toLowerCase() String
     * @param value
     * @param index
     * @return true if string starts with "iframe", else false
     */
    private boolean containsIframe(String value, int index) {
        int offset = index;
        if ((value.charAt(offset) == 'i') &&
            (value.charAt(++offset) == 'f') &&
            (value.charAt(++offset) == 'r') &&
            (value.charAt(++offset) == 'a') &&
            (value.charAt(++offset) == 'm') &&
            (value.charAt(++offset) == 'e')) {
            log.debug("Found a '<iframe' tag in a HttpServletRequest parameter.");
            return true;
        }
        return false;
    }

    /**
     * Check for div tag at start of a URLDecoder.decode().toLowerCase() String
     * @param value
     * @param index
     * @return true if string starts with "div", else false
     */
    private boolean containsDiv(String value, int index) {
        int offset = index;
        if ((value.charAt(offset) == 'd') &&
            (value.charAt(++offset) == 'i') &&
            (value.charAt(++offset) == 'v')) {
            log.debug("Found a '<div' tag in a HttpServletRequest parameter.");
            return true;
        }
        return false;
    }

    /**
     * Check for style= tag anywhere in a URLDecoder.decode().toLowerCase() String
     * @param value
     * @return true if string contains "style=", else false
     */
    private boolean containsStyle(String value) {
        String style = "style=";
        if (value.indexOf(style) != -1) {
            log.debug("Found a 'style=' tag in a HttpServletRequest parameter.");
            return true;
        }
        return false;
    }

}
