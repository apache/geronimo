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

import java.util.Date;

/**
 * @version $Rev$ $Date$
 */
public abstract class DateTerm extends ComparisonTerm {
    protected Date date;

    protected DateTerm(int comparison, Date date) {
        super(comparison);
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        this.date = date;
    }

    public boolean equals(Object other) {
        return super.equals(other) && ((DateTerm) other).date.equals(date);
    }

    public int getComparison() {
        return super.getComparison();
    }

    public Date getDate() {
        return date;
    }

    public int hashCode() {
        return super.hashCode() + date.hashCode();
    }

    protected boolean match(Date match) {
        return compare(date.compareTo(match));
    }
}
