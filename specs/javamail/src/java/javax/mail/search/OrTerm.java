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

import java.util.Arrays;
import javax.mail.Message;

/**
 * @version $Rev$ $Date$
 */
public final class OrTerm extends SearchTerm {
    protected SearchTerm[] terms;

    public OrTerm(SearchTerm a, SearchTerm b) {
        terms = new SearchTerm[]{a, b};
    }

    public OrTerm(SearchTerm[] terms) {
        this.terms = terms;
    }

    public boolean equals(Object other) {
        return super.equals(other)
                && Arrays.equals(terms, ((OrTerm) other).terms);
    }

    public SearchTerm[] getTerms() {
        return terms;
    }

    public int hashCode() {
        return super.hashCode() + terms.length * 37;
    }

    public boolean match(Message message) {
        boolean result = false;
        for (int i = 0; (!result) && i < terms.length; i++) {
            SearchTerm term = terms[i];
            result = term.match(message);
        }
        return result;
    }
}
