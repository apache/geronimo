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

import java.io.Serializable;
import javax.mail.Message;

/**
 * Base class for Terms in a parse tree used to represent a search condition.
 *
 * This class is Serializable to allow for the short term persistence of
 * searches between Sessions; this is not intended for long-term persistence.
 *
 * @version $Rev$ $Date$
 */
public abstract class SearchTerm implements Serializable {
    /**
     * Checks a matching criteria defined by the concrete subclass of this Term.
     *
     * @param message the message to apply the matching criteria to
     * @return true if the matching criteria is met
     */
    public abstract boolean match(Message message);
}
