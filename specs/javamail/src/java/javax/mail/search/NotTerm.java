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
import javax.mail.Message;
/**
 * @version $Rev$ $Date$
 */
public final class NotTerm extends SearchTerm {
    protected SearchTerm term;
    public NotTerm(SearchTerm term) {
        this.term = term;
    }
    public boolean equals(Object other) {
        return super.equals(other) && ((NotTerm) other).term.equals(term);
    }
    public SearchTerm getTerm() {
        return term;
    }
    public int hashCode() {
        return super.hashCode() + term.hashCode();
    }
    public boolean match(Message message) {
        return !term.match(message);
    }
}
