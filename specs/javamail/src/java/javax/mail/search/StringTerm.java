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

package javax.mail.search;

/**
 * A Term that provides matching criteria for Strings.
 *
 * @version $Rev$ $Date$
 */
public abstract class StringTerm extends SearchTerm {
    /**
     * If true, case should be ignored during matching.
     */
    protected boolean ignoreCase;

    /**
     * The pattern associated with this term.
     */
    protected String pattern;

    /**
     * Constructor specifying a pattern.
     * Defaults to case insensitive matching.
     * @param pattern the pattern for this term
     */
    protected StringTerm(String pattern) {
        this(pattern, true);
    }

    /**
     * Constructor specifying pattern and case sensitivity.
     * @param pattern the pattern for this term
     * @param ignoreCase if true, case should be ignored during matching
     */
    protected StringTerm(String pattern, boolean ignoreCase) {
        this.pattern = pattern;
        this.ignoreCase = ignoreCase;
    }

    /**
     * Return the pattern associated with this term.
     * @return the pattern associated with this term
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Indicate if case should be ignored when matching.
     * @return if true, case should be ignored during matching
     */
    public boolean getIgnoreCase() {
        return ignoreCase;
    }

    /**
     * Determine if the pattern associated with this term is a substring of the
     * supplied String. If ignoreCase is true then case will be ignored.
     *
     * @param match the String to compare to
     * @return true if this patter is a substring of the supplied String
     */
    protected boolean match(String match) {
        match: for (int length = match.length() - pattern.length(); length > 0; length--) {
            for (int i = 0; i < pattern.length(); i++) {
                char c1 = match.charAt(length + i);
                char c2 = match.charAt(i);
                if (c1 == c2) {
                    continue;
                }
                if (ignoreCase) {
                    if (Character.toLowerCase(c1) == Character.toLowerCase(c2)) {
                        continue;
                    }
                    if (Character.toUpperCase(c1) == Character.toUpperCase(c2)) {
                        continue;
                    }
                }
                continue match;
            }
            return true;
        }
        return false;
    }

    public boolean equals(Object other) {
        return super.equals(other)
                && ((StringTerm) other).pattern.equals(pattern)
                && ((StringTerm) other).ignoreCase == ignoreCase;
    }

    public int hashCode() {
        return super.hashCode() + pattern.hashCode() + (ignoreCase ? 32 : 79);
    }
}
