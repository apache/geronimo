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

package org.apache.geronimo.security.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * Utility class for <code>WebModuleConfiguration</code>.  This class is used to generate qualified patterns, HTTP
 * method sets, complements of HTTP method sets, and HTTP method sets w/ transport restrictions for URL patterns that
 * are found in the web deployment descriptor.
 * @version $Revision: 1.5 $ $Date: 2004/06/27 18:12:48 $
 * @see org.apache.geronimo.security.jacc.WebModuleConfiguration
 */
public class URLPattern {
    private final static String[] HTTP_METHODS = {"GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS", "TRACE"};
    private final static int[] HTTP_MASKS = {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40};
    private final static int NA = 0x00;
    private final static int INTEGRAL = 0x01;
    private final static int CONFIDENTIAL = 0x02;

    private URLPatternCheck type;
    private String pattern;
    private int mask;
    private int transport;
    private HashSet roles = new HashSet();

    /**
     * Construct an instance of the utility class for <code>WebModuleConfiguration</code>.
     * @param pat the URL pattern that this instance is to collect information on
     * @see org.apache.geronimo.security.jacc.WebModuleConfiguration
     * @see "JSR 115, section 3.1.3" Translating Servlet Deployment Descriptors
     */
    public URLPattern(String pat) {
        if (pat == null) throw new java.lang.IllegalArgumentException("URL pattern cannot be null");
        if (pat.length() == 0) throw new java.lang.IllegalArgumentException("URL pattern cannot be empty");

        if (pat.equals("/") || pat.equals("/*")) {
            type = DEFAULT;
        } else if (pat.charAt(0) == '/' && pat.endsWith("/*")) {
            type = PATH_PREFIX;
        } else if (pat.charAt(0) == '*') {
            type = EXTENSION;
        } else {
            type = EXACT;
        }
        pattern = pat;
    }

    /**
     * Get a qualifed URL pattern relative to a particular set of URL patterns.  This algorithm is described in
     * JSR 115, section 3.1.3.1 "Qualified URL Pattern Names".
     * @param patterns the set of possible URL patterns that could be used to qualify this pattern
     * @return a qualifed URL pattern
     */
    public String getQualifiedPattern(Set patterns) {
        if (type == EXACT) {
            return pattern;
        } else {
            HashSet bucket = new HashSet();
            StringBuffer result = new StringBuffer(pattern);
            Iterator iter = patterns.iterator();

            // Collect a set of qualifying patterns, depending on the type of this pattern.
            while (iter.hasNext()) {
                URLPattern p = (URLPattern) iter.next();
                if (type.check(this, p)) {
                    bucket.add(p.pattern);
                }
            }

            // append the set of qualifying patterns
            iter = bucket.iterator();
            while (iter.hasNext()) {
                result.append(':');
                result.append((String) iter.next());
            }
            return result.toString();
        }
    }

    /**
     * Add a method to the union of HTTP methods associated with this URL pattern.  An empty string is short hand for
     * the set of all HTTP methods.
     * @param method the HTTP method to be added to the set.
     */
    public void addMethod(String method) {
        if (method.length() == 0) {
            mask = 0xFF;
            return;
        }

        boolean found = false;
        for (int j = 0; j < HTTP_METHODS.length; j++) {
            if (method.equals(HTTP_METHODS[j])) {
                mask |= HTTP_MASKS[j];
                found = true;

                break;
            }
        }
        if (!found) throw new IllegalArgumentException("Invalid HTTP method");
    }

    /**
     * Return the set of HTTP methods that have been associated with this URL pattern.
     * @return a set of HTTP methods
     */
    public String getMethods() {
        StringBuffer buffer = null;

        for (int i = 0; i < HTTP_MASKS.length; i++) {
            if ((mask & HTTP_MASKS[i]) > 0) {
                if (buffer == null) {
                    buffer = new StringBuffer();
                } else {
                    buffer.append(",");
                }
                buffer.append(HTTP_METHODS[i]);
            }
        }

        return (buffer == null ? "" : buffer.toString());
    }

    public String getComplementedMethods() {
        StringBuffer buffer = null;

        for (int i = 0; i < HTTP_MASKS.length; i++) {
            if ((mask & HTTP_MASKS[i]) == 0) {
                if (buffer == null) {
                    buffer = new StringBuffer();
                } else {
                    buffer.append(",");
                }
                buffer.append(HTTP_METHODS[i]);
            }
        }

        return (buffer == null ? "" : buffer.toString());
    }

    public String getMethodsWithTransport() {
        StringBuffer buffer = new StringBuffer(getMethods());


        if (transport != NA) {
            buffer.append(":");

            if (transport != 0x03) {
                if (transport == INTEGRAL) {
                    buffer.append("INTEGRAL");
                } else {
                    buffer.append("CONFIDENTIAL");
                }
            }
        }

        return buffer.toString();
    }

    public void setTransport(String trans) {
        switch (transport) {
            case NA:
                {
                    if ("INTEGRAL".equals(trans)) {
                        transport = INTEGRAL;
                    } else if ("CONFIDENTIAL".equals(trans)) {
                        transport = CONFIDENTIAL;
                    }
                    break;
                }

            case INTEGRAL:
                {
                    if ("CONFIDENTIAL".equals(trans)) {
                        transport = CONFIDENTIAL;
                    }
                    break;
                }
        }
    }

    public void addRole(String role) {
        roles.add(role);
    }

    public void addAllRoles(Collection collection) {
        roles.addAll(collection);
    }

    public HashSet getRoles() {
        return roles;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof URLPattern)) return false;

        URLPattern test = (URLPattern) obj;

        return pattern.equals(test.pattern);
    }

    public int hashCode() {
        return pattern.hashCode();
    }

    boolean matches(URLPattern p) {
        String test = p.pattern;

        // their pattern values are String equivalent
        if (pattern.equals(test)) return true;

        return type.matches(pattern, test);
    }

    private final static URLPatternCheck EXACT = new URLPatternCheck() {
        public boolean check(URLPattern base, URLPattern test) {
            return matches(base.pattern, test.pattern);
        }

        public boolean matches(String base, String test) {
            return base.equals(test);
        }
    };

    private final static URLPatternCheck PATH_PREFIX = new URLPatternCheck() {
        public boolean check(URLPattern base, URLPattern test) {
            return ((test.type == PATH_PREFIX || test.type == EXACT)
                    && base.matches(test)
                    && !base.equals(test));
        }

        /**
         * This pattern is a path-prefix pattern (that is, it starts with "/" and ends with "/*") and the argument
         * pattern starts with the substring of this pattern, minus its last 2 characters, and the next character of
         * the argument pattern, if there is one, is "/"
         * @param base the base pattern
         * @param test the pattern to be tested
         * @return <code>true</code> if <code>test</code> is matched by <code>base</code>
         */
        public boolean matches(String base, String test) {
            int length = base.length() - 2;
            if (length > test.length()) return false;

            for (int i = 0; i < length; i++) {
                if (base.charAt(i) != test.charAt(i)) return false;
            }

            if (test.length() == length)
                return true;
            else if (test.charAt(length) != '/') return false;

            return true;
        }
    };

    private final static URLPatternCheck EXTENSION = new URLPatternCheck() {
        public boolean check(URLPattern base, URLPattern test) {
            if (test.type == PATH_PREFIX) return true;

            if (test.type == EXACT) return matches(base.pattern, test.pattern);

            return false;
        }

        /**
         * This pattern is an extension pattern (that is, it startswith "*.") and the argument pattern ends with
         * this pattern.
         * @param base the base pattern
         * @param test the pattern to be tested
         * @return <code>true</code> if <code>test</code> is matched by <code>base</code>
         */
        public boolean matches(String base, String test) {
            return test.endsWith(base.substring(1));
        }
    };

    private final static URLPatternCheck DEFAULT = new URLPatternCheck() {
        public boolean check(URLPattern base, URLPattern test) {
            return base.matches(test) && !base.equals(test);
        }

        /**
         * This pattern is the path-prefix pattern "/*" or the reference pattern is the special default pattern,
         * "/", which matches all argument patterns.
         * @param base the base pattern
         * @param test the pattern to be tested
         * @return <code>true</code> if <code>test</code> is matched by <code>base</code>
         * @see "JSR 115"
         */
        public boolean matches(String base, String test) {
            return true;
        }
    };
}
