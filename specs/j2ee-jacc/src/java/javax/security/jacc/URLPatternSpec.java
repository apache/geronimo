/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * This source code implements specifications defined by the Java
 * Community Process. In order to remain compliant with the specification
 * DO NOT add / change / or delete method signatures!
 *
 * ====================================================================
 */

package javax.security.jacc;

import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/30 01:55:13 $
 */
class URLPatternSpec {

    private String pattern;
    private URLPattern first;
    private LinkedList qualifiers = new LinkedList();

    public URLPatternSpec(String name) {
        if (name == null) throw new java.lang.IllegalArgumentException("URLPatternSpec cannot be null");
        if (name.length() == 0) name = "/";

        pattern = name;

        String[] tokens = pattern.split(":", -1);
        first = new URLPattern(tokens[0]);

        URLPattern candidate;
        for (int i=1; i<tokens.length; i++) {
            candidate = new URLPattern(tokens[i]);

            // No pattern may exist in the URLPatternList that matches the first pattern.
            if (candidate.matches(first)) {
                throw new java.lang.IllegalArgumentException("Qualifier patterns in the URLPatternSpec cannot match the first URLPattern");
            }

            if (first.type == URLPattern.PATH_PREFIX ) {

                // If the first pattern is a path-prefix pattern, only exact patterns
                // matched by the first pattern and path-prefix patterns matched by,
                // but different from, the first pattern may occur in the URLPatternList.

                if (candidate.type == URLPattern.EXACT && !first.matches(candidate))
                {
                    throw new java.lang.IllegalArgumentException("Exact qualifier patterns in the URLPatternSpec must be matched by the first URLPattern");
                }
                else if (candidate.type == URLPattern.PATH_PREFIX
                         && !(first.matches(candidate) && first.pattern.length() < candidate.pattern.length()))
                {
                    throw new java.lang.IllegalArgumentException("path-prefix qualifier patterns in the URLPatternSpec must be matched by, but different from, the first URLPattern");
                } else if (candidate.type == URLPattern.EXTENSION) {
                    throw new java.lang.IllegalArgumentException("extension qualifier patterns in the URLPatternSpec are not allowed when the first URLPattern is path-prefix");
                }
            } else if (first.type == URLPattern.EXTENSION) {

                // If the first pattern is an extension pattern, only exact patterns
                // that are matched by the first pattern and path-prefix patterns may
                // occur in the URLPatternList.

                if (candidate.type == URLPattern.EXACT && !first.matches(candidate)) {
                    throw new java.lang.IllegalArgumentException("Exact qualifier patterns in the URLPatternSpec must be matched when first URLPattern is an extension pattern");
                } else if (candidate.type != URLPattern.PATH_PREFIX) {
                    throw new java.lang.IllegalArgumentException("Only exact and path-prefix qualifiers in the URLPatternSpec are allowed when first URLPattern is an extension pattern");
                }
            } else if (first.type == URLPattern.DEFAULT) {

                // If the first pattern is the default pattern, "/", any pattern
                // except the default pattern may occur in the URLPatternList.

                if (candidate.type == URLPattern.DEFAULT) {
                    throw new java.lang.IllegalArgumentException("Qualifier patterns must not be default when first URLPattern is a default pattern");
                }
            } else if (first.type == URLPattern.EXACT) {

                // If the first pattern is an exact pattern a URLPatternList
                // must not be present in the URLPatternSpec

                throw new java.lang.IllegalArgumentException("Qualifier patterns must be present when first URLPattern is an exact pattern");
            }

            qualifiers.add(candidate);
        }
    }

    public boolean equals(URLPatternSpec o) {
        return implies(o) && o.implies(this);
    }

    public int hashCode() {
        return pattern.hashCode();
    }

    public String getPatternSpec() {
        return pattern;
    }

    public boolean implies(URLPatternSpec p) {

        // The first URLPattern in the name of the argument permission is
        // matched by the first URLPattern in the name of this permission.
        if (!first.matches(p.first)) return false;

        // The first URLPattern in the name of the argument permission is NOT
        // matched by any URLPattern in the URLPatternList of the URLPatternSpec
        // of this permission.
        Iterator iter1 = qualifiers.iterator();
        while (iter1.hasNext()) {
            if (((URLPattern)iter1.next()).matches(p.first)) return false;
        }

        // If the first URLPattern in the name of the argument permission
        // matches the first URLPattern in the URLPatternSpec of this
        // permission, then every URLPattern in the URLPatternList of the
        // URLPatternSpec of this permission is matched by a URLPattern in
        // the URLPatternList of the argument permission.
        if (p.first.matches(first)) {
            Iterator iter2 = p.qualifiers.iterator();

            while (iter2.hasNext()) {
                Iterator iter3 = qualifiers.iterator();
                URLPattern test = (URLPattern)iter2.next();
                boolean found = false;

                while (iter3.hasNext()) {
                    if (test.matches((URLPattern)iter3.next())) {
                        found = true;
                        break;
                    }
                }
                if (!found) return false;
            }
        }

        return true;
    }

    private class URLPattern {
        public final static int EXACT       = 0x0;
        public final static int PATH_PREFIX = 0x1;
        public final static int EXTENSION   = 0x2;
        public final static int DEFAULT     = 0x4;

        public int type;
        public String pattern;

        public URLPattern(String pat) {
            if (pat == null) throw new java.lang.IllegalArgumentException("URLPattern cannot be null");
            if (pat.length() == 0) throw new java.lang.IllegalArgumentException("URLPattern cannot be empty");

            if (pat.equals("/") || pat.equals("/*") ) {
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

        public boolean matches(URLPattern p) {

            String test = p.pattern;

            // their pattern values are String equivalent
            if (pattern.equals(test)) return true;

            switch (type) {

                // this pattern is a path-prefix pattern (that is, it starts
                // with "/" and ends with "/*") and the argument pattern
                // starts with the substring of this pattern, minus its last
                // 2 characters, and the next character of the argument pattern,
                // if there is one, is "/"
                case PATH_PREFIX: {
                    int length = pattern.length()-2;
                    if (length > test.length()) return false;

                    for (int i=0; i<length; i++) {
                        if (pattern.charAt(i) != test.charAt(i)) return false;
                    }

                    if (test.length() == length) return true;
                    else if (test.charAt(length) != '/') return false;

                    return true;
                }

                // this pattern is an extension pattern (that is, it starts
                // with "*.") and the argument pattern ends with this pattern
                case EXTENSION: {
                    return test.endsWith(pattern.substring(1));
                }

                // this pattern is the path-prefix pattern "/*" or
                // the reference pattern is the special default pattern,
                // "/", which matches all argument patterns
                case DEFAULT: {
                    return true;
                }
            }
            return false;
        }
    }
}
