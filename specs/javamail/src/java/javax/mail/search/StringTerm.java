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
 * @version $Rev$ $Date$
 */
public abstract class StringTerm extends SearchTerm {
    protected boolean ignoreCase;
    protected String pattern;

    protected StringTerm(String pattern) {
        this(pattern, true);
    }

    protected StringTerm(String pattern, boolean ignoreCase) {
        this.pattern = pattern;
        this.ignoreCase = ignoreCase;
    }

    public boolean equals(Object other) {
        return super.equals(other)
                && ((StringTerm) other).pattern.equals(pattern)
                && ((StringTerm) other).ignoreCase == ignoreCase;
    }

    public boolean getIgnoreCase() {
        return ignoreCase;
    }

    public String getPattern() {
        return pattern;
    }

    public int hashCode() {
        return super.hashCode() + pattern.hashCode() + (ignoreCase ? 32 : 79);
    }

    protected boolean match(String match) {
        String a = match;
        String b = pattern;
        if (ignoreCase) {
            a = a.toUpperCase();
            b = b.toUpperCase();
        }
        return a.indexOf(b) != -1;
    }
}
