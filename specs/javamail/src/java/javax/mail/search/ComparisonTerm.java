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
public abstract class ComparisonTerm extends SearchTerm {
    // Constants from J2SE 1.4 API Doc (Constant Values)
    public static final int EQ = 3;
    public static final int GE = 6;
    public static final int GT = 5;
    public static final int LE = 1;
    public static final int LT = 2;
    public static final int NE = 4;
    protected int comparison;

    // Dozy idiots didn't provide a constructor with an int comparison argument
    public ComparisonTerm() {
    }

    ComparisonTerm(int comparison) {
        this.comparison = comparison;
    }

    boolean compare(int answer) {
        if (answer == 0) {
            return (comparison == EQ || comparison == LE || comparison == GE);
        } else if (answer > 0) {
            return (comparison == GE || comparison == GT || comparison == NE);
        } else {
            return (comparison == LE || comparison == LT || comparison == NE);
        }
    }

    public boolean equals(Object other) {
        return super.equals(other)
                && ((ComparisonTerm) other).comparison == comparison;
    }

    int getComparison() {
        return comparison;
    }

    public int hashCode() {
        return super.hashCode() + comparison * 31;
    }
}
